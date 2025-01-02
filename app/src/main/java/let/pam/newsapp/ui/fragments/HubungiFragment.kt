package let.pam.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import let.pam.newsapp.R
import let.pam.newsapp.databinding.FragmentHubungiBinding
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel

class HubungiFragment : Fragment() {

    private var _binding: FragmentHubungiBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NewsViewModel
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHubungiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).newsViewModel

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        val myRef = database.getReferenceFromUrl("https://myappnews-fa569-default-rtdb.asia-southeast1.firebasedatabase.app/")

        binding.btnKirim.setOnClickListener {
            val nama = binding.tvFullname.text.toString()
            val email = binding.tvEmail.text.toString()
            val pesan = binding.etPesan.text.toString()

            if (validateForm(nama, email, pesan)) {
                // Create customer object
                val customer = HashMap<String, String>()
                customer["nama"] = nama
                customer["email"] = email
                customer["pesan"] = pesan

                // Save to Firebase under "customer" node with push() to generate unique key
                myRef.child("customer").push().setValue(customer)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Pesan berhasil terkirim!", Toast.LENGTH_SHORT).show()
                        clearForm()
                        // Optional: Navigate back to profile
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal mengirim pesan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnGoToProfile.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_hubungiFragment_to_profileFragment)
        }

        setupUserProfile()
    }

    private fun setupUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.apply {
                    tvFullname.setText(user.fullName ?: "NoFull Name")
                    tvEmail.setText(user.email ?: "No Email")
                }
            }
        }
        viewModel.loadUserProfile()
    }

    private fun validateForm(nama: String, email: String, pesan: String): Boolean {
        if (nama.isEmpty()) {
            binding.tvFullname.error = "Nama tidak boleh kosong"
            return false
        }
        if (email.isEmpty()) {
            binding.tvEmail.error = "Email tidak boleh kosong"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tvEmail.error = "Format email tidak valid"
            return false
        }
        if (pesan.isEmpty()) {
            binding.etPesan.error = "Pesan tidak boleh kosong"
            return false
        }
        return true
    }

    private fun clearForm() {
        binding.tvFullname.text?.clear()
        binding.tvEmail.text?.clear()
        binding.etPesan.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}