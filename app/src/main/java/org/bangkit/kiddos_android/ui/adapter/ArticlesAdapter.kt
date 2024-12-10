package org.bangkit.kiddos_android.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.databinding.ItemArticleBinding
import org.bangkit.kiddos_android.domain.model.Data
import org.bangkit.kiddos_android.ui.activity.ArticleDetailActivity
import java.text.SimpleDateFormat
import java.util.*

class ArticlesAdapter(
    private val articles: List<Data>,
    private val onItemClick: (Data) -> Unit
) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                putExtra("articleTitle", article.title)
                putExtra("articleContent", article.content)
                putExtra("articlePicture", article.articlePicture)
                putExtra("createdAt", article.createdAt)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = articles.size

    class ArticleViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Data) {
            Glide.with(itemView.context)
                .load(article.articlePicture)
                .placeholder(R.drawable.fruit_loops_placeholder)
                .into(binding.ivarticlesImage)

            val formattedDate = formatDate(article.createdAt)
            binding.tvPublishAt.text = formattedDate

            binding.tvarticlesTitle.text = article.title
            binding.tvarticlesDescription.text = article.content
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(dateString)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}
