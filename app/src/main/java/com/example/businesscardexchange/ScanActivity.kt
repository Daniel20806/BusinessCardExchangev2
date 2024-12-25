package com.example.businesscardexchange

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    /*確認相機權限，如果有則開啓相機*/
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "請開啓相機權限！", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        /*開啓相機掃描，不斷偵測是否有 QR Code*/

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)  // Only scan QR codes
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)

        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /*開啓相機流程*/
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview configuration
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image analysis configuration
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
                }

            // Select the back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Bind use cases to camera
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "ScanActivity"
    }

    private inner class BarcodeAnalyzer :
        ImageAnalysis.Analyzer {

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // Pass the image to the barcode scanner
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        // Process the first scanned barcode
                        barcodes.firstOrNull()?.let { barcode ->
                            if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                // 創建 Intent 並將掃描結果放到Intent裏面
                                val resultIntent = Intent().apply {
                                    putExtra("scan_result", barcode.rawValue)
                                }

                                // 設定結果並關閉 ScanActivity
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()

                            }
                        }
                    }
                    .addOnFailureListener {
                        // Handle scan failure
                        // ...
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }
}
