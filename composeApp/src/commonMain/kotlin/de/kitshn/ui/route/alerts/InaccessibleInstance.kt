package de.kitshn.ui.route.alerts

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
import de.kitshn.ui.component.alert.FullSizeAlertPane
import de.kitshn.ui.component.buttons.BackButton
import de.kitshn.ui.component.buttons.BackButtonType
import de.kitshn.ui.route.RouteParameters
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.error_instance_inaccessible
import org.jetbrains.compose.resources.stringResource

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
                contentDescription = stringResource(Res.string.error_instance_inaccessible),
                text = stringResource(Res.string.error_instance_inaccessible)
            )
        }
    }
}