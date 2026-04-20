package de.kitshn.ui.component.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.recipe_step_step_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun MarkdownEditor(
    state: RichTextState
) {
    Column {
        RichTextEditor(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.recipe_step_step_description)) },
        )

        MarkdownEditorRow(
            state = state
        )
    }
}