package de.kitshn

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import de.kitshn.db.Converters
import de.kitshn.db.dao.FoodDao
import de.kitshn.db.dao.RepoMetaDao
import de.kitshn.db.dao.ShoppingDao
import de.kitshn.db.dao.ShoppingListDao
import de.kitshn.db.dao.SupermarketCategoryDao
import de.kitshn.db.dao.SupermarketDao
import de.kitshn.db.dao.UnitDao
import de.kitshn.db.entity.FoodEntity
import de.kitshn.db.entity.FoodPendingDeleteEntity
import de.kitshn.db.entity.RepoMetaEntity
import de.kitshn.db.entity.ShoppingItemEntity
import de.kitshn.db.entity.ShoppingListEntity
import de.kitshn.db.entity.ShoppingTransactionEntity
import de.kitshn.db.entity.SupermarketCategoryEntity
import de.kitshn.db.entity.SupermarketCategoryPendingDeleteEntity
import de.kitshn.db.entity.SupermarketCategoryToSupermarketEntity
import de.kitshn.db.entity.SupermarketEntity
import de.kitshn.db.entity.UnitEntity
import de.kitshn.db.entity.UnitPendingDeleteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


@Database(
    entities = [
        UnitEntity::class,
        UnitPendingDeleteEntity::class,
        SupermarketCategoryEntity::class,
        SupermarketCategoryPendingDeleteEntity::class,
        FoodEntity::class,
        FoodPendingDeleteEntity::class,
        ShoppingItemEntity::class,
        ShoppingTransactionEntity::class,
        ShoppingListEntity::class,
        SupermarketEntity::class,
        SupermarketCategoryToSupermarketEntity::class,
        RepoMetaEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun supermarketCategoryDao(): SupermarketCategoryDao
    abstract fun foodDao(): FoodDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun supermarketDao(): SupermarketDao
    abstract fun repoMetaDao(): RepoMetaDao

    // Fallback for platforms where Room does not generate clearAllTables. Prefer wipeAllData
    // Also make sure the order of foreign restrictions is correct.
    suspend fun deleteAllData() {
        shoppingDao().deleteAllTransactions()
        shoppingDao().deleteAll()
        foodDao().deleteAll()
        foodDao().deleteAllPendingDeletes()
        supermarketCategoryDao().deleteAllPendingDeletes()
        supermarketDao().deleteAllJoins()
        shoppingListDao().deleteAll()
        supermarketDao().deleteAll()
        supermarketCategoryDao().deleteAll()
        unitDao().deleteAll()
        unitDao().deleteAllPendingDeletes()
        repoMetaDao().deleteAll()
    }
}

expect object AppDatabaseConstructor: RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(connection: SQLiteConnection) {
                // should be on by default but just be sure since we need it
                connection.execSQL("PRAGMA foreign_keys = ON")
                // dont immediately crash when db is locked
                connection.execSQL("PRAGMA busy_timeout = 500")
            }
        })
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun AppDatabase.closeAndDelete()
expect suspend fun AppDatabase.wipeAllData()
