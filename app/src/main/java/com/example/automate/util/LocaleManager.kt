package com.example.automate.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    HEBREW("iw");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

object LocaleManager {

    fun currentLanguage(): AppLanguage {
        val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return AppLanguage.fromTag(tag.substringBefore(","))
    }

    /**
     * MainActivity is a plain ComponentActivity, not AppCompatActivity, so
     * AppCompatDelegate can't auto-recreate it when the locale changes.
     * Recreate manually so the running Compose tree recomposes with the new resources.
     */
    fun setLanguage(context: Context, language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        context.findActivity()?.recreate()
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
