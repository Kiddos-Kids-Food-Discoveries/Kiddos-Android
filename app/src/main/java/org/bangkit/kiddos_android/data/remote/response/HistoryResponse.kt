package org.bangkit.kiddos_android.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bangkit.kiddos_android.domain.model.HistoryItem

@Parcelize
data class HistoryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<HistoryItem>
)  : Parcelable

