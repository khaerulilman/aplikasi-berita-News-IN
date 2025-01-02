package let.pam.newsapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import let.pam.newsapp.R
import let.pam.newsapp.databinding.ActivityEditProfileBinding
import let.pam.newsapp.ui.LoginActivity
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel
import let.pam.newsapp.util.SessionManager

class EditProfileFragment : Fragment() {
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NewsViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).newsViewModel
        sessionManager = SessionManager(requireContext())

        database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://myappnews-fa569-default-rtdb.asia-southeast1.firebasedatabase.app/")

        setupUserProfile()
        setupSaveButton()
        setupBackButton()
        setupDeleteButton()
    }

    private fun setupUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.apply {
                    tvUsername.setText(user.username ?: "No username")
                    tvEmail.setText(user.email ?: "No Email")
                    tvFullName.setText(user.fullName ?: "No full name")
                    tvPassword.setText(user.password ?: "No password")
                }
            }
        }
        viewModel.loadUserProfile()
    }

    private fun setupBackButton() {
        binding.btnGoToProfile.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSaveButton() {
        binding.btnSubmit.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun saveUserProfile() {
        val newDisplayUsername = binding.tvUsername.text.toString()
        val newEmail = binding.tvEmail.text.toString()
        val newFullName = binding.tvFullName.text.toString()
        val newPassword = binding.tvPassword.text.toString()

        if (newEmail.isEmpty() || newDisplayUsername.isEmpty() ||
            newFullName.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if email was changed
        if (newEmail != viewModel.userProfile.value?.email) {
            // Check if new email already exists
            database.child("users").orderByChild("email").equalTo(newEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(context, "Email already registered", Toast.LENGTH_SHORT).show()
                        } else {
                            // Proceed with OTP verification
                            val bundle = Bundle().apply {
                                putString("email", newEmail)
                                putString("username", newDisplayUsername)
                                putString("fullName", newFullName)
                                putString("password", newPassword)
                                putBoolean("isProfileEdit", true)
                            }
                            findNavController().navigate(
                                R.id.action_editProfileFragment_to_verifyOtpFragment,
                                bundle
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            // Save directly if email unchanged
            viewModel.saveUserProfile(
                newEmail = newEmail,
                newFullName = newFullName,
                newPassword = newPassword,
                newDisplayUsername = newDisplayUsername
            ) { success ->
                if (success) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun deleteUserAccount() {
        viewModel.deleteUserAccount { success ->
            if (success) {
                // Logout dari sistem
                viewModel.logout()

                // Clear session
                sessionManager.logout()

                // Show toast
                Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                // Navigate to LoginActivity with proper flags
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    // Add extra to indicate account deletion if needed
                    putExtra("ACCOUNT_DELETED", true)
                }

                // Start LoginActivity
                startActivity(intent)

                // Finish all activities in the stack
                requireActivity().finishAffinity()
            } else {
                Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDeleteButton() {
        binding.btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteUserAccount()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
