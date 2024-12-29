package let.pam.newsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import let.pam.newsapp.R
import let.pam.newsapp.adapters.NewsAdapter
import let.pam.newsapp.databinding.FragmentFavoriteBinding
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    private lateinit var noItemCard: CardView
    private lateinit var errorText: TextView
    private lateinit var binding: FragmentFavoriteBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoriteBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        noItemCard = view.findViewById(R.id.noItem)

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.no_item, null)

        errorText = view.findViewById(R.id.errorText)

        setupFavoritesRecycler()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_favoritesFragment_to_articleFragment, bundle)
        }

        newsViewModel.getFavoriteNews().observe(viewLifecycleOwner) { articles ->
            newsAdapter.differ.submitList(articles)
            if (articles.isNotEmpty()) {
                hideErrorMessage()
            } else {
                showErrorMessage("No data")
            }
        }
    }

    private var isError = false

    private fun hideErrorMessage() {
        noItemCard.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        noItemCard.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    private fun setupFavoritesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerFavourites.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}













