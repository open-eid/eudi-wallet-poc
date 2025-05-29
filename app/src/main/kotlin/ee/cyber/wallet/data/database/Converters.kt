package ee.cyber.wallet.data.database

import androidx.room.TypeConverter
import ee.cyber.wallet.domain.AppError
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class InstantConverter {
    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}

internal class SafeAppErrorConverter {
    @TypeConverter
    fun stringToAppError(value: String?): AppError? = value?.let { AppError.entries.find { it.name == value } ?: AppError.UNKNOWN_ERROR }

    @TypeConverter
    fun appErrorToString(appError: AppError?): String? = appError?.name
}

internal class JsonConverter {

    @TypeConverter
    fun jsonToString(data: JsonObject?): String? = data?.toString()

    @TypeConverter
    fun stringToJson(json: String?): JsonObject? = json?.let { Json.parseToJsonElement(it).jsonObject }
}
