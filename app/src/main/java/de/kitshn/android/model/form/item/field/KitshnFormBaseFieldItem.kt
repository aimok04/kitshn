package de.kitshn.android.model.form.item.field

import androidx.compose.runtime.Composable
import de.kitshn.android.model.form.item.KitshnFormBaseItem

abstract class KitshnFormBaseFieldItem(
    val label: @Composable () -> Unit,
    val placeholder: @Composable (() -> Unit)? = null,
    val leadingIcon: @Composable (() -> Unit)? = null,
    val trailingIcon: @Composable (() -> Unit)? = null,
    val prefix: @Composable (() -> Unit)? = null,
    val suffix: @Composable (() -> Unit)? = null,
    val optional: Boolean = false
) : KitshnFormBaseItem()