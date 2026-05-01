package de.kitshn.ui.component.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.toHex
import de.kitshn.ui.theme.custom.BLUE_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.GREEN_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.LIGHT_RED_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.LILA_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.MAGENTA_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.OLIVE_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.RED_COLOR_SCHEME_SEED
import de.kitshn.ui.theme.custom.TEAL_COLOR_SCHEME_SEED
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.color_black
import kitshn.shared.generated.resources.color_blue
import kitshn.shared.generated.resources.color_cyan
import kitshn.shared.generated.resources.color_dark_gray
import kitshn.shared.generated.resources.color_gray
import kitshn.shared.generated.resources.color_green
import kitshn.shared.generated.resources.color_light_gray
import kitshn.shared.generated.resources.color_light_red
import kitshn.shared.generated.resources.color_lila
import kitshn.shared.generated.resources.color_magenta
import kitshn.shared.generated.resources.color_olive
import kitshn.shared.generated.resources.color_red
import kitshn.shared.generated.resources.color_teal
import kitshn.shared.generated.resources.color_white
import kitshn.shared.generated.resources.color_yellow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val PREDEFINED_COLORS = listOf(
    RED_COLOR_SCHEME_SEED,
    LIGHT_RED_COLOR_SCHEME_SEED,
    MAGENTA_COLOR_SCHEME_SEED,
    LILA_COLOR_SCHEME_SEED,
    BLUE_COLOR_SCHEME_SEED,
    TEAL_COLOR_SCHEME_SEED,
    GREEN_COLOR_SCHEME_SEED,
    OLIVE_COLOR_SCHEME_SEED,
    Color.Gray,
    Color.Black,
    Color.White,
    Color.DarkGray,
    Color.LightGray,
    Color.Cyan,
    Color.Magenta,
    Color.Yellow
)

fun Color.toNameResource(): StringResource? {
    return when (this) {
        RED_COLOR_SCHEME_SEED -> Res.string.color_red
        LIGHT_RED_COLOR_SCHEME_SEED -> Res.string.color_light_red
        MAGENTA_COLOR_SCHEME_SEED -> Res.string.color_magenta
        LILA_COLOR_SCHEME_SEED -> Res.string.color_lila
        BLUE_COLOR_SCHEME_SEED -> Res.string.color_blue
        TEAL_COLOR_SCHEME_SEED -> Res.string.color_teal
        GREEN_COLOR_SCHEME_SEED -> Res.string.color_green
        OLIVE_COLOR_SCHEME_SEED -> Res.string.color_olive
        Color.Gray -> Res.string.color_gray
        Color.Black -> Res.string.color_black
        Color.White -> Res.string.color_white
        Color.DarkGray -> Res.string.color_dark_gray
        Color.LightGray -> Res.string.color_light_gray
        Color.Cyan -> Res.string.color_cyan
        Color.Magenta -> Res.string.color_magenta
        Color.Yellow -> Res.string.color_yellow
        else -> null
    }
}

@Composable
fun Color.toName(includeHex: Boolean = false): String {
    val color = this
    val resource = color.toNameResource()

    return buildString {
        if (resource != null) {
            append(stringResource(resource))
            if (includeHex) {
                append(" (")
                append(color.toHex())
                append(")")
            }
        } else {
            append(color.toHex())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseColorPickerField(
    value: Color?,
    onValueChange: (Color?) -> Unit,
    content: @Composable (
        thumbnail: @Composable (() -> Unit)?,
        text: String,
        onClick: () -> Unit
    ) -> Unit
) {
    val scope = rememberCoroutineScope()

    var showColorPickerDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    //TODO: allow custom hex input and more
    content(
        if (value != null) {
            {
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(value)
                )
            }
        } else null,
        value?.toName(includeHex = true) ?: "",
        {
            showColorPickerDialog = true
        }
    )

    if (showColorPickerDialog) {
        ModalBottomSheet(
            onDismissRequest = { showColorPickerDialog = false },
            sheetState = sheetState
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(46.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                items(PREDEFINED_COLORS) { color ->
                    val isSelected = color == value

                    val tint = if(color.luminance() > 0.5f) Color.Black else Color.White

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        BorderStroke(4.dp, tint),
                                        CircleShape
                                    )
                                } else Modifier
                            )
                            .clickable {
                                onValueChange(color)
                                scope.launch {
                                    sheetState.hide()
                                    showColorPickerDialog = false
                                }
                            }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutlinedColorPickerField(
    value: Color?,
    onValueChange: (Color?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) = BaseColorPickerField(
    value = value,
    onValueChange = onValueChange
) { t, text, onClick ->
    Box {
        OutlinedTextField(
            value = text,
            modifier = modifier
                .fillMaxWidth(),
            enabled = true,
            readOnly = true,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = t ?: leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            shape = shape,
            colors = colors,
            onValueChange = { }
        )

        Box(
            modifier = Modifier.matchParentSize().clickable { onClick() }
        )
    }
}

@Composable
fun ColorPickerField(
    value: Color?,
    onValueChange: (Color?) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) = BaseColorPickerField(
    value = value,
    onValueChange = onValueChange
) { t, text, onClick ->
    Box {
        TextField(
            value = text,
            modifier = modifier
                .fillMaxWidth(),
            enabled = true,
            readOnly = true,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = t ?: leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            shape = shape,
            colors = colors,
            onValueChange = { }
        )

        Box(
            modifier = Modifier.matchParentSize().clickable { onClick() }
        )
    }
}
