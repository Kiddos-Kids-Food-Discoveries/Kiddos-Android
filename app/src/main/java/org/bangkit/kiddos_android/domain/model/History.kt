package org.bangkit.kiddos_android.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("prediction") val prediction: Prediction,
    @SerializedName("input_image") val inputImage: String
) : Parcelable

@Parcelize
data class Prediction(
    @SerializedName("predicted_class") val predictedClass: String,
    @SerializedName("confidence") val confidence: String,
    @SerializedName("food_info") val foodInfo: FoodInfo
) : Parcelable

@Parcelize
data class FoodInfo(
    @SerializedName("id") val id: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("nutrition") val nutrition: Nutrition,
    @SerializedName("makanan_picture") val makananPicture: String,
    @SerializedName("kategori") val kategori: String
) : Parcelable

@Parcelize
data class Nutrition(
    @SerializedName("kalori") val kalori: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("karbohidrat") val karbohidrat: Double,
    @SerializedName("vitamin A") val vitaminA: String,
    @SerializedName("vitamin C") val vitaminC: String
) : Parcelable
