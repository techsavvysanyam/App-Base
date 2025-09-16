package com.example.appbase.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    companion object {
        fun fromFirebaseUser(firebaseUser: com.google.firebase.auth.FirebaseUser): UserProfile {
            return UserProfile(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    fun isValid(): Boolean {
        return uid.isNotEmpty() && email.isNotEmpty()
    }
}
