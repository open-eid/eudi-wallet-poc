package ee.cyber.wallet.security

import org.slf4j.LoggerFactory
import java.security.cert.CertPathBuilder
import java.security.cert.CertStore
import java.security.cert.CollectionCertStoreParameters
import java.security.cert.PKIXBuilderParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate

/**
 * Validates an X.509 certificate chain.
 *
 * @param certificateChain The certificate chain with the leaf certificate as the first element.
 * @param trustedRootCertificates A list of trusted root certificates (must not be empty).
 * @param enableRevocationCheck Flag to enable or disable certificate revocation checking.
 * @return `true` if the certificate chain is valid, `false` otherwise.
 * @throws IllegalArgumentException if certificateChain or trustedRootCertificates is empty.
 */
object CertificateChainValidator {

    private val logger = LoggerFactory.getLogger(CertificateChainValidator::class.java)

    fun validateCertificateChain(certificateChain: List<X509Certificate>, trustedRootCertificates: List<X509Certificate>, enableRevocationCheck: Boolean = true): Boolean {
        return runCatching {
            require(certificateChain.isNotEmpty()) { "Certificate chain cannot be empty" }
            require(trustedRootCertificates.isNotEmpty()) { "Trusted root certificates cannot be empty" }
            val pkixParams = PKIXBuilderParameters(
                trustedRootCertificates.map { TrustAnchor(it, null) }.toSet(),
                X509CertSelector().apply { certificate = certificateChain.first() }
            ).apply {
                isRevocationEnabled = enableRevocationCheck
                addCertStore(
                    CertStore.getInstance(
                        "Collection",
                        CollectionCertStoreParameters(certificateChain)
                    )
                )
            }
            CertPathBuilder.getInstance("PKIX").build(pkixParams)
        }.onFailure {
            logger.error("Failed to validate certificate chain", it)
        }.isSuccess
    }
}
