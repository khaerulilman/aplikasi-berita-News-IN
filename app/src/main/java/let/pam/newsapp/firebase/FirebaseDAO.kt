package let.pam.newsapp.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import let.pam.newsapp.models.Article
import let.pam.newsapp.models.User
import kotlinx.coroutines.tasks.await

class FirebaseDAO {
    private val database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://myappnews-fa569-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var currentUsername: String? = null

    fun setCurrentUsername(username: String) {
        currentUsername = username
    }

    // article DAO
    private fun getUserArticlesRef() = database.child("users")
        .child(currentUsername ?: throw IllegalStateException("Username not set"))
        .child("articles")

    private fun getUsersRef() = database.child("users")

    suspend fun insert(article: Article): Long {
        if (currentUsername == null) return -1

        val articlesRef = getUserArticlesRef()
        val key = articlesRef.push().key ?: return -1
        article.id = key
        articlesRef.child(key).setValue(article).await()
        return 1
    }

    fun getAllUserArticles(): LiveData<List<Article>> {
        val articlesLiveData = MutableLiveData<List<Article>>()

        if (currentUsername == null) {
            articlesLiveData.value = emptyList()
            return articlesLiveData
        }

        getUserArticlesRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val articles = snapshot.children.mapNotNull { it.getValue(Article::class.java) }
                articlesLiveData.value = articles
            }

            override fun onCancelled(error: DatabaseError) {
                articlesLiveData.value = emptyList()
            }
        })

        return articlesLiveData
    }

    fun getArticleByPublishedAt(publishedAt: String): LiveData<Article> {
        val articleLiveData = MutableLiveData<Article>()

        if (currentUsername == null) {
            return articleLiveData
        }

        getUserArticlesRef().orderByChild("publishedAt").equalTo(publishedAt)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.firstOrNull()?.getValue(Article::class.java)?.let {
                        articleLiveData.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

        return articleLiveData
    }

    suspend fun deleteArticleByPublishedAt(publishedAt: String): Int {
        if (currentUsername == null) return 0

        return try {
            getUserArticlesRef().orderByChild("publishedAt").equalTo(publishedAt).get().await()
                .children.firstOrNull()?.key?.let { key ->
                    getUserArticlesRef().child(key).removeValue().await()
                    1
                } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // User DAO
    suspend fun getUserProfile(username: String): User? {
        return try {
            val snapshot = getUsersRef().child(username).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(
        originalUsername: String,
        newFullName: String,
        newPassword: String,
        newDisplayUsername: String
    ): Boolean {
        return try {
            val updates = hashMapOf<String, Any>(
                "username" to newDisplayUsername,
                "fullName" to newFullName,
                "password" to newPassword
            )

            getUsersRef().child(originalUsername).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}