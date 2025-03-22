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
    val state: TandoorServerVersionCompatibilityState,
    val notes: String? = ""
) {
    V1_5_33(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_32(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_31(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_30(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_29(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_28(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_27(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_26(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_25(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_24(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_23(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_22(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_21(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_20(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_19(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_18(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V1_5_17(TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY);

    fun getLabel(): String {
        return this.name.substring(1).replace("_", ".")
    }

    companion object {
        private fun parseVersion(version: String): TandoorServerVersionCompatibility {
            return valueOf("V" + version.replace(".", "_"))
        }

        fun getCompatibilityStateOfVersion(version: String): TandoorServerVersionCompatibilityState {
            return try {
                parseVersion(version).state
            } catch(e: Exception) {
                TandoorServerVersionCompatibilityState.UNKNOWN
            }
        }
    }
}