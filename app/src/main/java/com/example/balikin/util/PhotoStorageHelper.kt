package com.example.balikin.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PhotoStorageHelper {

    private const val PHOTO_DIR_NAME = "item_photos"

    /**
     * Returns the dedicated directory for storing item photos: context.filesDir/item_photos/
     */
    fun getPhotoDirectory(context: Context): File {
        val dir = File(context.filesDir, PHOTO_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies a selected photo Uri into context.filesDir/item_photos/
     * Preserves image format by inspecting MIME type.
     * Returns the absolute path of the saved file, or null if copy failed.
     */
    fun savePhotoFromUri(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val extension = when {
                mimeType?.contains("png", ignoreCase = true) == true -> "png"
                mimeType?.contains("webp", ignoreCase = true) == true -> "webp"
                else -> "jpg"
            }

            val dir = getPhotoDirectory(context)
            val fileName = "item_${UUID.randomUUID()}.$extension"
            val destFile = File(dir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Safely deletes a photo file by its path if it exists.
     */
    fun deletePhotoFile(photoPath: String?): Boolean {
        if (photoPath.isNullOrBlank()) return false
        return try {
            val file = File(photoPath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Resolves the File object for displaying with Coil.
     * Handles both relative and absolute paths safely.
     */
    fun resolvePhotoFile(context: Context, photoPath: String?): File? {
        if (photoPath.isNullOrBlank()) return null
        val directFile = File(photoPath)
        if (directFile.exists()) return directFile

        val relativeFile = File(getPhotoDirectory(context), photoPath)
        return if (relativeFile.exists()) relativeFile else null
    }
}
