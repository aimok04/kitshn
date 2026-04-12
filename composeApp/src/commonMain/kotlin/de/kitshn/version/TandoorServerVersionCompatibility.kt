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
    val tint: @Composable () -> Color,
    val hideCompatibleVersionsList: Boolean = false,
    val disableDismiss: Boolean = false
) {
    INCOMPATIBLE(
        label = Res.string.tandoor_compatibility_incompatible_label,
        description = Res.string.tandoor_compatibility_incompatible_description,
        icon = Icons.Rounded.Block,
        iconTint = { MaterialTheme.colorScheme.error },
        tint = { MaterialTheme.colorScheme.error },
        disableDismiss = true
    ),
    UNKNOWN(
        label = Res.string.tandoor_compatibility_unknown_label,
        description = Res.string.tandoor_compatibility_unknown_description,
        icon = Icons.Rounded.QuestionMark,
        iconTint = { Color.Gray },
        tint = { Color.Gray }
    ),
    MIXED_COMPATIBILITY(
        label = Res.string.tandoor_compatibility_mixed_compatibility_label,
        description = Res.string.tandoor_compatibility_mixed_compatibility_description,
        icon = Icons.Rounded.WarningAmber,
        iconTint = { Color(0xFFBA8E23) }, // darker yellow
        tint = { Color.Yellow }
    ),
    FULL_COMPATIBILITY(
        label = Res.string.tandoor_compatibility_full_compatibility_label,
        description = Res.string.tandoor_compatibility_full_compatibility_description,
        icon = Icons.Rounded.Check,
        iconTint = { Color(0xFF06402B) }, // darker green
        tint = { Color.Green },
        hideCompatibleVersionsList = true
    ),
    NOT_CHECKABLE(
        label = Res.string.tandoor_compatibility_not_checkable_label,
        description = Res.string.tandoor_compatibility_not_checkable_description,
        icon = Icons.Rounded.QuestionMark,
        iconTint = { Color.Gray },
        tint = { Color.Gray }
    )
}

enum class TandoorServerVersionCompatibility(
    val version: String,
    val state: TandoorServerVersionCompatibilityState
) {
    V2_0_0("2.0.0", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_0_1("2.0.1", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_0_2("2.0.2", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_0_3("2.0.3", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_1_0("2.1.0", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_1_1("2.1.1", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_1_2("2.1.2", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_2_0("2.2.0", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_1("2.2.1", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_2("2.2.2", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_3("2.2.3", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_4("2.2.4", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_5("2.2.5", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_6("2.2.6", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_2_7("2.2.7", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_0("2.3.0", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_1("2.3.1", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_2("2.3.2", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_3("2.3.3", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_4("2.3.4", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_5("2.3.5", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_3_6("2.3.6", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_4_0("2.4.0", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_4_1("2.4.1", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_5_0("2.5.0", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_5_1("2.5.1", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_5_2("2.5.2", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_5_3("2.5.3", TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY),
    V2_6_0("2.6.0", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_6_1("2.6.1", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_6_2("2.6.2", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_6_3("2.6.3", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY),
    V2_6_4("2.6.4", TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY);

    fun getLabel(): String {
        return this.name.substring(1).replace("_", ".")
    }

    companion object {
        private fun parseVersion(version: String): TandoorServerVersionCompatibility {
            return entries.find { it.version == version } ?: throw NullPointerException()
        }

        fun getCompatibilityStateOfVersion(version: String): TandoorServerVersionCompatibilityState {
            // versions precedence:
            // 1. Version found in version matrix -> use that
            // 2. Go down bux-fix until 0 -> use that.
            // 3. Go down minor-fix until 0 (also include bug fix version) -> MIXED_COMPATIBILITY
            // Else Unknown

            val parts = version.split(".").map { it.toIntOrNull() ?: return TandoorServerVersionCompatibilityState.UNKNOWN }
            if (parts.size != 3) return TandoorServerVersionCompatibilityState.UNKNOWN

            val (major, minor, patch) = parts

            try { return parseVersion(version).state } catch (_: NullPointerException) {}

            // walk down bugfix and use that
            for (p in (patch - 1) downTo 0){
                try { return parseVersion("$major.$minor.$p").state } catch (_: NullPointerException) {}
            }

            // walk down minor and if found anything return at least MIXED or the more severe state
            for (m in (minor - 1) downTo 0) {
                for (p in (patch - 1) downTo 0){
                    try {
                        val state = parseVersion("$major.$m.$p").state

                        return if (state == TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY) {
                            TandoorServerVersionCompatibilityState.MIXED_COMPATIBILITY
                        } else {
                            state
                        }
                    } catch (_: NullPointerException) {}
                }
            }

            return TandoorServerVersionCompatibilityState.UNKNOWN
        }
    }
}