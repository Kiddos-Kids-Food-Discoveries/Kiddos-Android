package org.bangkit.kiddos_android.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.databinding.ItemFoodBinding
import org.bangkit.kiddos_android.domain.model.FoodItem
import org.bangkit.kiddos_android.ui.activity.FoodDetailActivity
import java.util.Locale

class FoodAdapter(private val foodList: List<FoodItem>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position])
    }

    override fun getItemCount(): Int = foodList.size

    inner class FoodViewHolder(private val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foodItem: FoodItem) {
            binding.tvfoodTitle.text = capitalizeWords(foodItem.nama)
            binding.tvfoodsDescription.text = foodItem.deskripsi
            Glide.with(binding.ivarticlesImage.context).load(foodItem.makananPicture).into(binding.ivarticlesImage)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, FoodDetailActivity::class.java).apply {
                    putExtra("FOOD_ITEM", foodItem)
                }

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context as AppCompatActivity,
                    Pair(binding.tvfoodTitle, "sharedName"),
                    Pair(binding.ivarticlesImage, "sharedImage")
                )

                context.startActivity(intent, options.toBundle())
            }
        }
    }

    private fun capitalizeWords(text: String?): String {
        return text?.split(" ")?.joinToString(" ") { it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        } } ?: ""
    }
}
