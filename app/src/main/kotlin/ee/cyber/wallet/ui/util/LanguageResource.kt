package ee.cyber.wallet.ui.util

import androidx.annotation.StringRes
import ee.cyber.wallet.R
import java.util.Locale

enum class LanguageResource(
    val tag: String,
    @StringRes val resId: Int,
    @StringRes val localizedResId: Int
) {
    EN("en", R.string.en, R.string.en_localized),
    ET("et", R.string.et, R.string.et_localized),
    RU("ru", R.string.ru, R.string.ru_localized);

    companion object {
        fun fromLocale(locale: Locale, default: LanguageResource = EN): LanguageResource = entries.find { it.tag == locale.language } ?: default
    }
}
