package de.kitshn.android.homepage.model

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class HomePage(
    val sections: MutableList<HomePageSection>,
    val validUntil: Long
) {
    @Transient
    val sectionsStateList = mutableStateListOf<HomePageSection>()
}