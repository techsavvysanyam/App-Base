package com.example.appbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appbase.databinding.FragmentHomeBinding
import com.google.firebase.analytics.FirebaseAnalytics

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //For direct call after the app launch
//        FirebaseTest.dummySignIn { success ->
//            if (success) {
//                Toast.makeText(context, "Dummy sign-in successful!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context, "Dummy sign-in failed.", Toast.LENGTH_SHORT).show()

        // Set up click listener for anonymous sign in button..
        binding.buttonAnonymousSignIn.setOnClickListener {
            val analytics = FirebaseAnalytics.getInstance(requireContext())
            FirebaseTest.logTestEvent(analytics)
            FirebaseTest.dummySignIn { success ->
                if (success) {
                    android.widget.Toast.makeText(requireContext(), "Dummy sign-in successful!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Dummy sign-in failed!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


