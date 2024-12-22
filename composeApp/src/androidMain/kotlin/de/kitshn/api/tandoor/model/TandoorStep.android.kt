package de.kitshn.api.tandoor.model

import android.content.Context
import de.kitshn.api.tandoor.getByteArray
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipFile

// only implemented in android
suspend fun TandoorStep.loadFile(
    context: Context
): File? {
    if(client == null || file == null) return null

    val cacheDir = context.cacheDir
    val filesDir = File(context.filesDir, "file-downloads")
    filesDir.mkdirs()

    val downloadedFile = File(filesDir, "${file.id}_${file.name.hashCode()}")
    if(downloadedFile.exists()) return downloadedFile

    val zipFile = File(cacheDir, UUID.randomUUID().toString())

    // Downloading file
    FileOutputStream(zipFile).use {
        it.write(client!!.getByteArray("/download-file/${file.id}"))
    }

    // Tandoor serves files in .zip containers
    ZipFile(zipFile).use { zipFile ->
        val folderStream = zipFile.getInputStream(zipFile.entries().asSequence().first())

        FileOutputStream(downloadedFile).use { os ->
            folderStream.copyTo(os)
        }
    }

    zipFile.delete()
    return downloadedFile
}