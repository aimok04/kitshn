package de.kitshn.android.ui.view

import de.kitshn.android.KitshnViewModel

class ViewParameters(
    val vm: KitshnViewModel,
    val back: (() -> Unit)?
)