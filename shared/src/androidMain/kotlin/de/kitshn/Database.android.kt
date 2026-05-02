package de.kitshn

import androidx.room.Room
import androidx.room.RoomDatabase
import coil3.PlatformContext
import java.io.File

private var dbPath: String? = null

fun getDatabasePath(context: PlatformContext): String {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(ROOM_DB_FILE)
    return dbFile.absolutePath.also { dbPath = it }
}

fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = getDatabasePath(context)
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFilePath
    )
}

actual fun AppDatabase.closeAndDelete() {
    close()
    dbPath?.let { path ->
        File(path).delete()
        File("$path-shm").delete()
        File("$path-wal").delete()
    }
}
