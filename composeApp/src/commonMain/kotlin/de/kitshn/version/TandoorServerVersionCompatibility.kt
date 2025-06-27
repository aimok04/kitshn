package de.kitshn.version

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.tandoor_compatibility_full_compatibility_description
import kitshn.composeapp.generated.resources.tandoor_compatibility_full_compatibility_label
import kitshn.composeapp.generated.resources.tandoor_compatibility_incompatible_description
import kitshn.composeapp.generated.resources.tandoor_compatibility_incompatible_label
import kitshn.composeapp.generated.resources.tandoor_compatibility_mixed_compatibility_description
import kitshn.composeapp.generated.resources.tandoor_compatibility_mixed_compatibility_label
import kitshn.composeapp.generated.resources.tandoor_compatibility_not_checkable_description
import kitshn.composeapp.generated.resources.tandoor_compatibility_not_checkable_label
import kitshn.composeapp.generated.resources.tandoor_compatibility_unknown_description
import kitshn.composeapp.generated.resources.tandoor_compatibility_unknown_label
import org.jetbrains.compose.resources.StringResource

enum class TandoorServerVersionCompatibilityState(
    val label: StringResource,
    val description: StringResource,
    val icon: ImageVector,
    val iconTint: @Composable () -> Color,
    val hideCompatibleVersionsList: Boolean = false,
    val disableDismiss: Boolean = false
) {
    INCOMPATIBLE(
        label = Res.string.tandoor_compatibility_incompatible_label,
        description = Res.string.tandoor_compatibility_incompatible_description,
        icon = Icons.Rounded.Block,
        iconTint = { MaterialTheme.colorScheme.error },
        disableDismiss = true
    ),
    MIXED_COMPATIBILITY(
        label = Res.string.tandoor_compatibility_mixed_compatibility_label,
        description = Res.string.tandoor_compatibility_mixed_compatibility_description,
        icon = Icons.Rounded.WarningAmber,
        iconTint = { Color.Yellow }
    ),
    FULL_COMPATIBILITY(
        label = Res.string.tandoor_compatibility_full_compatibility_label,
        description = Res.string.tandoor_compatibility_full_compatibility_description,
        icon = Icons.Rounded.Check,
        iconTint = { Color.Green },
        hideCompatibleVersionsList = true
    ),
    UNKNOWN(
        label = Res.string.tandoor_compatibility_unknown_label,
        description = Res.string.tandoor_compatibility_unknown_description,
        icon = Icons.Rounded.QuestionMark,
        iconTint = { Color.Gray }
    ),
    NOT_CHECKABLE(
        label = Res.string.tandoor_compatibility_not_checkable_label,
        description = Res.string.tandoor_compatibility_not_checkable_description,
        icon = Icons.Rounded.QuestionMark,
        iconTint = { Color.Gray }
    )
}

enum class TandoorServerVersionCompatibility(
    val version: String,
    val state: TandoorServerVersionCompatibilityState
) {
    V2_0_0_ALPHA_1("2.0.0-alpha-1", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_0_0_ALPHA_2("2.0.0-alpha-2", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_0_0_ALPHA_3("2.0.0-alpha-3", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_0_0_ALPHA_4("2.0.0-alpha-4", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_0_0_ALPHA_5("2.0.0-alpha-5", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_0_0_ALPHA_6("2.0.0-alpha-6", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_0_0_BETA_1("2.0.0-beta-1", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY);

    fun getLabel(): String {
        return this.name.substring(1).replace("_", ".")
    }

    companion object {
        private fun parseVersion(version: String): TandoorServerVersionCompatibility {
            return entries.find { it.version == version } ?: throw NullPointerException()
        }

        fun getCompatibilityStateOfVersion(version: String): TandoorServerVersionCompatibilityState {
            return try {
                parseVersion(version).state
            } catch(e: NullPointerException) {
                TandoorServerVersionCompatibilityState.UNKNOWN
            }
        }
    }
}