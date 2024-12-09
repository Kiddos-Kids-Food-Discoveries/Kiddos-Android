package org.bangkit.kiddos_android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.domain.model.Data
import java.text.SimpleDateFormat
import java.util.*

class ArticlesAdapter(
    private val articles: List<Data>,
    private val onItemClick: (Data) -> Unit
) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
        holder.itemView.setOnClickListener { onItemClick(article) }
    }

    override fun getItemCount(): Int = articles.size

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivArticlesImage: ImageView = itemView.findViewById(R.id.ivarticlesImage)
        private val tvPublishAt: TextView = itemView.findViewById(R.id.tvPublishAt)
        private val tvArticlesTitle: TextView = itemView.findViewById(R.id.tvarticlesTitle)
        private val tvArticlesDescription: TextView = itemView.findViewById(R.id.tvarticlesDescription)

        fun bind(article: Data) {
            Glide.with(itemView.context)
                .load(article.articlePicture)
                .placeholder(R.drawable.fruit_loops_placeholder)
                .into(ivArticlesImage)

            // Convert and format the date
            val formattedDate = formatDate(article.createdAt)
            tvPublishAt.text = formattedDate

            tvArticlesTitle.text = article.title
            tvArticlesDescription.text = article.content
        }

        private fun formatDate(dateString: String): String {
            try {
                // Define the input and output date formats
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

                // Parse the input date
                val date = inputFormat.parse(dateString)

                // Return the formatted date
                return if (date != null) {
                    outputFormat.format(date)
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}