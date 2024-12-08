package org.bangkit.kiddos_android.ui.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.data.remote.response.PredictResponse
import org.bangkit.kiddos_android.databinding.ActivityScanResultBinding
import org.bangkit.kiddos_android.ui.adapter.NutritionAdapter
import java.util.*

class ScanResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityScanResultBinding
    private lateinit var nutritionAdapter: NutritionAdapter
    private var textToSpeech: TextToSpeech? = null
    private var isTranslated = false  // Flag to keep track of translation state

    private val translationMap = mapOf(
        "tofu" to "tahu",
        "grape" to "anggur"
        // Add more translations as needed
    )

    private var originalFoodName: String? = null  // Store the original food name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)  // Initialize TextToSpeech

        val predictResponse = intent.getParcelableExtra<PredictResponse>("PREDICT_RESPONSE")

        predictResponse?.let { response ->
            originalFoodName = response.data?.prediction?.foodInfo?.nama
            binding.foodNameTitle.text = originalFoodName
            binding.foodNameDescription.text = originalFoodName
            binding.foodDescription.text = response.data?.prediction?.foodInfo?.deskripsi

            Glide.with(this).load(response.data?.inputImage).into(binding.imageView)

            response.data?.prediction?.foodInfo?.nutrition?.let { nutrition ->
                val nutritionList = listOf(
                    "Kalori" to nutrition.kalori.toString(),
                    "Protein" to nutrition.protein.toString(),
                    "Karbohidrat" to nutrition.karbohidrat.toString(),
                    "Vitamin A" to nutrition.vitaminA,
                    "Vitamin C" to nutrition.vitaminC
                )
                nutritionAdapter = NutritionAdapter(nutritionList)
                binding.foodNutritionRecycler.apply {
                    layoutManager = LinearLayoutManager(this@ScanResultActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = nutritionAdapter
                }
            }

            binding.btnTranslation.setOnClickListener {
                toggleTranslation()
            }

            binding.btnTextToSpeech.setOnClickListener {
                speakOut()
            }
        }

        binding.galleryButton.setOnClickListener {
            // Handle Rescan button click
            finish()
        }

        binding.analyzeButton.setOnClickListener {
            // Handle Home button click
            finish()
        }

        binding.imageButton.setOnClickListener {
            // Handle back button click
            finish()
        }
    }

    private fun toggleTranslation() {
        if (isTranslated) {
            // If currently translated, switch back to English
            binding.foodNameTitle.text = originalFoodName
            binding.foodNameDescription.text = originalFoodName
        } else {
            // If currently in English, translate to Indonesian
            val translatedName = translateFoodName(originalFoodName)
            binding.foodNameTitle.text = translatedName
            binding.foodNameDescription.text = translatedName
        }
        isTranslated = !isTranslated  // Toggle the translation state
    }

    private fun translateFoodName(foodName: String?): String {
        return translationMap[foodName] ?: foodName ?: "Unknown"
    }

    private fun speakOut() {
        val textToSpeak = if (isTranslated) binding.foodNameTitle.text.toString() else originalFoodName
        val locale = if (isTranslated) Locale.US else Locale("id", "ID")
        textToSpeech?.language = locale
        textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Initialize with Indonesian by default
            val result = textToSpeech?.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
