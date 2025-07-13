package com.example.appbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appbase.databinding.FragmentHomeBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Set up click listeners
        binding.buttonAnonymousSignIn.setOnClickListener {
            val analytics = FirebaseAnalytics.getInstance(requireContext())
            FirebaseTest.logTestEvent(analytics)
            FirebaseTest.dummySignIn { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Dummy sign-in successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Dummy sign-in failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonGoToSignIn.setOnClickListener {
            findNavController().navigate(R.id.signInFragment)
        }

        // Update user info display
        updateUserInfo()

        return binding.root
    }

    private fun updateUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userInfo = "Name: ${currentUser.displayName ?: "Unknown"}\n" +
                    "Email: ${currentUser.email ?: "No email"}\n" +
                    "Provider: ${currentUser.providerData.firstOrNull()?.providerId ?: "Unknown"}"
            binding.textUserInfo.text = userInfo
        } else {
            binding.textUserInfo.text = "Not signed in"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}