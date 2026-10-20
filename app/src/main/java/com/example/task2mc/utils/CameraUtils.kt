package com.example.task2mc.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat

private const val TAG = "CameraUtils"

fun generatePhotoName(): String = "ROUTE_IMG_${System.currentTimeMillis()}.jpg"

/**
 * Toma una foto y la guarda en la galeria del dispositivo (Pictures/Task2MC)
 * usando MediaStore. Devuelve nombre y uri por el callback onSaved.
 */
fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onSaved: (name: String, uri: String) -> Unit
) {
    val name = generatePhotoName()
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Task2MC")
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d(TAG, "Photo saved: ${output.savedUri}")
                output.savedUri?.let { onSaved(name, it.toString()) }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo error", exception)
            }
        }
    )
}
