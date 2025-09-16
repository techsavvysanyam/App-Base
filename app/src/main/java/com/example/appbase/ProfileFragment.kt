package com.example.appbase

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.appbase.databinding.FragmentProfileBinding
import com.example.appbase.models.UserProfile
import com.example.appbase.repository.ProfileRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileRepository: ProfileRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var currentProfile: UserProfile? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadProfileImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFirebase()
        setupUI()
        loadUserProfile()
    }

    private fun initializeFirebase() {
        profileRepository = ProfileRepository()
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupUI() {
        // Set up button click listeners
        binding.buttonChangePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.buttonSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.buttonSyncGoogle.setOnClickListener {
            syncWithGoogle()
        }

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val profile = profileRepository.getCurrentUserProfile()
                if (profile != null) {
                    currentProfile = profile
                    displayProfile(profile)
                } else {
                    showError(getString(R.string.profile_load_error))
                }
            } catch (e: Exception) {
                showError("${getString(R.string.profile_load_error)}: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayProfile(profile: UserProfile) {
        binding.textUserName.text = profile.displayName.ifEmpty { "Unknown User" }
        binding.textUserEmail.text = profile.email.ifEmpty { "No email" }

        // Load profile image
        if (profile.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profile.photoUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(binding.imageProfilePicture)
        } else {
            binding.imageProfilePicture.setImageResource(R.drawable.ic_person_placeholder)
        }

        // Fill form fields
        binding.editTextDisplayName.setText(profile.displayName)
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun uploadProfileImage(uri: Uri) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = profileRepository.uploadProfileImage(uri)
                result.fold(
                    onSuccess = { imageUrl ->
                        updateProfileImage(imageUrl)
                    },
                    onFailure = { error ->
                        showError("${getString(R.string.profile_upload_error)}: ${error.message}")
                        showLoading(false)
                    }
                )
            } catch (e: Exception) {
                showError("${getString(R.string.profile_upload_error)}: ${e.message}")
                showLoading(false)
            }
        }
    }

    private fun updateProfileImage(imageUrl: String) {
        lifecycleScope.launch {
            try {
                val result = profileRepository.updateProfileImage(imageUrl)
                result.fold(
                    onSuccess = { profile ->
                        currentProfile = profile
                        displayProfile(profile)
                        showSuccess(getString(R.string.profile_picture_updated))
                    },
                    onFailure = { error ->
                        showError("${getString(R.string.profile_save_error)}: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                showError("${getString(R.string.profile_save_error)}: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveProfile() {
        val profile = currentProfile ?: return

        val updatedProfile = profile.copy(
            displayName = binding.editTextDisplayName.text.toString().trim(),
            updatedAt = System.currentTimeMillis()
        )

        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = profileRepository.saveUserProfile(updatedProfile)
                result.fold(
                    onSuccess = { savedProfile ->
                        currentProfile = savedProfile
                        showSuccess(getString(R.string.profile_saved))
                    },
                    onFailure = { error ->
                        showError("${getString(R.string.profile_save_error)}: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                showError("${getString(R.string.profile_save_error)}: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun syncWithGoogle() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = profileRepository.syncWithGoogleAccount()
                result.fold(
                    onSuccess = { profile ->
                        currentProfile = profile
                        displayProfile(profile)
                        showSuccess(getString(R.string.profile_sync_success))
                    },
                    onFailure = { error ->
                        showError("${getString(R.string.profile_sync_error)}: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                showError("${getString(R.string.profile_sync_error)}: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun signOut() {
        showLoading(true)

        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            showLoading(false)
            showSuccess(getString(R.string.sign_out_success))
            findNavController().navigate(R.id.action_profile_to_signIn)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonSaveProfile.isEnabled = !show
        binding.buttonSyncGoogle.isEnabled = !show
        binding.buttonSignOut.isEnabled = !show
        binding.buttonChangePhoto.isEnabled = !show
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
