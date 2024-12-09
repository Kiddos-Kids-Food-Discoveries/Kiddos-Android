package org.bangkit.kiddos_android.ui.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.databinding.ActivityHistoryDetailBinding
import org.bangkit.kiddos_android.domain.model.HistoryItem
import org.bangkit.kiddos_android.ui.adapter.NutritionAdapter
import java.util.*

class HistoryDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var nutritionAdapter: NutritionAdapter
    private var textToSpeech: TextToSpeech? = null
    private var isTranslated = false  // Flag to keep track of translation state
    private val translationMap = mapOf(
        "tofu" to "tahu",
        "grape" to "anggur"
        // Add more translations as needed
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)  // Initialize TextToSpeech

        val historyItem = intent.getParcelableExtra<HistoryItem>("HISTORY_ITEM")

        historyItem?.let { item ->
            binding.foodNameTitle.text = item.prediction.foodInfo.nama
            binding.foodNameDescription.text = item.prediction.foodInfo.nama
            binding.foodDescription.text = item.prediction.foodInfo.deskripsi

            Glide.with(this).load(item.inputImage).into(binding.imageView)

            item.prediction.foodInfo.nutrition.let { nutrition ->
                val nutritionList = listOf(
                    "Kalori" to nutrition.kalori.toString(),
                    "Protein" to nutrition.protein.toString(),
                    "Karbohidrat" to nutrition.karbohidrat.toString(),
                    "Vitamin A" to nutrition.vitaminA,
                    "Vitamin C" to nutrition.vitaminC
                )
                nutritionAdapter = NutritionAdapter(nutritionList)
                binding.foodNutritionRecycler.apply {
                    layoutManager = LinearLayoutManager(this@HistoryDetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = nutritionAdapter
                }
            }

            binding.btnTranslation.setOnClickListener {
                toggleTranslation(item.prediction.foodInfo.nama)
            }

            binding.btnTextToSpeech.setOnClickListener {
                speakOut(item.prediction.foodInfo.nama)
            }
        }

        binding.imageButton.setOnClickListener {
            finish()
        }
    }

    private fun toggleTranslation(foodName: String) {
        if (isTranslated) {
            // If currently translated, switch back to English
            binding.foodNameTitle.text = foodName
            binding.foodNameDescription.text = foodName
        } else {
            // If currently in English, translate to Indonesian
            val translatedName = translateFoodName(foodName)
            binding.foodNameTitle.text = translatedName
            binding.foodNameDescription.text = translatedName
        }
        isTranslated = !isTranslated  // Toggle the translation state
    }

    private fun translateFoodName(foodName: String?): String {
        return translationMap[foodName] ?: foodName ?: "Unknown"
    }

    private fun speakOut(foodName: String?) {
        val textToSpeak = if (isTranslated) binding.foodNameTitle.text.toString() else foodName
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
