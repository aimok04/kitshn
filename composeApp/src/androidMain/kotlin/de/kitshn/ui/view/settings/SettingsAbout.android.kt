package de.kitshn.ui.view.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.Text
import de.kitshn.launchMarketPageHandler
import de.kitshn.ui.component.settings.SettingsListItem
import de.kitshn.ui.component.settings.SettingsListItemPosition
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.common_review
import kitshn.composeapp.generated.resources.settings_section_about_item_review_description
import org.jetbrains.compose.resources.stringResource

actual fun LazyListScope.platformSpecificItems() {
    item {
        val launchMarketPlace = launchMarketPageHandler()

        SettingsListItem(
            position = SettingsListItemPosition.SINGULAR,
            label = { Text(stringResource(Res.string.common_review)) },
            description = { Text(stringResource(Res.string.settings_section_about_item_review_description)) },
            icon = Icons.Rounded.RateReview,
            contentDescription = stringResource(Res.string.common_review),
            onClick = {
                launchMarketPlace()
            }
        )
    }
}