package let.pam.newsapp.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "AppSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USERNAME = "username"  // Changed to public const
    }

    fun createLoginSession(username: String) {
        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    // Add direct method to get username
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun checkLogin(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        editor.apply {
            clear()
            apply()
        }
    }
}