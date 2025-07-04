package ee.cyber.wallet.di

import android.content.Context
import com.android.identity.securearea.software.SoftwareSecureArea
import com.android.identity.storage.EphemeralStorageEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.europa.ec.eudi.iso18013.transfer.TransferManager
import eu.europa.ec.eudi.iso18013.transfer.engagement.BleRetrievalMethod
import eu.europa.ec.eudi.iso18013.transfer.readerauth.ReaderTrustStore
import eu.europa.ec.eudi.wallet.document.DocumentManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ProximityModule {

    @Singleton
    @Provides
    fun providesReaderTrustStore(): ReaderTrustStore {
        return ReaderTrustStore.getDefault(
            trustedCertificates = listOf()
        )
    }

    @Singleton
    @Provides
    fun providesDocumentManager(): DocumentManager {
        val storageEngine = EphemeralStorageEngine()
        val secureArea = SoftwareSecureArea(storageEngine)
        return DocumentManager.Builder()
            .setIdentifier("eudi_wallet_document_manager")
            .setStorageEngine(storageEngine)
            .addSecureArea(secureArea).build()
    }

    @Singleton
    @Provides
    fun providesTransferManager(
        @ApplicationContext context: Context,
        documentManager: DocumentManager,
        readerTrustStore: ReaderTrustStore
    ): TransferManager {
        val transferManager = TransferManager.getDefault(
            context = context,
            documentManager = documentManager,
            retrievalMethods = listOf(
                BleRetrievalMethod(
                    peripheralServerMode = true,
                    centralClientMode = true,
                    clearBleCache = true
                )
            ),
            readerTrustStore = readerTrustStore
        )
        return transferManager
    }
}
