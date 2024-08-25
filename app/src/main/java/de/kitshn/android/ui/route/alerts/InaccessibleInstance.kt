package de.kitshn.android.ui.route.alerts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.kitshn.android.R
import de.kitshn.android.ui.component.alert.FullSizeAlertPane
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.buttons.BackButtonType
import de.kitshn.android.ui.route.RouteParameters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteAlertInaccessibleInstance(
    p: RouteParameters
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    BackButton(onBack = p.onBack, type = BackButtonType.CLOSE)
                }
            )
        }
    ) { pv ->
        Box(
            Modifier
                .padding(pv)
                .fillMaxSize()
        ) {
            FullSizeAlertPane(
                imageVector = Icons.Rounded.KeyOff,
                contentDescription = stringResource(id = R.string.error_instance_inaccessible),
                text = stringResource(id = R.string.error_instance_inaccessible)
            )
        }
    }
}