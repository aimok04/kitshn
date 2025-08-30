package de.kitshn.ui.dialog.version

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.kitshn.KitshnViewModel
import de.kitshn.TestTagRepository
import de.kitshn.platformDetails
import de.kitshn.ui.theme.Typography
import kitshn.composeApp.BuildConfig
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.beta_dialog_description
import kitshn.composeapp.generated.resources.beta_dialog_title
import kitshn.composeapp.generated.resources.common_okay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TandoorBetaInfoDialog(
    vm: KitshnViewModel,
    shown: Boolean = false,
    autoDisplay: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    if(!BuildConfig.PACKAGE_IS_BETA) return

    val coroutineScope = rememberCoroutineScope()

    var mShown by remember { mutableStateOf(false) }

    val latestBetaVersionCheck by vm.settings.getLatestBetaVersionCheck.collectAsState(initial = null)
    LaunchedEffect(latestBetaVersionCheck) {
        if(autoDisplay) {
            if(platformDetails.packageExtendedVersion == latestBetaVersionCheck) return@LaunchedEffect

            mShown = true
        }
    }

    LaunchedEffect(shown) {
        if(shown) {
            mShown = true
        } else {
            mShown = false
        }
    }

    fun dismiss() {
        coroutineScope.launch {
            vm.settings.setLatestBetaVersionCheck(platformDetails.packageExtendedVersion)
            mShown = false

            onDismiss()
        }
    }

    if(mShown) ModalBottomSheet(
        onDismissRequest = { dismiss() }
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp),
                imageVector = Icons.Rounded.Science,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(Res.string.beta_dialog_title)
            )

            Text(
                text = stringResource(Res.string.beta_dialog_title),
                style = Typography().displaySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = stringResource(Res.string.beta_dialog_description),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.testTag(TestTagRepository.ACTION_OKAY.name),
                onClick = { dismiss() }
            ) {
                Text(
                    text = stringResource(Res.string.common_okay)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}