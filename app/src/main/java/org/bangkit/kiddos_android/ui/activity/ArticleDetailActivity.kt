package org.bangkit.kiddos_android.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.databinding.ActivityArticleDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleTitle = intent.getStringExtra("articleTitle")
        val articleContent = intent.getStringExtra("articleContent")
        val articlePicture = intent.getStringExtra("articlePicture")
        val createdAt = intent.getStringExtra("createdAt")

        Glide.with(this)
            .load(articlePicture)
            .into(binding.ivMediaCover)

        binding.tvArticleTitle.text = articleTitle
        binding.tvArticleContent.text = articleContent
        binding.tvCreatedAt.text = formatDate(createdAt)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun formatDate(dateString: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = dateString?.let { inputFormat.parse(it) }
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
