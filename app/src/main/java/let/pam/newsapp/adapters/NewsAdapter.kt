package let.pam.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import let.pam.newsapp.R
import let.pam.newsapp.models.Article
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private lateinit var articleImage: ImageView
    private lateinit var articleSource: TextView
    private lateinit var articleTitle: TextView
    private lateinit var articleDateTime: TextView

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Article) -> Unit)? = null

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]

        articleImage = holder.itemView.findViewById(R.id.articleImage)
        articleSource = holder.itemView.findViewById(R.id.articleSource)
        articleTitle = holder.itemView.findViewById(R.id.articleTitle)
        articleDateTime = holder.itemView.findViewById(R.id.articleDateTime)

        holder.itemView.apply {
            Glide.with(this)
                .load(article.urlToImage)
                .placeholder(R.drawable.image_newspaper)
                .error(R.drawable.image_newspaper)
                .into(articleImage)

            articleSource.text = article.source?.name
            articleTitle.text = article.title

            // Konversi waktu ke format rentang waktu
            val publishedAt = article.publishedAt // Contoh: "2025-01-01T12:00:00Z"
            if (publishedAt != null) {
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val articleDateTimeParsed = LocalDateTime.parse(publishedAt, formatter)
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Asia/Jakarta"))
                    .toLocalDateTime()
                val now = LocalDateTime.now(ZoneId.of("Asia/Jakarta"))

                val duration = ChronoUnit.SECONDS.between(articleDateTimeParsed, now)
                articleDateTime.text = getReadableTimeDifference(duration)
            } else {
                articleDateTime.text = "Waktu tidak tersedia"
            }

            setOnClickListener {
                onItemClickListener?.let {
                    it(article)
                }
            }
        }
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


    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

}
















