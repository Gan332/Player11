package com.musicplayer.melodex.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * 从种子颜色生成 Material 3 浅色 ColorScheme。
 */
fun generateLightColorScheme(seed: Color): androidx.compose.material3.ColorScheme {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (seed.red * 255).toInt(),
        (seed.green * 255).toInt(),
        (seed.blue * 255).toInt(),
        hsv
    )
    val h = hsv[0]
    val s = hsv[1]

    val primary = Color.hsv(h, max(s, 0.4f), 0.40f)
    val onPrimary = Color.White
    val primaryContainer = Color.hsv(h, s * 0.7f, 0.90f)
    val onPrimaryContainer = Color.hsv(h, s * 0.8f, 0.10f)

    val secondary = Color.hsv((h + 40f) % 360f, min(s, 0.5f) * 0.9f, 0.45f)
    val onSecondary = Color.White
    val secondaryContainer = Color.hsv((h + 40f) % 360f, min(s, 0.5f) * 0.5f, 0.90f)
    val onSecondaryContainer = Color.hsv((h + 40f) % 360f, min(s, 0.5f) * 0.8f, 0.10f)

    val tertiary = Color.hsv((h + 60f) % 360f, min(s, 0.6f), 0.45f)
    val onTertiary = Color.White
    val tertiaryContainer = Color.hsv((h + 60f) % 360f, min(s, 0.6f) * 0.5f, 0.90f)
    val onTertiaryContainer = Color.hsv((h + 60f) % 360f, min(s, 0.6f) * 0.8f, 0.10f)

    val error = Color.hsv(0f, 0.75f, 0.40f)
    val onError = Color.White
    val errorContainer = Color.hsv(0f, 0.75f * 0.5f, 0.90f)
    val onErrorContainer = Color.hsv(0f, 0.75f * 0.8f, 0.10f)

    val background = Color.hsv(h, s * 0.05f, 0.98f)
    val onBackground = Color.hsv(h, s * 0.1f, 0.10f)
    val surface = Color.hsv(h, s * 0.05f, 0.98f)
    val onSurface = Color.hsv(h, s * 0.1f, 0.10f)
    val surfaceVariant = Color.hsv(h, s * 0.1f, 0.90f)
    val onSurfaceVariant = Color.hsv(h, s * 0.2f, 0.30f)
    val outline = Color.hsv(h, s * 0.1f, 0.55f)
    val outlineVariant = Color.hsv(h, s * 0.05f, 0.80f)
    val inverseSurface = Color.hsv(h, s * 0.1f, 0.20f)
    val inverseOnSurface = Color.hsv(h, s * 0.05f, 0.95f)
    val inversePrimary = Color.hsv(h, max(s, 0.4f), 0.80f)

    return lightColorScheme(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
        secondary = secondary, onSecondary = onSecondary,
        secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary, onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
        error = error, onError = onError,
        errorContainer = errorContainer, onErrorContainer = onErrorContainer,
        background = background, onBackground = onBackground,
        surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
        outline = outline, outlineVariant = outlineVariant,
        inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary
    )
}

/**
 * 从种子颜色生成 Material 3 深色 ColorScheme。
 */
fun generateDarkColorScheme(seed: Color): androidx.compose.material3.ColorScheme {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (seed.red * 255).toInt(),
        (seed.green * 255).toInt(),
        (seed.blue * 255).toInt(),
        hsv
    )
    val h = hsv[0]
    val s = hsv[1]

    val primary = Color.hsv(h, max(s, 0.4f), 0.80f)
    val onPrimary = Color.hsv(h, max(s, 0.4f) * 0.5f, 0.10f)
    val primaryContainer = Color.hsv(h, max(s, 0.4f) * 0.7f, 0.30f)
    val onPrimaryContainer = Color.hsv(h, max(s, 0.4f), 0.90f)

    val secondary = Color.hsv((h + 40f) % 360f, min(s, 0.5f) * 0.9f, 0.80f)
    val onSecondary = Color.hsv((h + 40f) % 360f, min(s, 0.5f) * 0.5f, 0.10f)
    val secondaryContainer = Color.hsv((h + 40f) % 360f, min(s, 0.5f) * 0.5f, 0.30f)
    val onSecondaryContainer = Color.hsv((h + 40f) % 360f, min(s, 0.5f), 0.90f)

    val tertiary = Color.hsv((h + 60f) % 360f, min(s, 0.6f), 0.80f)
    val onTertiary = Color.hsv((h + 60f) % 360f, min(s, 0.6f) * 0.5f, 0.10f)
    val tertiaryContainer = Color.hsv((h + 60f) % 360f, min(s, 0.6f) * 0.5f, 0.30f)
    val onTertiaryContainer = Color.hsv((h + 60f) % 360f, min(s, 0.6f), 0.90f)

    val error = Color.hsv(0f, 0.75f, 0.80f)
    val onError = Color.hsv(0f, 0.75f * 0.5f, 0.10f)
    val errorContainer = Color.hsv(0f, 0.75f * 0.5f, 0.30f)
    val onErrorContainer = Color.hsv(0f, 0.75f, 0.90f)

    val background = Color.hsv(h, s * 0.05f, 0.10f)
    val onBackground = Color.hsv(h, s * 0.05f, 0.90f)
    val surface = Color.hsv(h, s * 0.05f, 0.10f)
    val onSurface = Color.hsv(h, s * 0.05f, 0.90f)
    val surfaceVariant = Color.hsv(h, s * 0.1f, 0.30f)
    val onSurfaceVariant = Color.hsv(h, s * 0.1f, 0.80f)
    val outline = Color.hsv(h, s * 0.1f, 0.60f)
    val outlineVariant = Color.hsv(h, s * 0.05f, 0.30f)
    val inverseSurface = Color.hsv(h, s * 0.05f, 0.90f)
    val inverseOnSurface = Color.hsv(h, s * 0.1f, 0.10f)
    val inversePrimary = Color.hsv(h, max(s, 0.4f), 0.40f)

    return darkColorScheme(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
        secondary = secondary, onSecondary = onSecondary,
        secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary, onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
        error = error, onError = onError,
        errorContainer = errorContainer, onErrorContainer = onErrorContainer,
        background = background, onBackground = onBackground,
        surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
        outline = outline, outlineVariant = outlineVariant,
        inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary
    )
}