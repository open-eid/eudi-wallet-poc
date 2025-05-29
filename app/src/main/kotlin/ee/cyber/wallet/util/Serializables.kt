package ee.cyber.wallet.util

import android.util.Base64
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

// TODO: Serialization of AuthorizationRequestPrepared fails for
//  eu.europa.ec.eudi:eudi-lib-jvm-openid4vci-kt:0.6.0 due to
//  eu.europa.ec.eudi.openid4vci.CredentialConfigurationIdentifier
//  and eu.europa.ec.eudi.openid4vci.CredentialIdentifier not
//  implementing java.io.Serializable; As a workaround, we serialize
//  using Jackson and Base64 encoding.
fun <T : Serializable> T.serializeToBase64(): String =
    ByteArrayOutputStream().use {
        ObjectOutputStream(it).writeObject(this)
        it.toByteArray()
    }.let { Base64.encodeToString(it, Base64.NO_WRAP) }

inline fun <reified T : Serializable> String.deserializeFromBase64(): T =
    ByteArrayInputStream(Base64.decode(this, Base64.NO_WRAP)).use {
        ObjectInputStream(it).readObject() as T
    }

val mapper = jacksonObjectMapper()

fun <T> T.toBase64Json(): String {
    val jsonString = mapper.writeValueAsString(this)
    return Base64.encodeToString(jsonString.toByteArray(), Base64.NO_WRAP)
}

inline fun <reified T> String.fromBase64Json(): T {
    val jsonString = Base64.decode(this, Base64.NO_WRAP).toString(Charsets.UTF_8)
    return mapper.readValue(jsonString, T::class.java)
}
