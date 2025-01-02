package let.pam.newsapp.repository

import android.provider.ContactsContract.CommonDataKinds.Email
import let.pam.newsapp.db.FirebaseDAO
import let.pam.newsapp.models.User

class UserRepository(private val firebaseDAO: FirebaseDAO) {

    suspend fun updateUserProfile(originalUsername: String, newEmail: String, newFullName: String, newPassword: String, newDisplayUsername: String): Boolean {
        return firebaseDAO.updateUserProfile(originalUsername, newEmail, newFullName, newPassword, newDisplayUsername)
    }

    suspend fun getUserProfile(username: String): User? {
        return firebaseDAO.getUserProfile(username)
    }

    suspend fun deleteUser(username: String): Boolean {
        return firebaseDAO.deleteUser(username)
    }

    fun logout() {
        firebaseDAO.logout()
    }
}
