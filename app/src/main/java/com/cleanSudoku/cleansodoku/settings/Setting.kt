package com.cleanSudoku.cleansodoku.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cleanSudoku.cleansodoku.R

class Setting(private val context: Context) {
    private val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)
    private val preferenceEditor: SharedPreferences.Editor
        get() = preferences.edit()

    var timer: Boolean
        get() = preferences.getBoolean(context.getString(R.string.setting_timer), true)
        set(enabled) = preferenceEditor.putBoolean(
            context.getString(R.string.setting_timer),
            enabled
        ).apply()
    var sound: Boolean
        get() = preferences.getBoolean(context.getString(R.string.setting_sound), true)
        set(enabled) = preferenceEditor.putBoolean(
            context.getString(R.string.setting_sound),
            enabled
        ).apply()
    var vibration: Boolean
        get() = preferences.getBoolean(context.getString(R.string.setting_vibration), true)
        set(enabled) = preferenceEditor.putBoolean(
            context.getString(R.string.setting_sound),
            enabled
        ).apply()
    var darkMode: Boolean
        get() = preferences.getBoolean(context.getString(R.string.setting_dark_theme), false)
        set(enabled) = preferenceEditor.putBoolean(
            context.getString(R.string.setting_dark_theme),
            enabled
        ).apply()


}