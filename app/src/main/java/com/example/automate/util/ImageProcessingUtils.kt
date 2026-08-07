package com.example.automate.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageProcessingUtils {
    suspend fun processImage(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            val rotation = context.contentResolver.openInputStream(uri)?.use { 
                val exif = ExifInterface(it)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0

            val rotatedBitmap = if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
            } else {
                originalBitmap
            }

            // Resize if too large
            val maxSize = 1600
            val scale = if (rotatedBitmap.width > maxSize || rotatedBitmap.height > maxSize) {
                val widthScale = maxSize.toFloat() / rotatedBitmap.width
                val heightScale = maxSize.toFloat() / rotatedBitmap.height
                kotlin.math.min(widthScale, heightScale)
            } else {
                1.0f
            }

            if (scale < 1.0f) {
                Bitmap.createScaledBitmap(
                    rotatedBitmap,
                    (rotatedBitmap.width * scale).toInt(),
                    (rotatedBitmap.height * scale).toInt(),
                    true
                )
            } else {
                rotatedBitmap
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageProcessing", "Error processing image", e)
            null
        }
    }
}
