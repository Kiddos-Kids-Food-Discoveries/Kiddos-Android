package org.bangkit.kiddos_android.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.datastore.preferences.core.Preferences
import com.google.android.material.textview.MaterialTextView
import org.bangkit.kiddos_android.data.preferences.UserPreference
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var userPreference: UserPreference
    private lateinit var helloNameTextView: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)


        userPreference = UserPreference.getInstance(requireContext())

        helloNameTextView = binding.HelloName

        lifecycleScope.launch {
            userPreference.getName().collect { userName ->
                Log.d("UserPreference", "Collected name: $userName")
                helloNameTextView.text = "Hello, $userName"
            }
        }

        return binding.root
    }
}
