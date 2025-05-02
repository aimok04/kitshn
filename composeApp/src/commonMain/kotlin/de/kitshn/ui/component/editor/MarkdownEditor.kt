package de.kitshn.ui.component.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

@Composable
fun MarkdownEditor(
    state: RichTextState
) {
    Column {
        RichTextEditor(
            state = state,
            modifier = Modifier.fillMaxWidth()
        )

        MarkdownEditorRow(
            state = state
        )
    }
}