package de.kitshn

sealed interface AppEvent {
    data class HouseholdChanged(val householdId: Int) : AppEvent
}
