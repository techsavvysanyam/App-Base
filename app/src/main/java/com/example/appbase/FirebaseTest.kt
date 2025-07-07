package com.example.appbase

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

object FirebaseTest {
    fun logTestEvent(analytics: FirebaseAnalytics) {
        val bundle = android.os.Bundle().apply {
            putString("test_key", "test_value")
        }
        analytics.logEvent("test_event", bundle)
        Log.d("FirebaseTest", "Logged test event to Firebase Analytics.")
    }

    fun dummySignIn(onResult: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseTest", "Anonymous sign-in successful.")
                    onResult(true)
                } else {
                    Log.e("FirebaseTest", "Anonymous sign-in failed.", task.exception)
                    onResult(false)
                }
            }
    }
} 