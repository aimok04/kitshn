package de.kitshn

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.kitshn.db.Converters
import de.kitshn.db.dao.ShoppingDao
import de.kitshn.db.dao.UnitDao
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingTransactionEntity
import de.kitshn.db.entity.UnitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


@Database(
    entities = [
        UnitEntity::class,
        ShoppingItemEntity::class,
        ShoppingTransactionEntity::class
    ],
    version = 2,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun shoppingDao(): ShoppingDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor: RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun AppDatabase.closeAndDelete()
