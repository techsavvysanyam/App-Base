package com.example.appbase.repository

import android.net.Uri
import com.example.appbase.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val usersCollection = firestore.collection("users")
    private val profileImagesRef = storage.reference.child("profile_images")

    suspend fun getCurrentUserProfile(): UserProfile? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val document = usersCollection.document(currentUser.uid).get().await()
            if (document.exists()) {
                document.toObject(UserProfile::class.java)
            } else {
                // Create profile from Firebase Auth user if not exists in Firestore
                val profile = UserProfile.fromFirebaseUser(currentUser)
                saveUserProfile(profile)
                profile
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            val updatedProfile = profile.copy(updatedAt = System.currentTimeMillis())
            usersCollection.document(profile.uid).set(updatedProfile.toMap()).await()
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val imageRef = profileImagesRef.child("${currentUser.uid}_${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImage(imageUrl: String): Result<UserProfile> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val currentProfile = getCurrentUserProfile() ?: return Result.failure(Exception("Profile not found"))
            val updatedProfile = currentProfile.copy(photoUrl = imageUrl, updatedAt = System.currentTimeMillis())
            saveUserProfile(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileField(field: String, value: String): Result<UserProfile> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val currentProfile = getCurrentUserProfile() ?: return Result.failure(Exception("Profile not found"))
            val updatedProfile = when (field) {
                "displayName" -> currentProfile.copy(displayName = value, updatedAt = System.currentTimeMillis())
                else -> return Result.failure(Exception("Invalid field: $field"))
            }
            saveUserProfile(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWithGoogleAccount(): Result<UserProfile> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val googleProfile = UserProfile.fromFirebaseUser(currentUser)
            val existingProfile = getCurrentUserProfile()

            val mergedProfile = existingProfile?.let { existing ->
                existing.copy(
                    displayName = googleProfile.displayName.ifEmpty { existing.displayName },
                    email = googleProfile.email.ifEmpty { existing.email },
                    photoUrl = googleProfile.photoUrl.ifEmpty { existing.photoUrl },
                    updatedAt = System.currentTimeMillis()
                )
            } ?: googleProfile

            saveUserProfile(mergedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
