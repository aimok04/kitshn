package de.kitshn.model.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class KitshnForm(
    private val sections: List<KitshnFormSection> = listOf(),
    val submitButton: @Composable (onClick: () -> Unit) -> Unit = { },
    val onSubmit: () -> Unit = { }
) {

    @Composable
    fun Render(
        nestedScrollConnection: NestedScrollConnection? = null
    ) {
        LazyVerticalGrid(
            modifier = nestedScrollConnection?.let { Modifier.nestedScroll(it) } ?: Modifier,
            columns = GridCells.Adaptive(250.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            repeat(sections.size) { sectionIndex ->
                val section = sections[sectionIndex]

                items(
                    section.items.size,
                    key = { section.items[it].hashCode() },
                    span = { index ->
                        GridItemSpan(
                            if(section.items.size == (index + 1)) {
                                this.maxCurrentLineSpan
                            } else {
                                1
                            }
                        )
                    }
                ) { index ->
                    section.items[index].Render()
                }

                item(
                    span = {
                        GridItemSpan(this.maxCurrentLineSpan)
                    }
                ) {
                    if(sections.size != (sectionIndex + 1))
                        HorizontalDivider()
                }
            }
        }
    }

    suspend fun checkSubmit(): Boolean {
        var successful = true
        for(section in sections)
            for(item in section.items)
                if(!item.submit()) successful = false

        return successful
    }

    @Composable
    fun RenderSubmitButton() {
        val coroutine = rememberCoroutineScope()

        submitButton {
            coroutine.launch {
                if(checkSubmit()) onSubmit()
            }
        }
    }

}