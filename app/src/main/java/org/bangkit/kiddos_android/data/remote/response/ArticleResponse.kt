package org.bangkit.kiddos_android.data.remote.response

import com.google.gson.annotations.SerializedName
import org.bangkit.kiddos_android.domain.model.Data

data class ArticleResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: List<Data>
)