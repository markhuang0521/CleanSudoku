package com.example.cleansodoku.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.cleansodoku.R
import com.example.cleansodoku.utils.removeBottomNav
import com.example.cleansodoku.utils.setToolbarTitle


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        setListeners()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setToolbarTitle("Setting")
        removeBottomNav()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    private fun setListeners() {
        listOf(
            requireContext().getString(R.string.setting_dark_theme),
            requireContext().getString(R.string.setting_sound),
            requireContext().getString(R.string.setting_vibration),
            requireContext().getString(R.string.setting_timer)
        )
            .forEach { findPreference<Preference>(it)?.onPreferenceChangeListener = this }
    }

    private fun toggleDarkTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO

        )
    }


    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            requireContext().getString(R.string.setting_dark_theme) -> toggleDarkTheme(newValue as Boolean)
        }
        return true
    }


}