package com.joincoded.bankapi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPreferencesManager {
    private const val PREF_NAME = "BokiPrefs"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_LAST_LOGGED_IN = "lastLoggedIn"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Original user profile functions
    fun saveUserName(context: Context, name: String) {
        getPrefs(context).edit { putString(KEY_FULL_NAME, name) }
    }

    fun getSavedUserName(context: Context): String {
        return getPrefs(context).getString(KEY_FULL_NAME, "") ?: ""
    }

    // to get first name for greeting
    fun getFirstName(context: Context): String {
        val fullName = getSavedUserName(context)
        return if (fullName.isNotEmpty()) {
            fullName.trim().split(" ").firstOrNull() ?: fullName
        } else {
            ""
        }
    }

    // to check if user exists (for smart routing)
    fun hasExistingUser(context: Context): Boolean {
        return getSavedUserName(context).isNotEmpty()
    }

    // to save last logged in username
    fun saveLastUsername(context: Context, username: String) {
        getPrefs(context).edit { putString(KEY_LAST_LOGGED_IN, username) }
    }

    fun getLastUsername(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_LOGGED_IN, "") ?: ""
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit { clear() }
    }
}