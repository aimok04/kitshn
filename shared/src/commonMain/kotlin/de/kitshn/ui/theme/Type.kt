package de.kitshn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.playfairdisplay_black
import kitshn.shared.generated.resources.playfairdisplay_blackitalic
import kitshn.shared.generated.resources.playfairdisplay_bold
import kitshn.shared.generated.resources.playfairdisplay_bolditalic
import kitshn.shared.generated.resources.playfairdisplay_extrabold
import kitshn.shared.generated.resources.playfairdisplay_extrabolditalic
import kitshn.shared.generated.resources.playfairdisplay_italic
import kitshn.shared.generated.resources.playfairdisplay_medium
import kitshn.shared.generated.resources.playfairdisplay_mediumitalic
import kitshn.shared.generated.resources.playfairdisplay_regular
import kitshn.shared.generated.resources.playfairdisplay_semibold
import kitshn.shared.generated.resources.playfairdisplay_semibolditalic
import org.jetbrains.compose.resources.Font

@Composable
fun playfairDisplay() = FontFamily(
    Font(Res.font.playfairdisplay_regular, FontWeight.Normal),
    Font(Res.font.playfairdisplay_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.playfairdisplay_medium, FontWeight.Medium),
    Font(Res.font.playfairdisplay_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.playfairdisplay_bold, FontWeight.Bold),
    Font(Res.font.playfairdisplay_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.playfairdisplay_semibold, FontWeight.SemiBold),
    Font(Res.font.playfairdisplay_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.playfairdisplay_extrabold, FontWeight.ExtraBold),
    Font(Res.font.playfairdisplay_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.playfairdisplay_black, FontWeight.Black),
    Font(Res.font.playfairdisplay_blackitalic, FontWeight.Black, FontStyle.Italic),
)

@Composable
fun Typography(): Typography {
    val fontFamily = playfairDisplay()

    return Typography(
        displayMedium = Typography().displayMedium.copy(
            fontFamily = fontFamily
        ),
        displaySmall = Typography().displaySmall.copy(
            fontFamily = fontFamily
        ),
        titleLarge = Typography().titleLarge.copy(
            fontFamily = fontFamily
        ),
        titleMedium = Typography().titleMedium.copy(
            fontFamily = fontFamily
        ),
        headlineSmall = Typography().headlineSmall.copy(
            fontFamily = fontFamily
        )
    )
}