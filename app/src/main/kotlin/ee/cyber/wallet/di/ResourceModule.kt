package ee.cyber.wallet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.domain.AndroidLocaleManager
import ee.cyber.wallet.ui.util.LanguageResource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ResourceModule {

    @Singleton
    @Provides
    fun providesAndroidLocaleManager(
        @ApplicationContext context: Context
    ) = AndroidLocaleManager(
        supportedLanguages = listOf(LanguageResource.EN, LanguageResource.ET),
        context = context
    )
}
