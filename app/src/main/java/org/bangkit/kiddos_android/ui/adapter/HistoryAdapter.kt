package org.bangkit.kiddos_android.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.bangkit.kiddos_android.databinding.ItemHistoryBinding
import org.bangkit.kiddos_android.domain.model.HistoryItem
import org.bangkit.kiddos_android.ui.activity.HistoryDetailActivity
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val historyList: List<HistoryItem>,
    private val onDeleteClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(historyItem: HistoryItem) {
            Glide.with(binding.imageEvent.context).load(historyItem.inputImage).into(binding.imageEvent)
            binding.tvTitle.text = historyItem.prediction.foodInfo.nama
            binding.tvDescription.text = historyItem.prediction.foodInfo.deskripsi
            binding.tvDate.text = formatTimestamp(historyItem.timestamp)

            binding.iconDelete.setOnClickListener {
                showDeleteConfirmationDialog(historyItem)
            }

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, HistoryDetailActivity::class.java).apply {
                    putExtra("HISTORY_ITEM", historyItem)
                }
                binding.root.context.startActivity(intent)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val date = inputFormat.parse(timestamp)
            return outputFormat.format(date)
        }

        private fun showDeleteConfirmationDialog(historyItem: HistoryItem) {
            val context = binding.root.context
            AlertDialog.Builder(context).apply {
                setTitle("Delete Confirmation")
                setMessage("Are you sure you want to delete this history item?")
                setPositiveButton("Yes") { dialog, _ ->
                    onDeleteClick(historyItem)
                    dialog.dismiss()
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }
}
