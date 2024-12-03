package org.bangkit.kiddos_android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.bangkit.kiddos_android.databinding.FragmentHistoriBinding
import org.bangkit.kiddos_android.viewmodel.HistoriViewModel

class HistoriFragment : Fragment() {

    private var _binding: FragmentHistoriBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val historiViewModel =
            ViewModelProvider(this).get(HistoriViewModel::class.java)

        _binding = FragmentHistoriBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}