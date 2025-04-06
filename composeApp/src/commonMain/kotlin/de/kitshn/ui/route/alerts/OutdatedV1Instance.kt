package de.kitshn.ui.route.alerts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.theme.Typography
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_sign_out
import kitshn.composeapp.generated.resources.error_outdated_v1_instance
import kitshn.composeapp.generated.resources.error_outdated_v1_instance_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteAlertOutdatedV1Instance(
    p: RouteParameters
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { }
            )
        }
    ) { pv ->
        Box(
            Modifier
                .padding(pv)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .fillMaxWidth(0.7f)
                    .widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp),
                    imageVector = Icons.Rounded.Dangerous,
                    contentDescription = stringResource(Res.string.error_outdated_v1_instance_title),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.error_outdated_v1_instance_title),
                    style = Typography().displayMedium
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.error_outdated_v1_instance)
                )

                Spacer(Modifier.height(64.dp))

                Button(
                    onClick = {
                        p.vm.signOut()
                    }
                ) {
                    Text(text = stringResource(Res.string.action_sign_out))
                }
            }
        }
    }
}