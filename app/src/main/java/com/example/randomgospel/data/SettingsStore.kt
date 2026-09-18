package com.example.randomgospel.data

import android.content.Context
import androidx.core.content.edit

/**
 * Persists the user's preferred translation code (NIV, AVD, …) and
 * translation/version ID (111, 13, …) between app launches.
 */
object SettingsStore {
    private const val PREFS = "random_gospel_prefs"
    private const val KEY_TRANSLATION_CODE = "translation_code"
    private const val KEY_VERSION_ID = "version_id"

    const val DEFAULT_TRANSLATION_CODE = "NIV"
    const val DEFAULT_VERSION_ID = "111"

    fun getTranslationCode(context: Context): String =
        prefs(context).getString(KEY_TRANSLATION_CODE, DEFAULT_TRANSLATION_CODE)
            ?: DEFAULT_TRANSLATION_CODE

    fun getVersionId(context: Context): String =
        prefs(context).getString(KEY_VERSION_ID, DEFAULT_VERSION_ID)
            ?: DEFAULT_VERSION_ID

    fun save(context: Context, translationCode: String, versionId: String) {
        prefs(context).edit {
            putString(KEY_TRANSLATION_CODE, translationCode)
            putString(KEY_VERSION_ID, versionId)
        }
    }

    fun reset(context: Context) {
        prefs(context).edit {
            putString(KEY_TRANSLATION_CODE, DEFAULT_TRANSLATION_CODE)
            putString(KEY_VERSION_ID, DEFAULT_VERSION_ID)
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
