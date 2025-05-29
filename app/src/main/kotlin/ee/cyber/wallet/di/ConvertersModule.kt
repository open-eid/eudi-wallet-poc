package ee.cyber.wallet.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.cyber.wallet.R
import ee.cyber.wallet.domain.documents.CredentialToDocumentMapper
import ee.cyber.wallet.util.getCertificates
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConvertersModule {

    @Singleton
    @Provides
    fun providesCredentialToDocumentConverter(
        @ApplicationContext context: Context
    ) = CredentialToDocumentMapper(context.getCertificates(R.raw.trusted))
}
