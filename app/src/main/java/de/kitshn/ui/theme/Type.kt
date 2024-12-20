package de.kitshn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import de.kitshn.R

val playfairDisplay = FontFamily(
    Font(R.font.playfairdisplay_regular, FontWeight.Normal),
    Font(R.font.playfairdisplay_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.playfairdisplay_medium, FontWeight.Medium),
    Font(R.font.playfairdisplay_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.playfairdisplay_bold, FontWeight.Bold),
    Font(R.font.playfairdisplay_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.playfairdisplay_semibold, FontWeight.SemiBold),
    Font(R.font.playfairdisplay_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.playfairdisplay_extrabold, FontWeight.ExtraBold),
    Font(R.font.playfairdisplay_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.playfairdisplay_black, FontWeight.Black),
    Font(R.font.playfairdisplay_blackitalic, FontWeight.Black, FontStyle.Italic),
)

val Typography = Typography(
    displayMedium = Typography().displayMedium.copy(
        fontFamily = playfairDisplay
    ),
    displaySmall = Typography().displaySmall.copy(
        fontFamily = playfairDisplay
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = playfairDisplay
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = playfairDisplay
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontFamily = playfairDisplay
    )
)