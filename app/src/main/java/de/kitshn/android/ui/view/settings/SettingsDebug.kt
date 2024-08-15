package de.kitshn.android.ui.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CarCrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import de.kitshn.android.ui.component.buttons.BackButton
import de.kitshn.android.ui.component.settings.SettingsListItem
import de.kitshn.android.ui.view.ViewParameters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSettingsDebug(
    p: ViewParameters
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(state = rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(p.back) },
                title = { Text(text = "Debug settings") },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingsListItem(
                    label = { Text("Test crash reporting") },
                    description = { Text("Simulate app crash") },
                    icon = Icons.Rounded.CarCrash,
                    contentDescription = "Test crash reporting",
                ) {
                    null!!
                }
            }
        }
    }
}