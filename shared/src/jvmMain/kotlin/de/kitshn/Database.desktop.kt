package de.kitshn

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabasePath(): String {
    val osName = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")
    val appDataDir = when {
        osName.contains("win") -> File(System.getenv("APPDATA"), "kitshn")
        osName.contains("mac") -> File(userHome, "Library/Application Support/kitshn")
        else -> {
            val xdgDataHome = System.getenv("XDG_DATA_HOME")
            if (xdgDataHome != null && xdgDataHome.isNotEmpty()) {
                File(xdgDataHome, "kitshn")
            } else {
                File(userHome, ".local/share/kitshn")
            }
        }
    }

    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }

    val dbFile = File(appDataDir, ROOM_DB_FILE)
    return dbFile.absolutePath
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = getDatabasePath()
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}

actual fun AppDatabase.closeAndDelete() {
    close()
    val path = getDatabasePath()
    File(path).delete()
    File("$path-shm").delete()
    File("$path-wal").delete()
}
