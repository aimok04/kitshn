package de.kitshn

import androidx.room.Room
import androidx.room.RoomDatabase
import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

fun getDatabasePath(): String {
    return "${documentDirectory()}/$ROOM_DB_FILE"
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = getDatabasePath()
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}

@OptIn(ExperimentalForeignApi::class)
actual fun AppDatabase.closeAndDelete() {
    close()
    val path = getDatabasePath()
    NSFileManager.defaultManager.removeItemAtPath(path, null)
    NSFileManager.defaultManager.removeItemAtPath("$path-shm", null)
    NSFileManager.defaultManager.removeItemAtPath("$path-wal", null)
}
