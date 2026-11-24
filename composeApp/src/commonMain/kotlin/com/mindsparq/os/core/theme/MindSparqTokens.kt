package com.mindsparq.os.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Immutable
data class MindSparqColors(
    val bgCanvas: Color,
    val bgSurface: Color,
    val bgElevated: Color,
    val borderHairline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusDanger: Color,
    val isDark: Boolean,
)

private val LightSemanticColors = MindSparqColors(
    bgCanvas = Color(0xFFF8FAFC),
    bgSurface = Color(0xFFFFFFFF),
    bgElevated = Color(0xFFF1F5F9),
    borderHairline = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    accentPrimary = Color(0xFF2563EB),
    accentSecondary = Color(0xFF0D9488),
    statusSuccess = Color(0xFF16A34A),
    statusWarning = Color(0xFFD97706),
    statusDanger = Color(0xFFDC2626),
    isDark = false,
)

private val DarkSemanticColors = MindSparqColors(
    bgCanvas = Color(0xFF0B0F17),
    bgSurface = Color(0xFF131A26),
    bgElevated = Color(0xFF1C2536),
    borderHairline = Color(0xFF1E293B),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    accentPrimary = Color(0xFF3B82F6),
    accentSecondary = Color(0xFF14B8A6),
    statusSuccess = Color(0xFF22C55E),
    statusWarning = Color(0xFFF59E0B),
    statusDanger = Color(0xFFEF4444),
    isDark = true,
)

val LocalMindSparqColors = staticCompositionLocalOf { LightSemanticColors }

private val LightMaterialColors = lightColorScheme(
    primary = LightSemanticColors.accentPrimary,
    onPrimary = Color.White,
    secondary = LightSemanticColors.accentSecondary,
    onSecondary = Color.White,
    background = LightSemanticColors.bgCanvas,
    onBackground = LightSemanticColors.textPrimary,
    surface = LightSemanticColors.bgSurface,
    onSurface = LightSemanticColors.textPrimary,
    surfaceVariant = LightSemanticColors.bgElevated,
    onSurfaceVariant = LightSemanticColors.textSecondary,
    outline = LightSemanticColors.borderHairline,
    error = LightSemanticColors.statusDanger,
)

private val DarkMaterialColors = darkColorScheme(
    primary = DarkSemanticColors.accentPrimary,
    onPrimary = Color.White,
    secondary = DarkSemanticColors.accentSecondary,
    onSecondary = Color(0xFF042F2E),
    background = DarkSemanticColors.bgCanvas,
    onBackground = DarkSemanticColors.textPrimary,
    surface = DarkSemanticColors.bgSurface,
    onSurface = DarkSemanticColors.textPrimary,
    surfaceVariant = DarkSemanticColors.bgElevated,
    onSurfaceVariant = DarkSemanticColors.textSecondary,
    outline = DarkSemanticColors.borderHairline,
    error = DarkSemanticColors.statusDanger,
)

val MindSparqTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontFeatureSettings = "tnum",
    ),
)

val MindSparqShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

@Composable
fun MindSparqTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val semantic = if (dark) DarkSemanticColors else LightSemanticColors
    val material = if (dark) DarkMaterialColors else LightMaterialColors

    CompositionLocalProvider(LocalMindSparqColors provides semantic) {
        MaterialTheme(
            colorScheme = material,
            typography = MindSparqTypography,
            shapes = MindSparqShapes,
            content = content,
        )
    }
}
