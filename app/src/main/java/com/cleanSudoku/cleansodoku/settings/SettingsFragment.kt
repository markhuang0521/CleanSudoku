package com.cleanSudoku.cleansodoku.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cleanSudoku.cleansodoku.R
import com.cleanSudoku.cleansodoku.utils.removeBottomNav
import com.cleanSudoku.cleansodoku.utils.setToolbarTitle


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

    private fun rateThisApp() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + requireContext().packageName)
                )
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Google Play not found!", Toast.LENGTH_SHORT).show()

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + requireContext().packageName)
                )
            )
        }

    }


    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            requireContext().getString(R.string.setting_dark_theme) -> toggleDarkTheme(newValue as Boolean)
            requireContext().getString(R.string.setting_rating) -> rateThisApp()
        }
        return true
    }


}