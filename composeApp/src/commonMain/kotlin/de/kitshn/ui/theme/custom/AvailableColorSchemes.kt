package de.kitshn.ui.theme.custom

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import de.kitshn.platformDetails

val RED_COLOR_SCHEME_SEED = Color(0xFFA83900)
val LIGHT_RED_COLOR_SCHEME_SEED = Color(0xFF904A41)
val MAGENTA_COLOR_SCHEME_SEED = Color(0xFF884A69)
val LILA_COLOR_SCHEME_SEED = Color(0xFF68548E)
val BLUE_COLOR_SCHEME_SEED = Color(0xFF4150AF)
val TEAL_COLOR_SCHEME_SEED = Color(0xFF006876)
val GREEN_COLOR_SCHEME_SEED = Color(0xFF39693B)
val OLIVE_COLOR_SCHEME_SEED = Color(0xFF5C631D)

enum class AvailableColorSchemes(
    val isAvailable: () -> Boolean,
    val preview: @Composable (customSeed: Color?) -> List<Color>,
    val generate: @Composable (isDark: Boolean, customSeed: Color) -> ColorScheme?
) {
    ANDROID_DYNAMIC_COLOR_SCHEME(
        isAvailable = { isAndroidDynamicColorSchemeAvailable() },
        preview = { generateAndroidDynamicColorSchemePreview() },
        generate = { isDark, _ -> generateAndroidDynamicColorScheme(isDark) }
    ),
    DEFAULT(
        isAvailable = { true },
        preview = { listOf(defaultDarkColorScheme.primary, defaultLightColorScheme.primary) },
        generate = { isDark, _ -> if(isDark) defaultDarkColorScheme else defaultLightColorScheme }
    ),
    CUPERTINO(
        isAvailable = { platformDetails.platform == de.kitshn.Platforms.IOS },
        preview = { listOf(cupertinoDarkColorScheme.primary, cupertinoLightColorScheme.primary) },
        generate = { isDark, _ -> if(isDark) cupertinoDarkColorScheme else cupertinoLightColorScheme }
    ),
    RED(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(RED_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                RED_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    LIGHT_RED(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(LIGHT_RED_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                LIGHT_RED_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    MAGENTA(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(MAGENTA_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                MAGENTA_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    LILA(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(LILA_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                LILA_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    BLUE(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(BLUE_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                BLUE_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    TEAL(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(TEAL_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                TEAL_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    GREEN(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(GREEN_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                GREEN_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    OLIVE(
        isAvailable = { true },
        preview = { generateColorSchemePreviewFromSeedColor(OLIVE_COLOR_SCHEME_SEED) },
        generate = { isDark, _ ->
            generateColorSchemeFromSeedColor(
                OLIVE_COLOR_SCHEME_SEED,
                isDark = isDark
            )
        }
    ),
    CUSTOM(
        isAvailable = { true },
        preview = {
            listOf(
                rememberDynamicColorScheme(
                    seedColor = it!!,
                    isDark = true,
                    isAmoled = false
                ).primary,
                rememberDynamicColorScheme(seedColor = it, isDark = false, isAmoled = false).primary
            )
        },
        generate = { isDark, customSeed ->
            rememberDynamicColorScheme(seedColor = customSeed, isDark = isDark, isAmoled = false)
        }
    );

    companion object {
        fun getDefault(): AvailableColorSchemes {
            if(ANDROID_DYNAMIC_COLOR_SCHEME.isAvailable()) return ANDROID_DYNAMIC_COLOR_SCHEME
            return DEFAULT
        }

        fun parse(name: String): AvailableColorSchemes? {
            try {
                return AvailableColorSchemes.valueOf(name)
            } catch(_: Exception) {
            }

            return null
        }
    }
}

@Composable
private fun generateColorSchemeFromSeedColor(seed: Color, isDark: Boolean): ColorScheme {
    return rememberDynamicColorScheme(seed, isDark, false)
}

@Composable
private fun generateColorSchemePreviewFromSeedColor(seed: Color): List<Color> {
    return listOf(
        generateColorSchemeFromSeedColor(seed, true).primary,
        generateColorSchemeFromSeedColor(seed, false).primary
    )
}