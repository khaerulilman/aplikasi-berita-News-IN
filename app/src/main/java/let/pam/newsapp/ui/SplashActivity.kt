package let.pam.newsapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import let.pam.newsapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        lifecycleScope.launch {
            delay(2000)
            navigateToNextActivity()
        }
    }

    private fun navigateToNextActivity() {
        val currentUser = auth.currentUser

        // If the user is logged in, navigate to NewsActivity, else navigate to LoginActivity
        val intent = if (currentUser != null) {
            Intent(this, NewsActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish() // Close SplashActivity after navigating
    }
}
