package let.pam.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import let.pam.newsapp.R
import let.pam.newsapp.adapters.NewsAdapter
import let.pam.newsapp.databinding.FragmentHeadlinesBinding
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel
import let.pam.newsapp.util.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {

    lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var retryButton: Button
    private lateinit var errorText: TextView
    private lateinit var itemHeadlinesError: CardView
    private lateinit var binding: FragmentHeadlinesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlinesBinding.bind(view)

        itemHeadlinesError = view.findViewById(R.id.itemHeadlinesError)

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        newsViewModel = (activity as NewsActivity).newsViewModel

        setupHeadlinesRecycler()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articleFragment, bundle)
        }

        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                    }
                }

                is Resource.Error<*> -> {
                    response.message?.let { message ->
                        Toast.makeText(activity, "Sorry error: $message", Toast.LENGTH_SHORT).show()
                        showErrorMessage(message)
                    }
                }
            }
        })

        // Set tanggal sebelumnya ke tvDate
        val previousDates = getPreviousDates()
        binding.tvDate.text = previousDates

        binding.itemHeadlinesError.retryButton.setOnClickListener {
            newsViewModel.getHeadlinesNews("id")
        }
    }

    var isError = false

    private fun hideErrorMessage() {
        itemHeadlinesError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemHeadlinesError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    private fun setupHeadlinesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    fun getPreviousDates(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Tanggal pertama sebelumnya
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val previousDate1 = dateFormat.format(calendar.time)

        // Tanggal kedua sebelumnya
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val previousDate2 = dateFormat.format(calendar.time)

        // Format hasilnya
        return "Diperbarui Pada $previousDate1"
    }

}






















