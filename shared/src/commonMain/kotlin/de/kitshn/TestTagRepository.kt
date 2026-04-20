package de.kitshn

enum class TestTagRepository {
    ACTION_ABORT,
    ACTION_ADD,
    ACTION_CLOSE_DIALOG,
    ACTION_CLOSE_RECIPE,
    ACTION_CLOSE_RECIPE_BOOK,
    ACTION_CONTINUE,
    ACTION_OKAY,
    CARD_HORIZONTAL_RECIPE_BOOK,
    CARD_MEAL_PLAN_DAY,
    CARD_RECIPE,
    LIST_ITEM_SHOPPING_LIST_ENTRY,
    SCAFFOLD_SHOPPING;

    fun active(isActive: Boolean): String {
        return when(isActive) {
            true -> "ACTIVE_$name"
            false -> "INACTIVE_$name"
        }
    }
}