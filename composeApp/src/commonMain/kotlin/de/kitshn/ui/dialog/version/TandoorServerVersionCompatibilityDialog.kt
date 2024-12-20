package de.kitshn.ui.dialog.version

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.kitshn.KitshnViewModel
import de.kitshn.ui.theme.Typography
import de.kitshn.version.TandoorServerVersionCompatibility
import de.kitshn.version.TandoorServerVersionCompatibilityState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TandoorServerVersionCompatibilityDialog(
    vm: KitshnViewModel,
    shown: Boolean = false,
    autoDisplay: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()

    val coroutineScope = rememberCoroutineScope()

    var currentVersion by remember { mutableStateOf<String?>(null) }
    var compatibilityState by remember {
        mutableStateOf<TandoorServerVersionCompatibilityState?>(
            null
        )
    }

    var mShown by remember { mutableStateOf(false) }

    val latestVersionCheck by vm.settings.getLatestVersionCheck.collectAsState(initial = null)
    LaunchedEffect(latestVersionCheck, vm.tandoorClient, vm.tandoorClient?.container?.openapiData) {
        if(vm.tandoorClient?.container?.openapiData == null) return@LaunchedEffect

        val mCurrentVersion = vm.tandoorClient?.container?.openapiData?.version
        val mCompatibilityState =
            TandoorServerVersionCompatibility.getCompatibilityStateOfVersion(mCurrentVersion!!)

        currentVersion = mCurrentVersion
        compatibilityState = mCompatibilityState

        if(autoDisplay) {
            if(mCompatibilityState == TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY) return@LaunchedEffect
            if(latestVersionCheck == null) return@LaunchedEffect
            if(latestVersionCheck == vm.tandoorClient?.container?.openapiData?.version) return@LaunchedEffect

            mShown = true
            sheetState.expand()
        }
    }

    LaunchedEffect(shown) {
        if(shown) {
            mShown = true
            sheetState.expand()
        } else {
            mShown = false
            sheetState.hide()
        }
    }

    val compatibleVersionsList =
        TandoorServerVersionCompatibility.entries.filter { it.state == TandoorServerVersionCompatibilityState.FULL_COMPATIBILITY }
    if(currentVersion == null || compatibilityState == null) return

    if(mShown) ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            coroutineScope.launch {
                vm.settings.setLatestVersionCheck(currentVersion!!)

                if(compatibilityState?.disableDismiss == true) {
                    sheetState.expand()
                } else {
                    sheetState.hide()
                    mShown = false
                }

                onDismiss()
            }
        }
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
                imageVector = compatibilityState!!.icon,
                tint = compatibilityState!!.iconTint(),
                contentDescription = stringResource(id = compatibilityState!!.label)
            )

            Text(
                text = stringResource(id = compatibilityState!!.label),
                style = Typography.displaySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = stringResource(id = compatibilityState!!.description),
                textAlign = TextAlign.Center
            )

            if(compatibilityState?.hideCompatibleVersionsList == false) {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(compatibleVersionsList.size) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text(text = compatibleVersionsList[it].getLabel()) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}