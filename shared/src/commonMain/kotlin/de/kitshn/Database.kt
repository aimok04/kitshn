package de.kitshn

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.kitshn.db.Converters
import de.kitshn.db.dao.FoodDao
import de.kitshn.db.dao.ShoppingDao
import de.kitshn.db.dao.SupermarketCategoryDao
import de.kitshn.db.dao.UnitDao
import de.kitshn.db.entity.FoodEntity
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingTransactionEntity
import de.kitshn.db.entity.SupermarketCategoryEntity
import de.kitshn.db.entity.UnitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


@Database(
    entities = [
        UnitEntity::class,
        SupermarketCategoryEntity::class,
        FoodEntity::class,
        ShoppingItemEntity::class,
        ShoppingTransactionEntity::class
    ],
    version = 4,
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun supermarketCategoryDao(): SupermarketCategoryDao
    abstract fun foodDao(): FoodDao
    abstract fun shoppingDao(): ShoppingDao
}

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
