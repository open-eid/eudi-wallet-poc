package ee.cyber.wallet.util

import com.nimbusds.jose.util.Base64URL
import java.security.MessageDigest
import java.util.Base64

fun ByteArray.sha256(): ByteArray = MessageDigest.getInstance("SHA-256").digest(this)
fun ByteArray.toBase64String(): String = Base64.getEncoder().encodeToString(this)
fun String.decodeBase64String(): ByteArray = Base64.getDecoder().decode(this)
fun ByteArray.toBase64UrlString(): String = Base64URL.encode(this).toString()
