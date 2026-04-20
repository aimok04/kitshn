package de.kitshn.ui

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.RoundedPolygon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun MaterialShapes.Companion.randomBackgroundShape(): RoundedPolygon {
    return listOf(
        Square,
        Slanted,
        Oval,
        Pill,
        Pentagon,
        Gem,
        Sunny,
        VerySunny,
        Cookie4Sided,
        Cookie6Sided,
        Cookie7Sided,
        Cookie9Sided,
        Cookie12Sided,
        Clover4Leaf,
        Clover8Leaf,
        SoftBurst,
        SoftBurst,
        SoftBoom,
        Flower,
        PixelCircle,
    ).random()
}