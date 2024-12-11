package org.bangkit.kiddos_android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.repository.HistoryRepository
import org.bangkit.kiddos_android.databinding.FragmentHistoryBinding
import org.bangkit.kiddos_android.ui.adapter.HistoryAdapter
import org.bangkit.kiddos_android.ui.viewmodel.HistoryViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.HistoryViewModelFactory
import org.bangkit.kiddos_android.utils.NetworkUtils
import android.widget.Toast

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = HistoryRepository()
        val userPreference = UserPreference.getInstance(requireContext())
        val viewModelFactory = HistoryViewModelFactory(repository, userPreference)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(HistoryViewModel::class.java)

        historyViewModel.history.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada data riwayat", Toast.LENGTH_SHORT).show()
            }
            historyAdapter = HistoryAdapter(historyList) { historyItem ->
                historyViewModel.deleteHistory(historyItem.id)
            }
            binding.rvHistori.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = historyAdapter
            }

            binding.swipeRefreshLayout.isRefreshing = false
        }

        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                historyViewModel.fetchHistory()
            } else {
                Toast.makeText(requireContext(), "Tidak ada koneksi internet.", Toast.LENGTH_LONG).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            historyViewModel.fetchHistory()
        } else {
            Toast.makeText(requireContext(), "Tidak ada koneksi internet.", Toast.LENGTH_LONG).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


