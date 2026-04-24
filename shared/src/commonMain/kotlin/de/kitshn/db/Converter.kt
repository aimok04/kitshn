package de.kitshn.db

import androidx.room.TypeConverter
import de.kitshn.api.tandoor.model.TandoorUnit
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingList
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryCreatedBy
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryFood
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntryListRecipeData
import de.kitshn.json

class Converters {
    @TypeConverter
    fun fromShoppingList(value: List<TandoorShoppingList>): String = json.encodeToString(value)

    @TypeConverter
    fun toShoppingList(value: String): List<TandoorShoppingList> = json.decodeFromString(value)

    @TypeConverter
    fun fromFood(value: TandoorShoppingListEntryFood): String = json.encodeToString(value)

    @TypeConverter
    fun toFood(value: String): TandoorShoppingListEntryFood = json.decodeFromString(value)

    @TypeConverter
    fun fromUnit(value: TandoorUnit?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toUnit(value: String?): TandoorUnit? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromCreatedBy(value: TandoorShoppingListEntryCreatedBy): String = json.encodeToString(value)

    @TypeConverter
    fun toCreatedBy(value: String): TandoorShoppingListEntryCreatedBy = json.decodeFromString(value)

    @TypeConverter
    fun fromListRecipeData(value: TandoorShoppingListEntryListRecipeData?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toListRecipeData(value: String?): TandoorShoppingListEntryListRecipeData? = value?.let { json.decodeFromString(it) }
}