package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageCompressor {

    private const val TAG = "ImageCompressor"

    /**
     * Creates a temporary file in the cache directory and returns its content Uri via FileProvider.
     */
    fun createTempImageUri(context: Context): Pair<Uri, File>? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val storageDir = File(context.cacheDir, "camera_photos").apply {
                if (!exists()) mkdirs()
            }
            val file = File(storageDir, "IMG_AUDIT_${timeStamp}.jpg")
            if (!file.exists()) {
                file.createNewFile()
            }
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            Pair(uri, file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create temp image URI", e)
            null
        }
    }

    /**
     * Compresses an image file from camera into an optimized local persistent image and returns its absolute path.
     * Resizes large dimensions to max 1280px and compresses JPEG quality to ~75% (< 150 KB).
     */
    suspend fun compressAndSaveImage(
        context: Context,
        sourceFile: File,
        auditPrefix: String = "audit_photo",
        watermarkText: String? = null,
        auditorName: String? = null,
        watermarkTimestamp: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.filesDir, "audit_photos").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val targetFile = File(destinationDir, "${auditPrefix}_${timeStamp}.jpg")

            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, boundsOptions)

            val maxDimension = 4096
            var sampleSize = 1
            if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / sampleSize) >= maxDimension || (halfWidth / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = false
            }
            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions) ?: return@withContext sourceFile.absolutePath

            // Check orientation from EXIF
            val exif = try {
                ExifInterface(sourceFile.absolutePath)
            } catch (e: Exception) {
                null
            }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (!matrix.isIdentity) {
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = rotatedBitmap
                }
            }

            // Stamped Watermark No Mobil & Auditor if provided (Permanen & Tidak dapat diedit manual)
            if (!watermarkText.isNullOrBlank()) {
                val mutableBitmap = if (bitmap.isMutable) bitmap else bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = android.graphics.Canvas(mutableBitmap)
                WatermarkHelper.drawVehicleWatermark(
                    canvas = canvas,
                    width = mutableBitmap.width.toFloat(),
                    height = mutableBitmap.height.toFloat(),
                    vehicleNumber = watermarkText,
                    auditorName = auditorName,
                    timestampStr = watermarkTimestamp
                )
                if (mutableBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = mutableBitmap
                }
            }

            // Save high quality JPEG with 98% quality (Ultra Crisp / HD)
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 98, out)
                out.flush()
            }
            bitmap.recycle()

            // Delete original temporary camera file if different
            if (sourceFile.exists() && sourceFile.absolutePath != targetFile.absolutePath) {
                sourceFile.delete()
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image file", e)
            sourceFile.absolutePath
        }
    }

    /**
     * Compresses an image directly from a content Uri (e.g. from gallery/picker) into an optimized persistent file.
     */
    suspend fun compressAndSaveFromUri(
        context: Context,
        sourceUri: Uri,
        auditPrefix: String = "audit_photo",
        watermarkText: String? = null,
        auditorName: String? = null,
        watermarkTimestamp: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.filesDir, "audit_photos").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val targetFile = File(destinationDir, "${auditPrefix}_${timeStamp}.jpg")

            // Read dimensions
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val maxDimension = 4096
            var sampleSize = 1
            if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / sampleSize) >= maxDimension || (halfWidth / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = false
            }
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext ""

            // Check orientation
            val orientation = try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    val exif = ExifInterface(input)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            var finalBitmap = bitmap
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (!matrix.isIdentity) {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    finalBitmap = rotated
                }
            }

            // Stamped Watermark No Mobil & Auditor if provided (Permanen & Tidak dapat diedit manual)
            if (!watermarkText.isNullOrBlank()) {
                val mutableBitmap = if (finalBitmap.isMutable) finalBitmap else finalBitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = android.graphics.Canvas(mutableBitmap)
                WatermarkHelper.drawVehicleWatermark(
                    canvas = canvas,
                    width = mutableBitmap.width.toFloat(),
                    height = mutableBitmap.height.toFloat(),
                    vehicleNumber = watermarkText,
                    auditorName = auditorName,
                    timestampStr = watermarkTimestamp
                )
                if (mutableBitmap != finalBitmap) {
                    finalBitmap.recycle()
                    finalBitmap = mutableBitmap
                }
            }

            FileOutputStream(targetFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 98, out)
                out.flush()
            }
            finalBitmap.recycle()

            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image from Uri", e)
            ""
        }
    }

    /**
     * Saves an image file from camera maintaining 4K Ultra HD resolution (up to 4096px).
     * Automatically fixes EXIF rotation and applies ultra high-quality JPEG compression (98%).
     */
    suspend fun save4KImageFile(
        context: Context,
        sourceFile: File,
        prefix: String = "cat_4k"
    ): String = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.filesDir, "audit_photos").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val targetFile = File(destinationDir, "${prefix}_${timeStamp}.jpg")

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourceFile.absolutePath, boundsOptions)

            val maxDimension = 4096 // 4K Ultra HD Crisp
            var sampleSize = 1
            if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / sampleSize) >= maxDimension || (halfWidth / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = false
            }
            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
                ?: return@withContext sourceFile.absolutePath

            // EXIF Orientation fix
            val exif = try { ExifInterface(sourceFile.absolutePath) } catch (_: Exception) { null }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (!matrix.isIdentity) {
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = rotatedBitmap
                }
            }

            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 98, out)
                out.flush()
            }
            bitmap.recycle()

            if (sourceFile.exists() && sourceFile.absolutePath != targetFile.absolutePath) {
                sourceFile.delete()
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving 4K image file", e)
            sourceFile.absolutePath
        }
    }

    /**
     * Saves an image from Uri (gallery) maintaining 4K Ultra HD resolution (up to 4096px).
     * Automatically fixes EXIF rotation and applies ultra high-quality JPEG compression (98%).
     */
    suspend fun save4KImageFromUri(
        context: Context,
        sourceUri: Uri,
        prefix: String = "cat_4k"
    ): String = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.filesDir, "audit_photos").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val targetFile = File(destinationDir, "${prefix}_${timeStamp}.jpg")

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val maxDimension = 4096 // 4K Ultra HD Crisp
            var sampleSize = 1
            if (boundsOptions.outHeight > maxDimension || boundsOptions.outWidth > maxDimension) {
                val halfHeight = boundsOptions.outHeight / 2
                val halfWidth = boundsOptions.outWidth / 2
                while ((halfHeight / sampleSize) >= maxDimension || (halfWidth / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = false
            }
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext ""

            val orientation = try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    val exif = ExifInterface(input)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            var finalBitmap = bitmap
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (!matrix.isIdentity) {
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    finalBitmap = rotated
                }
            }

            FileOutputStream(targetFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 98, out)
                out.flush()
            }
            finalBitmap.recycle()

            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving 4K image from Uri", e)
            ""
        }
    }

    /**
     * Converts a local image path to Base64 string for direct upload to Google Apps Script / Drive.
     */
    suspend fun fileToBase64(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext ""
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding file to Base64", e)
            ""
        }
    }
}

object DateUtils {
    private val indonesianLocale = Locale("id", "ID")

    fun getCurrentDateFormatted(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", indonesianLocale)
        return sdf.format(Date())
    }

    fun getCurrentTimestampFormatted(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", indonesianLocale)
        return sdf.format(Date())
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatTimestampWithSeconds(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }
}
