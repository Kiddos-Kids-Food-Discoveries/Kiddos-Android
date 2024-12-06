package org.bangkit.kiddos_android.domain.model

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("articleId")
    val articleId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("article_picture")
    val articlePicture: String,

    @SerializedName("created_at")
    val createdAt: String
)
