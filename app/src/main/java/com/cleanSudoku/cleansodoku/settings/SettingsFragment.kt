package com.cleanSudoku.cleansodoku.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cleanSudoku.cleansodoku.R
import com.cleanSudoku.cleansodoku.util.removeBottomNav
import com.cleanSudoku.cleansodoku.util.setToolbarTitle
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber


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
//        switch Preferences
        listOf(
            requireContext().getString(R.string.setting_dark_theme),
            requireContext().getString(R.string.setting_sound),
            requireContext().getString(R.string.setting_vibration),
            requireContext().getString(R.string.setting_timer)

        )
            .forEach { findPreference<Preference>(it)?.onPreferenceChangeListener = this }

//        onclick Preference
        val preference =
            findPreference<Preference>(requireContext().getString(R.string.setting_rating))
        preference?.setOnPreferenceClickListener {
            rateThisApp()
            true
        }
    }

    private fun toggleDarkTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO

        )
    }

    private fun rateThisApp() {

        Toast.makeText(requireContext(), "rating", Toast.LENGTH_SHORT).show()
        val manager = ReviewManagerFactory.create(requireContext())

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            } else {
                // There was some problem, log or handle the error code.
                Timber.d(task.exception.toString())
            }
        }

//        try {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("market://details?id=" + requireContext().packageName)
//                )
//            )
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(requireContext(), "Google Play not found!", Toast.LENGTH_SHORT).show()
//
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("http://play.google.com/store/apps/details?id=" + requireContext().packageName)
//                )
//            )
//        }

    }


    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            requireContext().getString(R.string.setting_dark_theme) -> toggleDarkTheme(newValue as Boolean)

        }
        return true
    }


}