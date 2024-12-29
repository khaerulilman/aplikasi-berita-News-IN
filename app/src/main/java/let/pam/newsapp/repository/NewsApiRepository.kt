package let.pam.newsapp.repository

import let.pam.newsapp.api.RetrofitInstance

class NewsApiRepository {

    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadlines(countryCode, pageNumber)

}