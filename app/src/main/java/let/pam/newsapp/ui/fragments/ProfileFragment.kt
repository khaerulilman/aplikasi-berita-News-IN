package let.pam.newsapp.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import let.pam.newsapp.R
import let.pam.newsapp.databinding.FragmentProfileBinding
import let.pam.newsapp.ui.LoginActivity
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel
import let.pam.newsapp.util.SessionManager

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NewsViewModel
    private lateinit var sessionManager: SessionManager  // Add this line

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())  // Add this line
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).newsViewModel

        // Setup user profile
        setupUserProfile()

        // Handle the "Edit Profile" button click
        binding.btnGoToEditProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Handle logout
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Handle "Hubungi Kami" button click to navigate to HubungiFragment
        binding.btnGoToHubungikami.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_hubungiFragment)
        }
    }

    // logout in profile fragment
    private fun performLogout() {
        // Clear session
        sessionManager.logout()

        // Navigate to login activity
        Intent(requireContext(), LoginActivity::class.java).also { intent ->
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun setupUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.apply {
                    tvUsername.text = user.username ?: "No username"
                    tvEmail.text = user.email ?: "No Email"
                    tvFullName.text = user.fullName ?: "No full name"
                    tvPassword.text = user.password ?: "No Password"

                }
            } else {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

