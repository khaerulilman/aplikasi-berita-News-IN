package let.pam.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import let.pam.newsapp.databinding.ActivityEditProfileBinding
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel

class EditProfileFragment : Fragment() {
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NewsViewModel

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

        setupUserProfile()
        setupSaveButton()
        setupBackButton()
    }

    private fun setupUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.apply {
                    tvUsername.setText(user.username ?: "No username")
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
        val newFullName = binding.tvFullName.text.toString()
        val newPassword = binding.tvPassword.text.toString()

        if (newDisplayUsername.isEmpty() || newFullName.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveUserProfile(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
