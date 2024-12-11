package org.bangkit.kiddos_android.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.remote.response.PredictResponse
import org.bangkit.kiddos_android.databinding.ActivityScanResultBinding
import org.bangkit.kiddos_android.ui.adapter.NutritionAdapter
import java.util.*

class ScanResultActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityScanResultBinding
    private lateinit var nutritionAdapter: NutritionAdapter
    private var textToSpeech: TextToSpeech? = null
    private var isTranslated = false

    val translationMap = mapOf(
        "apple" to "Apel",
        "avocado" to "Alpukat",
        "banana" to "Pisang",
        "broccoli" to "Brokoli",
        "carrot" to "Wortel",
        "chicken" to "Ayam",
        "corn" to "Jagung",
        "dragon fruit" to "Buah Naga",
        "egg" to "Telur",
        "grape" to "Anggur",
        "green vegetables" to "Sayuran Hijau",
        "orange" to "Jeruk",
        "porridge" to "Bubur",
        "potato" to "Kentang",
        "rice" to "Nasi",
        "tempeh" to "Tempe",
        "tofu" to "Tahu",
        "tomato" to "Tomat",
        "watermelon" to "Semangka"
    )

    private var originalFoodName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)

        val predictResponse = intent.getParcelableExtra<PredictResponse>("PREDICT_RESPONSE")

        predictResponse?.let { response ->
            originalFoodName = response.data?.prediction?.foodInfo?.nama
            binding.foodNameTitle.text = capitalizeWords(originalFoodName)
            binding.foodNameDescription.text = capitalizeWords(originalFoodName)
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
                response.data?.prediction?.foodInfo?.let { it1 -> toggleTranslation(it1.nama) }
            }

            binding.btnTextToSpeech.setOnClickListener {
                response.data?.prediction?.foodInfo?.let { it1 -> speakOut(it1.nama) }
            }
        }

        binding.scanButton.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigate_to", R.id.navigation_home) // Kirim informasi untuk navigasi ke HomeFragment
            startActivity(intent)
            finish()
        }

        binding.imageButton.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun toggleTranslation(foodName: String) {
        if (isTranslated) {
            val capitalizedFoodName = capitalizeWords(foodName)
            binding.foodNameTitle.text = capitalizedFoodName
            binding.foodNameDescription.text = capitalizedFoodName
        } else {
            val translatedName = translateFoodName(foodName)
            binding.foodNameTitle.text = translatedName
            binding.foodNameDescription.text = translatedName
        }
        isTranslated = !isTranslated
    }

    private fun capitalizeWords(text: String?): String {
        return text?.split(" ")?.joinToString(" ") { it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        } } ?: ""
    }

    private fun translateFoodName(foodName: String?): String {
        return translationMap[foodName] ?: foodName ?: "Unknown"
    }

    private fun speakOut(foodName: String?) {
        val textToSpeak = if (isTranslated) binding.foodNameTitle.text.toString() else foodName
        val locale = if (isTranslated) Locale("id", "ID") else Locale.US
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
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
        finish()
    }
}
