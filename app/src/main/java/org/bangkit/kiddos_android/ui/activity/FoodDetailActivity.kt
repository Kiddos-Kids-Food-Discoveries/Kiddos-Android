package org.bangkit.kiddos_android.ui.activity

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.transition.TransitionInflater
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.databinding.ActivityFoodDetailBinding
import org.bangkit.kiddos_android.domain.model.FoodItem
import org.bangkit.kiddos_android.ui.adapter.NutritionAdapter
import java.util.*

class FoodDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityFoodDetailBinding
    private lateinit var nutritionAdapter: NutritionAdapter
    private var textToSpeech: TextToSpeech? = null
    private var isTranslated = false  // Flag to keep track of translation state
    private val translationMap = mapOf(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.move)
        window.sharedElementReturnTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.move)

        textToSpeech = TextToSpeech(this, this)  // Initialize TextToSpeech

        val foodItem = intent.getParcelableExtra<FoodItem>("FOOD_ITEM")

        foodItem?.let { item ->
            val foodName = item.nama
            binding.foodNameTitle.text = capitalizeWords(foodName)
            binding.foodNameDescription.text = capitalizeWords(foodName)
            binding.foodDescription.text = item.deskripsi

            Glide.with(this).load(item.makananPicture).into(binding.foodImage)

            item.nutrition.let { nutrition ->
                val nutritionList = listOf(
                    "Kalori" to nutrition.kalori.toString(),
                    "Protein" to nutrition.protein.toString(),
                    "Karbohidrat" to nutrition.karbohidrat.toString(),
                    "Vitamin A" to nutrition.vitaminA,
                    "Vitamin C" to nutrition.vitaminC
                )
                nutritionAdapter = NutritionAdapter(nutritionList)
                binding.foodNutritionRecycler.apply {
                    layoutManager = LinearLayoutManager(this@FoodDetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = nutritionAdapter
                }
            }

            binding.btnTranslation.setOnClickListener {
                toggleTranslation(item.nama)
            }

            binding.btnTextToSpeech.setOnClickListener {
                speakOut(item.nama)
            }
        }

        binding.imageButton.setOnClickListener {
            supportFinishAfterTransition()
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

    private fun translateFoodName(foodName: String?): String {
        return translationMap[foodName] ?: foodName ?: "Unknown"
    }

    private fun capitalizeWords(text: String?): String {
        return text?.split(" ")?.joinToString(" ") { it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        } } ?: ""
    }

    private fun speakOut(foodName: String?) {
        val textToSpeak = if (isTranslated) binding.foodNameTitle.text.toString() else foodName
        val locale = if (isTranslated) Locale("id", "ID") else Locale.US
        textToSpeech?.language = locale
        textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
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
        supportFinishAfterTransition()
        super.onBackPressed()
    }
}
