package let.pam.newsapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import let.pam.newsapp.R
import let.pam.newsapp.ui.LoginActivity
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class VerifyOtpFragment : Fragment() {
    private lateinit var etDigit1: EditText
    private lateinit var etDigit2: EditText
    private lateinit var etDigit3: EditText
    private lateinit var etDigit4: EditText
    private lateinit var etDigit5: EditText
    private lateinit var etDigit6: EditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var tvResendOtp: TextView
    private lateinit var database: DatabaseReference
    private lateinit var countDownTimer: CountDownTimer

    private var userEmail: String = ""
    private var username: String = ""
    private var fullName: String = ""
    private var password: String = ""
    private var generatedOTP: String = ""
    private var isResendEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        etDigit1 = view.findViewById(R.id.etDigit1)
        etDigit2 = view.findViewById(R.id.etDigit2)
        etDigit3 = view.findViewById(R.id.etDigit3)
        etDigit4 = view.findViewById(R.id.etDigit4)
        etDigit5 = view.findViewById(R.id.etDigit5)
        etDigit6 = view.findViewById(R.id.etDigit6)
        btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp)
        tvResendOtp = view.findViewById(R.id.tvResendOtp)

        // Get user data from arguments
        arguments?.let {
            userEmail = it.getString("email", "")
            username = it.getString("username", "")
            fullName = it.getString("fullName", "")
            password = it.getString("password", "")
        }

        database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://myappnews-fa569-default-rtdb.asia-southeast1.firebasedatabase.app/")

        // Setup OTP box navigation
        setupOtpInputs()

        // Generate and send initial OTP
        generateAndSendOTP()
        startResendTimer()

        btnVerifyOtp.setOnClickListener {
            val enteredOTP = "${etDigit1.text}${etDigit2.text}${etDigit3.text}${etDigit4.text}${etDigit5.text}${etDigit6.text}"
            verifyOTP(enteredOTP)
        }

        tvResendOtp.setOnClickListener {
            if (isResendEnabled) {
                generateAndSendOTP()
                startResendTimer()
            }
        }
    }

    private fun startResendTimer() {
        isResendEnabled = false
        tvResendOtp.isEnabled = false
        tvResendOtp.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

        countDownTimer = object : CountDownTimer(40000, 1000) { // Ubah dari 20000 menjadi 40000
            override fun onTick(millisUntilFinished: Long) {
                tvResendOtp.text = "Resend OTP in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                isResendEnabled = true
                tvResendOtp.isEnabled = true
                tvResendOtp.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                tvResendOtp.text = "Resend OTP"
            }
        }.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }


    private fun setupOtpInputs() {
        val editTexts = listOf(etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6)

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus()
                    } else if (s?.length == 0 && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }
                }
            })
        }
    }

    private fun generateAndSendOTP() {
        generatedOTP = (100000..999999).random().toString()

        // Configure JavaMail
        val props = Properties()
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"

        // Launch coroutine for email sending
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("ilmanclasher@gmail.com", "nwkb atlb vzcn fcdn")
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress("ilmanclasher@gmail.com"))
                message.addRecipient(Message.RecipientType.TO, InternetAddress(userEmail))
                message.subject = "Your OTP Code"
                message.setText("Your OTP code is: $generatedOTP")

                Transport.send(message)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to send OTP: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyOTP(enteredOTP: String) {
        if (enteredOTP == generatedOTP) {
            val isProfileEdit = arguments?.getBoolean("isProfileEdit", false) ?: false

            val user = HashMap<String, String>()
            user["username"] = username
            user["email"] = userEmail
            user["fullName"] = fullName
            user["password"] = password

            if (isProfileEdit) {
                // Update existing user
                database.child("users").child(username).updateChildren(user as Map<String, Any>)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                            // Navigate back to profile
                            findNavController().navigate(R.id.action_verifyOtpFragment_to_profileFragment)
                        } else {
                            Toast.makeText(context, "Update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Create new user (existing registration logic)
                database.child("users").child(username).setValue(user)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(context, LoginActivity::class.java))
                            activity?.finish()
                        } else {
                            Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
        }
    }
}