package com.basecalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.basecalc.ColorMode

// ─── Paleta OLED (Modo Cola Discreta) ───────────────────────────────────────────

private val CoresOled = darkColorScheme(
    primary = Color(0xFF555555),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF222222),
    onPrimaryContainer = Color(0xFFCCCCCC),
    secondary = Color(0xFF666666),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A1A1A),
    onSecondaryContainer = Color(0xFFBBBBBB),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF111111),
    onSurface = Color(0xFFAAAAAA),
    onSurfaceVariant = Color(0xFF888888),
    background = Color.Black,
    onBackground = Color(0xFFCCCCCC),
    error = Color(0xFFFF6B6B),
    onError = Color.Black,
)

// ─── Typography para Modo Discreta ───────────────────────────────────────────────

private val TypographyDiscreta = Typography(
    headlineMedium = TextStyle(fontSize = 24.sp),
    titleLarge = TextStyle(fontSize = 13.sp),
    titleMedium = TextStyle(fontSize = 12.sp),
    bodyMedium = TextStyle(fontSize = 10.sp),
    bodySmall = TextStyle(fontSize = 9.sp),
    labelSmall = TextStyle(fontSize = 8.sp),
    labelMedium = TextStyle(fontSize = 9.sp),
    labelLarge = TextStyle(fontSize = 10.sp),
)

// ─── Paleta clara ─────────────────────────────────────────────────────────────

private val CoresClaro = lightColorScheme(
    primary = Color(0xFF135D8F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE5FF),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF5C4438),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF22100A),
    surface = Color(0xFFF6F6F4),
    surfaceVariant = Color(0xFFE6E6E4),
    onSurface = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFF444444),
    background = Color(0xFFF2F2F0),
    onBackground = Color(0xFF1C1C1C),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

// ─── Paleta escura ────────────────────────────────────────────────────────────

private val CoresEscuro = darkColorScheme(
    primary = Color(0xFF8EC7FF),
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF004478),
    onPrimaryContainer = Color(0xFFCFE5FF),
    secondary = Color(0xFFE1B383),
    onSecondary = Color(0xFF3B2500),
    secondaryContainer = Color(0xFF553800),
    onSecondaryContainer = Color(0xFFFFDDB4),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFFBBBBBB),
    background = Color(0xFF141414),
    onBackground = Color(0xFFEDEDED),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF690005),
)

private val CoresClaroAltoContraste = lightColorScheme(
    primary = Color(0xFF0B0B0B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDADADA),
    onPrimaryContainer = Color(0xFF0B0B0B),
    secondary = Color(0xFF0B0B0B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = Color(0xFF0B0B0B),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF1A1A1A),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    error = Color(0xFFB00020),
    onError = Color.White,
)

private val CoresEscuroAltoContraste = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1C1C1C),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF2B2B2B),
    onSecondaryContainer = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    surfaceVariant = Color(0xFF1B1B1B),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFE0E0E0),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF000000),
)

private val CoresClaroDaltonismo = lightColorScheme(
    primary = Color(0xFF0072B2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE9FF),
    onPrimaryContainer = Color(0xFF003552),
    secondary = Color(0xFFD55E00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD7B3),
    onSecondaryContainer = Color(0xFF3A1F00),
    surface = Color(0xFFF8F8F8),
    surfaceVariant = Color(0xFFEDEDED),
    onSurface = Color(0xFF141414),
    onSurfaceVariant = Color(0xFF3A3A3A),
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF141414),
    error = Color(0xFFB00020),
    onError = Color.White,
)

private val CoresEscuroDaltonismo = darkColorScheme(
    primary = Color(0xFF56B4E9),
    onPrimary = Color(0xFF00324A),
    primaryContainer = Color(0xFF004C6D),
    onPrimaryContainer = Color(0xFFCFEFFF),
    secondary = Color(0xFFE69F00),
    onSecondary = Color(0xFF3D2B00),
    secondaryContainer = Color(0xFF5A3F00),
    onSecondaryContainer = Color(0xFFFFE4B5),
    surface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurface = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFFCFCFCF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF0F0F0),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF3D0000),
)

@Composable
fun BaseCalcTheme(
    colorMode: ColorMode = ColorMode.PADRAO,
    modoDiscreta: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    
    val scheme = when {
        modoDiscreta -> CoresOled
        colorMode == ColorMode.PADRAO -> if (dark) CoresEscuro else CoresClaro
        colorMode == ColorMode.ALTO_CONTRASTE -> if (dark) CoresEscuroAltoContraste else CoresClaroAltoContraste
        colorMode == ColorMode.DALTONISMO -> if (dark) CoresEscuroDaltonismo else CoresClaroDaltonismo
        else -> if (dark) CoresEscuro else CoresClaro
    }

    val typography = if (modoDiscreta) TypographyDiscreta else Typography

    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        content = content,
    )
}
