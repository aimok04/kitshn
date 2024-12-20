package de.kitshn.ui.route.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.R
import de.kitshn.ui.component.onboarding.KitshnLogoAnimation
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.theme.Typography

@Composable
fun RouteOnboarding(
    p: RouteParameters
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                p.vm.navHostController?.navigate("onboarding/signIn")
            }) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    stringResource(R.string.action_next)
                )
            }
        }
    ) {
        LazyColumn(
            Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    Modifier
                        .padding(24.dp)
                        .widthIn(100.dp, 400.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    KitshnLogoAnimation()

                    Spacer(Modifier.height(24.dp))

                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = Typography.displayMedium
                        )

                        Text(
                            text = stringResource(R.string.onboarding_introduction)
                        )
                    }
                }
            }
        }
    }
}