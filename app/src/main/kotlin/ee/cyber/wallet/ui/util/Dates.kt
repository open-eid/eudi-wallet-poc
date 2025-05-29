package ee.cyber.wallet.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

const val DATE_FORMAT = "dd.MM.yyyy HH:mm"

fun Instant.format(): String {
    return DateTimeFormatter.ofPattern(DATE_FORMAT)
        .format(toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
}
