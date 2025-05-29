package ee.cyber.wallet.ui.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.CharacterSetECI
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumMap

object QRCode {

    suspend fun createQrCode(content: String, size: Int = 600): Bitmap = withContext(Dispatchers.IO) {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = CharacterSetECI.UTF8
        val bitMapMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        for (i in 0 until size) {
            for (j in 0 until size) {
                bitmap.setPixel(i, j, if (bitMapMatrix[i, j]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    }
}
