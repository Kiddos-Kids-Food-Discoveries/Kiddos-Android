package org.bangkit.kiddos_android.ui.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.core.util.Pair
import org.bangkit.kiddos_android.databinding.ItemHistoryBinding
import org.bangkit.kiddos_android.domain.model.HistoryItem
import org.bangkit.kiddos_android.ui.activity.HistoryDetailActivity
import org.bangkit.kiddos_android.utils.NetworkUtils
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
            binding.tvTitle.text = capitalizeWords(historyItem.prediction.foodInfo.nama)
            binding.tvDescription.text = historyItem.prediction.foodInfo.deskripsi
            binding.tvDate.text = formatTimestamp(historyItem.timestamp)

            binding.iconDelete.setOnClickListener {
                if (NetworkUtils.isNetworkAvailable(binding.root.context)) {
                    showDeleteConfirmationDialog(historyItem)
                } else {
                    Toast.makeText(binding.root.context, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                    Log.e("HistoryAdapter", "No internet connection")
                }
            }


            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(binding.root.context, HistoryDetailActivity::class.java).apply {
                    putExtra("HISTORY_ITEM", historyItem)
                }

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context as AppCompatActivity,
                    Pair(binding.tvTitle, "sharedName"),
                    Pair(binding.imageEvent, "sharedImage")
                )
                binding.root.context.startActivity(intent, options.toBundle())
            }
        }
        private fun capitalizeWords(text: String?): String {
            return text?.split(" ")?.joinToString(" ") { it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            } } ?: ""
        }

        private fun formatTimestamp(timestamp: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
            val date = inputFormat.parse(timestamp)
            return outputFormat.format(date)
        }


        private fun showDeleteConfirmationDialog(historyItem: HistoryItem) {
            val context = binding.root.context
            AlertDialog.Builder(context).apply {
                setTitle("Konfirmasi Hapus")
                setMessage("Apakah anda yakin ingin menghapus item riwayat ini?")
                setPositiveButton("Ya") { dialog, _ ->
                    onDeleteClick(historyItem)
                    dialog.dismiss()
                }
                setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }
}
