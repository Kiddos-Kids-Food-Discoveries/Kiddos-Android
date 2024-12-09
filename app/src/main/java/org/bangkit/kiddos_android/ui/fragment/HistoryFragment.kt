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
            historyAdapter = HistoryAdapter(historyList) { historyItem ->
                historyViewModel.deleteHistory(historyItem.id)
            }
            binding.rvHistori.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = historyAdapter
            }
        }

        historyViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        historyViewModel.fetchHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
