@file:Suppress("DEPRECATION")

package com.example.appbase

import android.annotation.SuppressLint
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
import com.bumptech.glide.Glide
import com.example.appbase.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

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
        binding.buttonAnonymousSignIn.setOnClickListener {
            val analytics = FirebaseAnalytics.getInstance(requireContext())
            FirebaseTest.logTestEvent(analytics)
            FirebaseTest.dummySignIn { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Dummy sign-in successful!", Toast.LENGTH_SHORT).show()
                    updateUI(auth.currentUser)
                } else {
                    Toast.makeText(requireContext(), "Dummy sign-in failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            signIn()
        }

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }

        binding.buttonGoToMainMenu.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_mainMenuFragment)
        }

        // Update user info display
        updateUI(auth.currentUser)

        return binding.root
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                    showSuccess("Sign-in successful!")
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

    @SuppressLint("SetTextI18n")
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in
            binding.buttonGoogleSignIn.visibility = View.GONE
            binding.cardUserInfoDetails.visibility = View.VISIBLE
            binding.textUserInfo.text = "Signed in as: ${user.displayName ?: "Anonymous"}"

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

        } else {
            // User is signed out
            binding.buttonGoogleSignIn.visibility = View.VISIBLE
            binding.cardUserInfoDetails.visibility = View.GONE
            binding.textUserInfo.text = "Not signed in"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateUI(auth.currentUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
