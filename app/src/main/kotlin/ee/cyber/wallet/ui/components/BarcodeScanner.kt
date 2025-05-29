package ee.cyber.wallet.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import ee.cyber.wallet.R
import ee.cyber.wallet.ui.theme.PreviewThemes
import ee.cyber.wallet.ui.theme.WalletThemePreviewSurface
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
@PreviewThemes
fun BarcodeScannerPreview() {
    WalletThemePreviewSurface {
        BarcodeScanner(onBarcodeDetected = {})
    }
}

private val logger = LoggerFactory.getLogger("BarcodeScanner")

@Composable
fun BarcodeScanner(modifier: Modifier = Modifier, onBarcodeDetected: (String) -> Unit) {
    var barcode by remember {
        mutableStateOf<String?>(null)
    }
    val barcodeScannerUseCase by remember {
        derivedStateOf {
            BarcodeScannerCameraUseCase.create {
                if (it != barcode) {
                    barcode = it
                    onBarcodeDetected(it)
                }
            }
        }
    }

    val inPreview = LocalInspectionMode.current

    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!inPreview) {
            CameraPreview(
                modifier = modifier.fillMaxSize(),
                useCases = arrayOf(barcodeScannerUseCase)
            )
        } else {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.scrim)
                    .fillMaxSize()
            )
        }
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.barcode_scanner_overlay),
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    useCases: Array<UseCase> = arrayOf()
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // CameraX Preview UseCase
            val previewUseCase = Preview.Builder()
                .build()
                .apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

            coroutineScope.launch {
                val cameraProvider = context.getCameraProvider()
                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase, *useCases)
                } catch (ex: Exception) {
                    logger.error("Use case binding failed", ex)
                }
            }

            previewView
        }
    )
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

object BarcodeScannerCameraUseCase {
    fun create(onBarcodeDetected: (String) -> Unit): ImageAnalysis {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        return ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(ResolutionStrategy(Size(1440, 1920), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER))
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(Executors.newSingleThreadExecutor()) { processImage(scanner, it, onBarcodeDetected) }
            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImage(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy,
        onBarcodeDetected: (String) -> Unit
    ) {
        imageProxy.image?.let { image ->
            InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees).apply {
                barcodeScanner.process(this)
                    .addOnSuccessListener { barcodeList ->
                        barcodeList.getOrNull(0)?.rawValue?.also { onBarcodeDetected(it) }
                    }
                    .addOnFailureListener {
                        logger.error(it.message.orEmpty())
                    }
                    .addOnCompleteListener {
                        imageProxy.image?.close()
                        imageProxy.close()
                    }
            }
        }
    }
}
