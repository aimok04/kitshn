package de.kitshn.android.model.form.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class KitshnFormCheckItem(
    val value: () -> Boolean,
    val onValueChange: (value: Boolean) -> Unit,

    val label: @Composable () -> Unit,
    val description: @Composable (() -> Unit)? = null,
    val leadingIcon: @Composable (() -> Unit)? = null
) : KitshnFormBaseItem() {

    @Composable
    override fun Render() {
        ListItem(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    generalError = null
                    onValueChange(!value())
                },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            leadingContent = leadingIcon,
            headlineContent = label,
            supportingContent = description,
            trailingContent = {
                Switch(
                    checked = value(),
                    onCheckedChange = onValueChange
                )
            }
        )
    }

}