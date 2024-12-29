package let.pam.newsapp.models

import java.io.Serializable

data class Article(
    var id: String? = null,
    val author: String? = null,
    val content: String? = null,
    val description: String? = null,
    val publishedAt: String? = null,
    val source: Source? = null,  // Make sure Source class is also correctly defined and has a no-argument constructor
    val title: String? = null,
    val url: String? = null,
    val urlToImage: String? = null
) : Serializable
