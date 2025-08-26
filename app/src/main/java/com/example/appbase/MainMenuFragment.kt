package com.example.appbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appbase.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)

        // Set up click listeners for each feature layout
        binding.layoutFaceRecognition.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_faceRecognitionFragment)
        }

        binding.layoutObjectDetection.setOnClickListener {
            val dialog = ObjectDetectionChoiceDialogFragment()
            dialog.show(childFragmentManager, "ObjectDetectionChoiceDialogFragment")
        }

        binding.layoutOcr.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_ocrFragment)
        }

        binding.layoutNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_navigationFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
