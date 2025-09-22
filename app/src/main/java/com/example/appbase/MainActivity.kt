package com.example.appbase

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.appbase.databinding.ActivityMainBinding
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.appbase.preferences.SettingsDataStore
import com.example.appbase.utils.LanguageHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsDataStore: SettingsDataStore

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize settings data store
        settingsDataStore = SettingsDataStore(this)
        applySettings()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
        // Initialize FCM token (removed logging for security)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token)
            }
        }
        // Register receiver for FCM messages
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                fcmReceiver,
                IntentFilter(MyFirebaseMessagingService.ACTION_FCM_MESSAGE),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(fcmReceiver, IntentFilter(MyFirebaseMessagingService.ACTION_FCM_MESSAGE))
        }
        requestNotificationPermissionIfNeeded()
    }

    private val fcmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra(MyFirebaseMessagingService.EXTRA_TITLE) ?: "FCM"
            val message = intent?.getStringExtra(MyFirebaseMessagingService.EXTRA_MESSAGE) ?: ""
            Toast.makeText(this@MainActivity, "$title: $message", Toast.LENGTH_LONG).show()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
        } else {
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(fcmReceiver)
    }

    private fun applySettings() {
        lifecycleScope.launch {
            // Apply language settings
            val selectedLanguage = settingsDataStore.selectedLanguage.first()
            if (selectedLanguage != "en") {
                LanguageHelper.applyLanguage(this@MainActivity, selectedLanguage)
            }
            
            // Apply theme settings
            settingsDataStore.autoThemeEnabled.collect { autoThemeEnabled ->
                if (autoThemeEnabled) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    // Only apply dark theme when auto theme is disabled
                    val darkThemeEnabled = settingsDataStore.darkThemeEnabled.first()
                    val nightMode = if (darkThemeEnabled) {
                        AppCompatDelegate.MODE_NIGHT_YES
                    } else {
                        AppCompatDelegate.MODE_NIGHT_NO
                    }
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
