package let.pam.newsapp.ui.ViewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import let.pam.newsapp.repository.NewsFavoriteRepository
import let.pam.newsapp.repository.NewsApiRepository
import let.pam.newsapp.repository.UserRepository

class NewsViewModelProviderFactory(
    private val app: Application,
    private val newsApiRepository: NewsApiRepository,
    private val newsFavoriteRepository: NewsFavoriteRepository,
    private val userRepository: UserRepository,
    private val username: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            return NewsViewModel(
                app,
                newsApiRepository,
                newsFavoriteRepository,
                userRepository,
                username
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}