package let.pam.newsapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import let.pam.newsapp.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import let.pam.newsapp.ui.fragments.VerifyOtpFragment


class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFullName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var database: DatabaseReference
    private lateinit var registrationFormContainer: LinearLayout
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etFullName = findViewById(R.id.tvFullName)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        registrationFormContainer = findViewById(R.id.registration_form_container)
        fragmentContainer = findViewById(R.id.fragment_container)

        database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://myappnews-fa569-default-rtdb.asia-southeast1.firebasedatabase.app/")

        btnLogin.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val fullName = etFullName.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(applicationContext, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            database.child("users").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(applicationContext, "Email already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            database.child("users").child(username)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            Toast.makeText(applicationContext, "Username already exists", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Hide registration form and show fragment container
                                            registrationFormContainer.visibility = View.GONE
                                            fragmentContainer.visibility = View.VISIBLE

                                            // Navigate to VerifyOtpFragment
                                            val fragment = VerifyOtpFragment().apply {
                                                arguments = Bundle().apply {
                                                    putString("email", email)
                                                    putString("username", username)
                                                    putString("fullName", fullName)
                                                    putString("password", password)
                                                }
                                            }

                                            supportFragmentManager.beginTransaction()
                                                .replace(R.id.fragment_container, fragment)
                                                .addToBackStack(null)
                                                .commit()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            registrationFormContainer.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}
