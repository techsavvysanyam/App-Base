package com.example.appbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController

class ObjectDetectionChoiceDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_object_detection_choice, container, false)

        val btnLiveDetection: Button = view.findViewById(R.id.btn_live_detection)
        val btnImageDetection: Button = view.findViewById(R.id.btn_image_detection)

        btnLiveDetection.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_objectDetectionLiveFragment)
            dismiss()
        }

        btnImageDetection.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_objectDetectionImageFragment)
            dismiss()
        }

        return view
    }
}
