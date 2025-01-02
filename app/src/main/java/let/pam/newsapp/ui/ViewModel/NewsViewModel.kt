package let.pam.newsapp.ui.ViewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import let.pam.newsapp.models.Article
import let.pam.newsapp.models.User
import let.pam.newsapp.repository.UserRepository
import let.pam.newsapp.util.Resource
import kotlinx.coroutines.launch
import let.pam.newsapp.models.NewsResponse
import let.pam.newsapp.repository.NewsApiRepository
import let.pam.newsapp.repository.NewsFavoriteRepository
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewsViewModel(
    app: Application,
    private val newsApiRepository: NewsApiRepository,
    private val newsFavoriteRepository: NewsFavoriteRepository,
    private val userRepository: UserRepository,
    private val username: String
) : AndroidViewModel(app) {
    // headline val
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    private var headlinesResponse: NewsResponse? = null

    // article favorite val
    private val _articleSaveStatus = MutableLiveData<Map<String, Boolean>>(mapOf())
    val articleSaveStatus: LiveData<Map<String, Boolean>> get() = _articleSaveStatus

    // profile favorite val
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: MutableLiveData<User?> = _userProfile

    // Headline View Models
    init {
        getHeadlinesNews("us")
    }

    fun getHeadlinesNews(countryCode: String) = viewModelScope.launch {
        headlinesNewsRemote(countryCode)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++

                // Get current date and date from 1 week ago
                val currentDate = Calendar.getInstance()
                val oneWeekAgo = Calendar.getInstance()
                oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7)

                // Filter out articles with null/removed content AND filter by date
                val filteredArticles = resultResponse.articles.filter { article ->
                    // First check content is valid
                    val hasValidContent = article.content != null && article.content != "[Removed]"

                    // Then check publication date
                    val articleDate = try {
                        article.publishedAt?.let { publishedAt ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            formatter.parse(publishedAt)?.let { date ->
                                val cal = Calendar.getInstance()
                                cal.time = date
                                cal
                            }
                        }
                    } catch (e: Exception) {
                        null
                    }

                    hasValidContent && articleDate != null &&
                            articleDate.after(oneWeekAgo) && articleDate.before(currentDate)
                }.toMutableList()

                // Update filtered articles in response
                resultResponse.articles.clear()
                resultResponse.articles.addAll(filteredArticles)

                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticles = headlinesResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun headlinesNewsRemote(countryCode: String) {
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsApiRepository.getHeadlines(countryCode, headlinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    // Favorite Article View Models
    fun addNewsToFavorites(article: Article) = viewModelScope.launch {
        val publishedAt = article.publishedAt ?: return@launch
        val result = newsFavoriteRepository.insert(article)
        if (result > 0) {
            updateArticleSaveStatus(publishedAt, true)
        }
    }

    fun getFavoriteNews() = newsFavoriteRepository.getFavoriteNews()

    fun getFavoriteNewsByPublishedAt(publishedAt: String) {
        newsFavoriteRepository.getFavoriteNewsByPublishedAt(publishedAt).observeForever { article ->
            updateArticleSaveStatus(publishedAt, article?.id != null)
        }
    }

    private fun updateArticleSaveStatus(publishedAt: String, isSaved: Boolean) {
        val currentMap = _articleSaveStatus.value?.toMutableMap() ?: mutableMapOf()
        currentMap[publishedAt] = isSaved
        _articleSaveStatus.value = currentMap
    }

    fun isArticleSaved(publishedAt: String): Boolean {
        return _articleSaveStatus.value?.get(publishedAt) ?: false
    }

    fun deleteArticle(publishedAt: String) = viewModelScope.launch {
        try {
            val result = newsFavoriteRepository.deleteArticle(publishedAt)
            if (result > 0) {
                updateArticleSaveStatus(publishedAt, false)
            }
        } catch (e: Exception) {
            Log.d("NewsViewModel", "Delete article error: ${e.message}")
        }
    }

    // Profile View Models
    fun loadUserProfile() {
        viewModelScope.launch {
            val user = userRepository.getUserProfile(username)
            _userProfile.postValue(user)
        }
    }

    fun saveUserProfile(newEmail: String, newFullName: String, newPassword: String, newDisplayUsername: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.updateUserProfile(
                originalUsername = username,
                newEmail = newEmail,
                newFullName = newFullName,
                newPassword = newPassword,
                newDisplayUsername = newDisplayUsername
            )
            if (success) {
                _userProfile.postValue(User(newDisplayUsername, newEmail, newFullName, newPassword))
            }
            callback(success)
        }
    }

    fun deleteUserAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.deleteUser(username)
            onComplete(success)
        }
    }

    fun logout() {
        userRepository.logout()
    }

    // internet connection
    private fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }
}
