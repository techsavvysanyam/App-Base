package com.example.appbase

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.appbase.databinding.FragmentSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.bumptech.glide.Glide
import com.google.android.gms.common.GoogleApiAvailability

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Set up activity result launcher for Google Sign-In
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    showError("Google Sign-In failed: ${e.message}")
                }
            } else {
                showError("Google Sign-In was cancelled or failed")
            }
        }

        // Set up click listeners
        binding.buttonGoogleSignIn.setOnClickListener {
            signIn()
        }

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }

        // Check if user is already signed in
        updateUI(auth.currentUser)

        return binding.root
    }

    private fun signIn() {
        // Check if Google Play Services is available
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())

        if (resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS) {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        } else {
            showError("Google Play Services not available. Please update Google Play Services.")
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                    showSuccess("Sign-in successful!")

                    // Navigate to home after successful sign-in
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    showError("Authentication failed: ${task.exception?.message}")
                    updateUI(null)
                }
            }
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            updateUI(null)
            showSuccess("Signed out successfully!")
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in
            binding.buttonGoogleSignIn.visibility = View.GONE
            binding.cardUserInfo.visibility = View.VISIBLE

            // Update user info
            binding.textUserName.text = user.displayName ?: "Unknown User"
            binding.textUserEmail.text = user.email ?: "No email"

            // Load profile image
            user.photoUrl?.let { photoUrl ->
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(binding.imageProfile)
            } ?: run {
                binding.imageProfile.setImageResource(R.drawable.ic_person_placeholder)
            }

            hideError()
        } else {
            // User is signed out
            binding.buttonGoogleSignIn.visibility = View.VISIBLE
            binding.cardUserInfo.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.textError.text = message
        binding.textError.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun hideError() {
        binding.textError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 