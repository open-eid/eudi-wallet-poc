package ee.cyber.wallet.domain

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import ee.cyber.wallet.ui.util.LanguageResource
import java.util.Locale

class AndroidLocaleManager(
    supportedLanguages: List<LanguageResource>,
    private val context: Context
) {

    val supportedLocales = supportedLanguages.map { Locale(it.tag) }.takeIf { it.isNotEmpty() } ?: listOf(Locale("en"))

    fun getApplicationLocale(): Locale {
        val applicationLocales = AppCompatDelegate.getApplicationLocales()
        return applicationLocales
            .takeIf { it.size() > 0 }?.get(0)
            ?: supportedLocales.find { it.language == Locale.getDefault().language }
            ?: supportedLocales.first()
    }

    fun setApplicationLocale(language: String) {
        val appLocale = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
