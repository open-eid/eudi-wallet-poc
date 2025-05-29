package ee.cyber.wallet.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Base64

@Composable
fun rememberBase64DecodedBitmap(base64Image: String): ImageBitmap? {
    var decodedImage by remember(base64Image) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(decodedImage) {
        if (decodedImage != null) return@LaunchedEffect
        launch(Dispatchers.Default) {
            decodedImage = try {
                val decodedImageByteArray: ByteArray = Base64.getDecoder().decode(base64Image)
                BitmapFactory.decodeByteArray(decodedImageByteArray, 0, decodedImageByteArray.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    return remember(decodedImage) {
        decodedImage?.asImageBitmap()
    }
}
