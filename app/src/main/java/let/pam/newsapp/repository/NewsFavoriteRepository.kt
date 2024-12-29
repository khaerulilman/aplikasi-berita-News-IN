package let.pam.newsapp.repository

import let.pam.newsapp.db.FirebaseDAO
import let.pam.newsapp.models.Article

class NewsFavoriteRepository(private val firebaseDAO: FirebaseDAO) {

    suspend fun insert(article: Article) = firebaseDAO.insert(article)

    fun getFavoriteNews() = firebaseDAO.getAllUserArticles()

    fun getFavoriteNewsByPublishedAt(publishedAt: String) =
        firebaseDAO.getArticleByPublishedAt(publishedAt)

    suspend fun deleteArticle(publishedAt: String) =
        firebaseDAO.deleteArticleByPublishedAt(publishedAt)

}