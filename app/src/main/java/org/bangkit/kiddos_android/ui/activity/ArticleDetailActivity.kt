package org.bangkit.kiddos_android.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.databinding.ActivityArticleDetailBinding

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
        binding.tvCreatedAt.text = createdAt

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}