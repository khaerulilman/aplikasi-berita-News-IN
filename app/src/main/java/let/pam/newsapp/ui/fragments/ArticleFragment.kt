package let.pam.newsapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import let.pam.newsapp.R
import let.pam.newsapp.databinding.FragmentArticleBinding
import let.pam.newsapp.models.Article
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel

class ArticleFragment : Fragment(R.layout.fragment_article) {
    private lateinit var newsViewModel: NewsViewModel
    private val args: ArticleFragmentArgs by navArgs()
    private lateinit var binding: FragmentArticleBinding
    private lateinit var titleText: TextView
    private lateinit var sourceText: TextView
    private lateinit var dateTimeText: TextView
    private lateinit var contentText: TextView
    private lateinit var authorText: TextView
    private lateinit var articleImage: ImageView
    private lateinit var bookmarkButton: ImageButton


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        titleText = view.findViewById(R.id.articleTitle)
        sourceText = view.findViewById(R.id.articleSource)
        dateTimeText = view.findViewById(R.id.articleDateTime)
        contentText = view.findViewById(R.id.articleContent)
        authorText = view.findViewById(R.id.articleAuthor)
        articleImage = view.findViewById(R.id.articleImage)
        bookmarkButton = view.findViewById(R.id.btnBookmark)
        val urlText = view.findViewById<TextView>(R.id.articleUrl)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val article = args.article
        val publishedAt = article.publishedAt ?: return

        setContent(article)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBookmark.setOnClickListener {
            if (newsViewModel.isArticleSaved(publishedAt)) {
                newsViewModel.deleteArticle(publishedAt)
                Snackbar.make(view, "Remove Success", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        newsViewModel.addNewsToFavorites(article)
                    }
                    show()
                }
            } else {
                newsViewModel.addNewsToFavorites(article)
                Snackbar.make(view, "Added to favorites", Snackbar.LENGTH_SHORT).show()
            }
        }

        urlText.setOnClickListener {
            article.url?.let { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    Snackbar.make(view, "Error opening link", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        newsViewModel.getFavoriteNewsByPublishedAt(publishedAt)

        newsViewModel.articleSaveStatus.observe(viewLifecycleOwner) { statusMap ->
            val isSaved = statusMap[publishedAt] ?: false
            bookmarkButton.setImageResource(
                if (isSaved) R.drawable.baseline_bookmark_24
                else R.drawable.ic_bookmark
            )
        }
    }


    private fun setContent(article: Article) {
        titleText.text = article.title
        sourceText.text = article.source?.name ?: ""
        dateTimeText.text = article.publishedAt?.substring(0, 10)
        authorText.text = article.author

        val fullContent = buildString {
            article.content?.let { append(it.substringBefore("[")) }
            append("\n\n")
            article.description?.let { append(it) }
        }

        contentText.text = fullContent
        val readMoreText: TextView = requireView().findViewById(R.id.articleReadMoreText)
        val urlText: TextView = requireView().findViewById(R.id.articleUrl)

        readMoreText.visibility = if (article.url.isNullOrEmpty()) View.GONE else View.VISIBLE
        urlText.visibility = if (article.url.isNullOrEmpty()) View.GONE else View.VISIBLE
        urlText.text = article.url ?: "No URL provided"

        Glide.with(this)
            .load(article.urlToImage)
            .placeholder(R.drawable.image_newspaper)
            .error(R.drawable.image_newspaper)
            .into(articleImage)
    }
}