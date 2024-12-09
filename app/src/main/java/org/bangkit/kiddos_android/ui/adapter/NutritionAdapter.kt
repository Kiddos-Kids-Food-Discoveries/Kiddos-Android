package org.bangkit.kiddos_android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bangkit.kiddos_android.databinding.ItemNutritionBinding

class NutritionAdapter(private val nutritionList: List<Pair<String, String>>) :
    RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionViewHolder {
        val binding = ItemNutritionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NutritionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutritionViewHolder, position: Int) {
        val (name, value) = nutritionList[position]
        holder.bind(name, value)
    }

    override fun getItemCount() = nutritionList.size

    inner class NutritionViewHolder(private val binding: ItemNutritionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String, value: String) {
            binding.nutritionName.text = value
            binding.nutritionAmount.text = name
        }
    }
}
