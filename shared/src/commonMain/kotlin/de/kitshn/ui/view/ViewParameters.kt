package de.kitshn.ui.view

import de.kitshn.KitshnViewModel

class ViewParameters(
    val vm: KitshnViewModel,
    val back: (() -> Unit)?
)