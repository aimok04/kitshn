package de.kitshn.cache

import coil3.PlatformContext
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.model.shopping.TandoorShoppingListEntry
import de.kitshn.json
import kotlinx.serialization.Serializable

@Serializable
enum class ShoppingListEntryOfflineActions {
    CHECK,
    DELETE
}

class ShoppingListEntriesCache(
    context: PlatformContext,
    client: TandoorClient
) : BaseCache("SHOPPING_LIST_ENTRIES", context, client) {

    fun update(entries: List<TandoorShoppingListEntry>) {
        settings.putString("entries", json.encodeToString(entries))
    }

    fun retrieve() =
        settings.getStringOrNull("entries")?.let {
            json.decodeFromString<List<TandoorShoppingListEntry>>(it)
        }

    // delete items with _destroyed or _checked set to true
    fun purgeCache() {
        val items = retrieve() ?: return
        update(
            items.filterNot {
                (it.destroyed || it._destroyed)
                        || (it.checked || it._checked)
            }
        )
    }

    fun setOfflineAction(entryId: Int, action: ShoppingListEntryOfflineActions) {
        settings.putString("offlineAction_${entryId}", json.encodeToString(action))
    }

    fun resetOfflineAction(entryId: Int) {
        settings.remove("offlineAction_${entryId}")
    }

    fun retrieveOfflineActions(): Map<Int, ShoppingListEntryOfflineActions> {
        return settings.keys.filter { it.startsWith("offlineAction_") }
            .map { it.replaceFirst("offlineAction_", "").toInt() }
            .associateWith {
                json.decodeFromString<ShoppingListEntryOfflineActions>(
                    settings.getStringOrNull(
                        "offlineAction_$it"
                    )!!
                )
            }
    }

}