package ee.cyber.wallet.test

import ee.cyber.wallet.test.TestCryptoUtils.certificate
import ee.cyber.wallet.test.TestCryptoUtils.generateECKeyPair
import ee.cyber.wallet.test.TestCryptoUtils.privateKey
import io.ktor.util.toCharArray
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.Date

internal object TestCryptoUtils {

    fun createInMemoryJavKeyStore(pwd: String? = null): KeyStore {
        return KeyStore.getInstance("JKS")!!.apply {
            load(null, pwd?.toCharArray())
        }
    }

    fun generateECKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp521r1"))
        }.generateKeyPair()
    }

    fun KeyStore.privateKey(alias: String, pwd: String? = null): PrivateKey {
        return getKey(alias, pwd?.toCharArray()) as PrivateKey
    }

    fun KeyStore.certificate(alias: String): X509Certificate {
        return getCertificate(alias) as X509Certificate
    }
}

internal object TestCertificateUtils {

    private const val CERT_CN_SUBJECT = "C=EE, O=TEST, CN=%s"
    private const val SIGNATURE_ALGORITHM = "SHA512withECDSA"
    private const val EXPIRES_IN = 1 * 60 * 60 * 1000 // 1 hour

    fun KeyStore.issueCertificate(
        isCA: Boolean,
        cn: String,
        alias: String,
        pwd: String = "",
        caAlias: String? = null,
        caKeyPassword: String? = null,
        uniformResourceIdentifier: String? = null
    ) {
        val caCert = caAlias?.let { certificate(it) }
        val caKey = caAlias?.let { privateKey(it, caKeyPassword) }

        val keyPair = generateECKeyPair()
        val certificate = createCertificate(X500Name(CERT_CN_SUBJECT.format(cn)), keyPair, isCA, caKey, caCert, uniformResourceIdentifier)

        setCertificateEntry(alias, certificate)
        setKeyEntry(alias, keyPair.private, pwd.toCharArray(), arrayOf(certificate, caCert))
    }

    private fun createCertificate(
        subject: X500Name,
        keyPair: KeyPair,
        isCA: Boolean,
        caKey: PrivateKey? = null,
        caCertificate: X509Certificate? = null,
        uniformResourceIdentifier: String? = null
    ): X509Certificate {
        val serial = BigInteger(32, SecureRandom())
        val from = Date()
        val to = Date(System.currentTimeMillis() + EXPIRES_IN)
        val signer = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
            .build(caKey ?: keyPair.private)
        val issuer = caCertificate?.let { JcaX509CertificateHolder(it).subject } ?: subject
        val publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
        val certHolder = X509v3CertificateBuilder(issuer, serial, from, to, subject, publicKeyInfo).apply {
            addExtension(Extension.basicConstraints, false, BasicConstraints(isCA))
            addExtension(Extension.subjectKeyIdentifier, false, SubjectKeyIdentifier(publicKeyInfo.encoded))
            uniformResourceIdentifier?.also {
                addExtension(
                    Extension.subjectAlternativeName,
                    false,
                    GeneralNames(GeneralName(GeneralName.uniformResourceIdentifier, uniformResourceIdentifier))
                )
            }
            if (!isCA) {
                addExtension(
                    Extension.keyUsage,
                    false,
                    KeyUsage(KeyUsage.digitalSignature or KeyUsage.keyEncipherment or KeyUsage.nonRepudiation)
                )
                if (caCertificate != null) {
                    addExtension(
                        Extension.authorityKeyIdentifier,
                        false,
                        AuthorityKeyIdentifier(
                            GeneralNames(GeneralName(issuer)),
                            caCertificate.serialNumber
                        )
                    )
                }
            }
        }.build(signer)

        return JcaX509CertificateConverter().getCertificate(certHolder)
    }
}
