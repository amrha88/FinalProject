package com.example.automate.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.automate.util.FileUtils
import java.io.ByteArrayOutputStream

private const val MAX_DIMENSION_PX = 320
private const val JPEG_QUALITY = 80

@Composable
fun rememberProfileImagePicker(
    maxDimension: Int = MAX_DIMENSION_PX,
    onImagePicked: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            uriToBase64(context, uri, maxDimension)?.let(onImagePicked)
        }
    }
    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

@Composable
fun rememberCameraImagePicker(
    maxDimension: Int = MAX_DIMENSION_PX,
    onImagePicked: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            uriToBase64(context, uri, maxDimension)?.let(onImagePicked)
        }
    }
    return {
        val uri = FileUtils.createImageUri(context)
        pendingUri = uri
        launcher.launch(uri)
    }
}

private fun uriToBase64(context: Context, uri: Uri, maxDimension: Int): String? {
    return try {
        val original = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return null
        val scaled = scaleDown(original, maxDimension)
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val ratio = minOf(
        maxDimension.toFloat() / bitmap.width,
        maxDimension.toFloat() / bitmap.height,
        1f
    )
    if (ratio >= 1f) return bitmap
    val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
    val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}