package ee.cyber.wallet.util

import android.content.Context
import androidx.annotation.RawRes
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@JvmSynthetic
internal fun Context.getCertificates(
    @RawRes resId: Int
): List<X509Certificate> =
    resources.openRawResource(resId).use {
        CertificateFactory.getInstance("X509")
            .generateCertificates(it).map { cert -> cert as X509Certificate }
    }

internal class TrustAllX509TrustManager : X509TrustManager {
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}
