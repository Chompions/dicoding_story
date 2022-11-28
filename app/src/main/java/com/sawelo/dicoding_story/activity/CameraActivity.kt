package com.sawelo.dicoding_story.activity

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.databinding.ActivityCameraBinding
import com.sawelo.dicoding_story.utils.CameraUtilsImpl

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isCameraPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS_CAMERA,
                REQUEST_CODE_PERMISSIONS_CAMERA
            )
        }

        binding.ibCameraSwitch.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            startCamera()
        }

        binding.ibCameraCapture.setOnClickListener {
            takePhoto()
        }

        startCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS_CAMERA) {
            if (!isCameraPermissionsGranted()) {
                Toast
                    .makeText(this, "Permissions denied", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun isCameraPermissionsGranted() = REQUIRED_PERMISSIONS_CAMERA.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .apply {
                    setSurfaceProvider(binding.pvCameraPreviewView.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Snackbar
                    .make(binding.root, "Unable to open camera", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = CameraUtilsImpl.createTempFile(this)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    println(exc)
                    Snackbar
                        .make(binding.root, "Failed to take a photo", Snackbar.LENGTH_SHORT)
                        .show()
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent()
                    intent.putExtra(PHOTO_EXTRA, photoFile)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        )
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS_CAMERA = 10
        private val REQUIRED_PERMISSIONS_CAMERA = arrayOf(CAMERA)
        const val PHOTO_EXTRA = "PHOTO_EXTRA"
    }
}