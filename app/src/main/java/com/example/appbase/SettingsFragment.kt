package com.example.appbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.appbase.databinding.FragmentSettingsBinding
import com.example.appbase.preferences.SettingsDataStore
import com.example.appbase.utils.LanguageHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var settingsDataStore: SettingsDataStore
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        settingsDataStore = SettingsDataStore(requireContext())
        setupUI()
        observeSettings()
    }
    
    private fun setupUI() {
        // Set up notification toggles
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDataStore.setNotificationsEnabled(isChecked)
                updateNotificationTogglesState(isChecked)
            }
        }
        
        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDataStore.setPushNotificationsEnabled(isChecked)
            }
        }
        
        binding.switchEmailNotifications.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDataStore.setEmailNotificationsEnabled(isChecked)
            }
        }
        
        // Set up theme toggles
        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDataStore.setDarkThemeEnabled(isChecked)
                // Only apply theme if auto theme is disabled
                val autoThemeEnabled = settingsDataStore.autoThemeEnabled.first()
                if (!autoThemeEnabled) {
                    applyTheme(isChecked)
                }
            }
        }
        
        binding.switchAutoTheme.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDataStore.setAutoThemeEnabled(isChecked)
                if (isChecked) {
                    // Use system theme when auto theme is enabled
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    // Apply manual theme when auto theme is disabled
                    val darkThemeEnabled = settingsDataStore.darkThemeEnabled.first()
                    applyTheme(darkThemeEnabled)
                }
            }
        }
        
        // Set up language selection
        binding.languageSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val languages = resources.getStringArray(R.array.language_options)
                val languageCodes = resources.getStringArray(R.array.language_codes)
                if (position < languageCodes.size) {
                    lifecycleScope.launch {
                        val selectedLanguageCode = languageCodes[position]
                        settingsDataStore.setSelectedLanguage(selectedLanguageCode)
                        
                        // Apply language immediately for better UX
                        context?.let { ctx ->
                            LanguageHelper.applyLanguage(ctx, selectedLanguageCode)
                        }
                        
                        Toast.makeText(context, "Language changed to ${languages[position]}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun observeSettings() {
        // Observe notification settings
        lifecycleScope.launch {
            settingsDataStore.notificationsEnabled.collect { enabled ->
                binding.switchNotifications.isChecked = enabled
                updateNotificationTogglesState(enabled)
            }
        }
        
        lifecycleScope.launch {
            settingsDataStore.pushNotificationsEnabled.collect { enabled ->
                binding.switchPushNotifications.isChecked = enabled
            }
        }
        
        lifecycleScope.launch {
            settingsDataStore.emailNotificationsEnabled.collect { enabled ->
                binding.switchEmailNotifications.isChecked = enabled
            }
        }
        
        // Observe theme settings
        lifecycleScope.launch {
            settingsDataStore.darkThemeEnabled.collect { enabled ->
                binding.switchDarkTheme.isChecked = enabled
            }
        }
        
        lifecycleScope.launch {
            settingsDataStore.autoThemeEnabled.collect { enabled ->
                binding.switchAutoTheme.isChecked = enabled
            }
        }
        
        // Observe language setting
        lifecycleScope.launch {
            settingsDataStore.selectedLanguage.collect { language ->
                val languageCodes = resources.getStringArray(R.array.language_codes)
                val position = languageCodes.indexOf(language)
                if (position >= 0) {
                    binding.languageSpinner.setSelection(position)
                }
            }
        }
    }
    
    private fun updateNotificationTogglesState(masterEnabled: Boolean) {
        binding.switchPushNotifications.isEnabled = masterEnabled
        binding.switchEmailNotifications.isEnabled = masterEnabled
        
        if (!masterEnabled) {
            binding.switchPushNotifications.isChecked = false
            binding.switchEmailNotifications.isChecked = false
            lifecycleScope.launch {
                settingsDataStore.setPushNotificationsEnabled(false)
                settingsDataStore.setEmailNotificationsEnabled(false)
            }
        }
    }
    
    private fun applyTheme(isDarkTheme: Boolean) {
        val nightMode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
