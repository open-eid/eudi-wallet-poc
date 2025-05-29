package ee.cyber.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

@HiltAndroidApp
class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupBouncyCastle()
    }

    private fun setupBouncyCastle() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())
    }
}
