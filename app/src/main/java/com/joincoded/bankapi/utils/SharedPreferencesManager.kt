package com.joincoded.bankapi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPreferencesManager {
    private const val PREF_NAME = "BokiPrefs"
    private const val KEY_FIRST_NAME = "first_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserName(context: Context, name: String) {
        getPrefs(context).edit { putString(KEY_FIRST_NAME, name) }
    }

    fun getSavedUserName(context: Context): String {
        return getPrefs(context).getString(KEY_FIRST_NAME, "") ?: ""
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit { clear() }
    }
}