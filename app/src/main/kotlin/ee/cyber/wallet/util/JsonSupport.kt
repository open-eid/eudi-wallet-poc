package ee.cyber.wallet.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import java.time.Instant

object JsonSupport {
    val json by lazy { buildWithDefaults() }
    val prettyJson by lazy { buildWithDefaults { prettyPrint = true } }

    private fun buildWithDefaults(builderBlock: JsonBuilder.() -> Unit = {}) = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
        this.builderBlock()
    }

    inline fun <reified T> T.toJson(): String = json.encodeToString(this)
    inline fun <reified T> T.toPrettyJson(): String = prettyJson.encodeToString(this)
    inline fun <reified T> String.fromJson(): T = json.decodeFromString(this)
}

object InstantEpochSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.epochSecond)
    override fun deserialize(decoder: Decoder): Instant = Instant.ofEpochSecond(decoder.decodeLong())
}
