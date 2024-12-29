package let.pam.newsapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import let.pam.newsapp.R
import let.pam.newsapp.databinding.ActivityNewsBinding
import let.pam.newsapp.db.FirebaseDAO
import let.pam.newsapp.repository.NewsFavoriteRepository
import let.pam.newsapp.repository.NewsApiRepository
import let.pam.newsapp.repository.UserRepository
import let.pam.newsapp.ui.ViewModel.NewsViewModel
import let.pam.newsapp.ui.ViewModel.NewsViewModelProviderFactory
import let.pam.newsapp.util.SessionManager


class NewsActivity : AppCompatActivity() {
    lateinit var newsViewModel: NewsViewModel
    private lateinit var binding: ActivityNewsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager directly
        sessionManager = SessionManager(applicationContext)

        // Check if user is logged in, if not, redirect to login
        if (!sessionManager.checkLogin()) {
            startLoginActivity()
            return
        }

        // Get username using the direct method
        val username = sessionManager.getUsername()
        if (username == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_LONG).show()
            startLoginActivity()
            return
        }

        // Initialize ViewModel with username
        val newsApiRepository = NewsApiRepository()
        val firebaseDAO = FirebaseDAO().apply {
            setCurrentUsername(username)
        }

        class NewsActivity : AppCompatActivity() {
            lateinit var newsViewModel: NewsViewModel
            private lateinit var binding: ActivityNewsBinding
            private lateinit var sessionManager: SessionManager

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                binding = ActivityNewsBinding.inflate(layoutInflater)
                setContentView(binding.root)

                // Initialize SessionManager directly
                sessionManager = SessionManager(applicationContext)

                // Check if user is logged in, if not, redirect to login
                if (!sessionManager.checkLogin()) {
                    startLoginActivity()
                    return
                }

                // Get username using the direct method
                val username = sessionManager.getUsername()
                if (username == null) {
                    Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_LONG).show()
                    startLoginActivity()
                    return
                }

                // Initialize ViewModel with username
                val newsApiRepository = NewsApiRepository()
                val firebaseDAO = FirebaseDAO().apply {
                    setCurrentUsername(username)
                }

                // Initialize UserRepository with firebaseDAO
                val userRepository = UserRepository(firebaseDAO)
                val newsFavoriteRepository = NewsFavoriteRepository(firebaseDAO)
                val viewModelProviderFactory = NewsViewModelProviderFactory(
                    application,
                    newsApiRepository,
                    newsFavoriteRepository,
                    userRepository,
                    username
                )

                newsViewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
                newsViewModel.loadUserProfile()

                // Setup navigation
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
                val navHostController = navHostFragment.navController
                binding.bottomNavigationView.setupWithNavController(navHostController)
            }

            private fun startLoginActivity() {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        val newsFavoriteRepository = NewsFavoriteRepository(firebaseDAO)
        val userRepository = UserRepository(firebaseDAO)
        val viewModelProviderFactory = NewsViewModelProviderFactory(
            application,
            newsApiRepository,
            newsFavoriteRepository,
            userRepository,
            username
        )

        newsViewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
        newsViewModel.loadUserProfile()

        // Setup navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navHostController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navHostController)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}