package com.example.ccscan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 扫码Activity - 使用CameraX + ML Kit Barcode Scanning
 */
class ScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var barcodeScanner: BarcodeScanner? = null
    private var isScanning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        previewView = findViewById(R.id.previewView)

        // 初始化相机执行器
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 初始化条码扫描器
        barcodeScanner = BarcodeScanning.getClient()

        // 请求相机权限（应该已经在调用前请求过）
        startCamera()
    }

    /**
     * 启动相机预览
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 预览用例
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 图像分析用例 - 用于条码识别
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        if (isScanning) {
                            isScanning = false
                            handleBarcode(barcode)
                        }
                    })
                }

            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 解除绑定所有用例
                cameraProvider.unbindAll()

                // 绑定用例到相机
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "相机启动失败", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 处理识别到的条码
     */
    private fun handleBarcode(barcode: String) {
        Log.d(TAG, "Barcode detected: $barcode")

        // 返回结果
        val intent = Intent().apply {
            putExtra(EXTRA_BARCODE, barcode)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * 条码分析器
     */
    private class BarcodeAnalyzer(
        private val onBarcodeDetected: (String) -> Unit
    ) : ImageAnalysis.Analyzer {

        private val scanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: return

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        when (barcode.valueType) {
                            Barcode.TYPE_TEXT,
                            Barcode.TYPE_CODE_128,
                            Barcode.TYPE_CODE_39,
                            Barcode.TYPE_CODE_93,
                            Barcode.TYPE_CODABAR,
                            Barcode.TYPE_EAN_13,
                            Barcode.TYPE_EAN_8,
                            Barcode.TYPE_UPC_A,
                            Barcode.TYPE_UPC_E,
                            Barcode.TYPE_QR_CODE -> {
                                barcode.rawValue?.let {
                                    onBarcodeDetected(it)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // 识别失败，继续处理下一帧
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner?.close()
    }

    companion object {
        private const val TAG = "ScanActivity"
        const val EXTRA_BARCODE = "barcode"
        const val RESULT_OK = Activity.RESULT_OK
    }
}