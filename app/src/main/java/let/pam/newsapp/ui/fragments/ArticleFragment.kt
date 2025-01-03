package let.pam.newsapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import let.pam.newsapp.R
import let.pam.newsapp.databinding.FragmentArticleBinding
import let.pam.newsapp.models.Article
import let.pam.newsapp.ui.NewsActivity
import let.pam.newsapp.ui.ViewModel.NewsViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

        setupWebViewButton()

        newsViewModel.getFavoriteNewsByPublishedAt(publishedAt)

        newsViewModel.articleSaveStatus.observe(viewLifecycleOwner) { statusMap ->
            val isSaved = statusMap[publishedAt] ?: false
            bookmarkButton.setImageResource(
                if (isSaved) R.drawable.baseline_bookmark_24
                else R.drawable.ic_bookmark
            )
        }
    }

    private fun setupWebViewButton() {
        binding.btnGoToWebview.setOnClickListener {
            val url = args.article.url
            if (!url.isNullOrEmpty()) {
                val action = ArticleFragmentDirections.actionArticleFragmentToWebViewFragment(url)
                findNavController().navigate(action)
            } else {
                Snackbar.make(binding.root, "URL tidak tersedia", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun setContent(article: Article) {
        titleText.text = article.title
        sourceText.text = article.source?.name ?: ""
        authorText.text = article.author ?: "Anonym"

        // Konversi waktu ke rentang waktu
        val publishedAt = article.publishedAt
        if (publishedAt != null) {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val articleDateTimeParsed = LocalDateTime.parse(publishedAt, formatter)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Jakarta"))
                .toLocalDateTime()
            val now = LocalDateTime.now(ZoneId.of("Asia/Jakarta"))

            val duration = ChronoUnit.SECONDS.between(articleDateTimeParsed, now)
            dateTimeText.text = getReadableTimeDifference(duration)
        } else {
            dateTimeText.text = "Waktu tidak tersedia"
        }

        val fullContent = buildString {
            article.content?.let {
                if (it.contains("[")) {
                    append(it.substringBefore("["))
                } else {
                    append(it) // Use full content if no "[" present
                }
            }
            append("\n\n")
            article.description?.let { append(it) }
        }

        // Remove HTML tags
        val contentWithoutHtmlTags = fullContent.replace(Regex("<[^>]*>"), "")

        // Split the content into paragraphs
        val paragraphs = contentWithoutHtmlTags.trim().split("\n\n")

        // Process each paragraph by removing the last word
        val contentWithoutLastWordInEachParagraph = paragraphs.joinToString("\n\n") { paragraph ->
            val words = paragraph.split(" ")
            if (words.size > 1) {
                words.dropLast(1).joinToString(" ") // Remove last word from each paragraph
            } else {
                "" // If the paragraph has only one word, return an empty string
            }
        }

        // Limit content to a maximum of 24 words
        val limitedContent = contentWithoutLastWordInEachParagraph.split(" ").let { words ->
            if (words.size > 24) {
                words.take(24).joinToString(" ") + "..." // Add ellipsis if truncated
            } else {
                words.joinToString(" ")
            }
        }

        contentText.text = limitedContent.trim() // Ensure no extra spaces

        Glide.with(this)
            .load(article.urlToImage)
            .placeholder(R.drawable.image_newspaper)
            .error(R.drawable.image_newspaper)
            .into(articleImage)
    }

    // Fungsi untuk mendapatkan rentang waktu dalam format string
    private fun getReadableTimeDifference(durationInSeconds: Long): String {
        val seconds = durationInSeconds % 60
        val minutes = (durationInSeconds / 60) % 60
        val hours = (durationInSeconds / (60 * 60)) % 24
        val days = durationInSeconds / (60 * 60 * 24)

        return when {
            days > 0 -> "$days hari ${hours} jam yang lalu"
            hours > 0 -> "$hours jam ${minutes} menit yang lalu"
            minutes > 0 -> "$minutes menit ${seconds} detik yang lalu"
            else -> "$seconds detik yang lalu"
        }
    }
}