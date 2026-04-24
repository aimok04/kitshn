package de.kitshn

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabasePath(): String {
    val dbFile = File(System.getProperty("java.io.tmpdir"), ROOM_DB_FILE)
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
