package com.kalenyator.app.util

import android.content.Context
import android.net.Uri
import java.io.File

object PhotoStorage {
    private fun photosDir(context: Context) = File(context.filesDir, "photos").apply { mkdirs() }

    fun fileFor(context: Context, eventId: Long): File = File(photosDir(context), "event_$eventId.jpg")

    fun saveFromUri(context: Context, eventId: Long, uri: Uri): String? = runCatching {
        val dest = fileFor(context, eventId)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    }.getOrNull()

    fun delete(context: Context, path: String?) {
        path?.let { runCatching { File(it).delete() } }
    }
}
