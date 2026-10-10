package com.fyp.mobileaccessories.stockcontrol.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────
// COLOR PALETTE — Dynamic per module
// ─────────────────────────────────────────────
object AppColors {
    // Primary palette
    var Primary: Color = Color(0xFF7C3AED)          // Default Royal Purple
    var PrimaryVariant: Color = Color(0xFF6D28D9)
    var PrimaryContainer: Color = Color(0xFFEDE9FE)

    // Secondary
    var Secondary: Color = Color(0xFF4F46E5)
    var SecondaryContainer: Color = Color(0xFFE0E7FF)

    // Backgrounds
    var Background: Color = Color(0xFFFAF5FF)
    var Surface: Color = Color(0xFFFFFFFF)
    var SurfaceVariant: Color = Color(0xFFF3E8FF)

    // Status colors
    val Success = Color(0xFF059669)
    val SuccessContainer = Color(0xFFD1FAE5)
    val Warning = Color(0xFFD97706)
    val WarningContainer = Color(0xFFFEF3C7)
    val Error = Color(0xFFDC2626)
    val ErrorContainer = Color(0xFFFEE2E2)
    val Info = Color(0xFF0284C7)
    val InfoContainer = Color(0xFFE0F2FE)

    // Text
    val OnPrimary = Color(0xFFFFFFFF)
    var OnBackground = Color(0xFF0F172A)
    var OnSurface = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val TextTertiary = Color(0xFF94A3B8)
    val Divider = Color(0xFFE2E8F0)

    // Dark theme colors
    val PrimaryDark = Color(0xFF4F78E8)
    val BackgroundDark = Color(0xFF0F172A)
    val SurfaceDark = Color(0xFF1E293B)
    val SurfaceVariantDark = Color(0xFF334155)
    val OnSurfaceDark = Color(0xFFE2E8F0)

    // Role badge colors
    val AdminBadge = Color(0xFF059669)
    val AdminBadgeContainer = Color(0xFFD1FAE5)
    val ModBadge = Color(0xFFD97706)
    val ModBadgeContainer = Color(0xFFFEF3C7)
    val UserBadge = Color(0xFF7C3AED)
    val UserBadgeContainer = Color(0xFFEDE9FE)

    // Card gradients
    var CardGradientStart: Color = Color(0xFF7C3AED)
    var CardGradientEnd: Color = Color(0xFF4F46E5)

    fun applyAdminPalette() {
        Primary = Color(0xFF059669)          // Greenish Primary
        PrimaryVariant = Color(0xFF047857)
        PrimaryContainer = Color(0xFFD1FAE5)
        Secondary = Color(0xFF0D9488)
        SecondaryContainer = Color(0xFFCCFBF1)
        Background = Color(0xFFF0FDF4)
        SurfaceVariant = Color(0xFFDCFCE7)
        OnBackground = Color(0xFF064E3B)
        OnSurface = Color(0xFF065F46)
        CardGradientStart = Color(0xFF059669)
        CardGradientEnd = Color(0xFF0D9488)
    }

    fun applyModeratorPalette() {
        Primary = Color(0xFFEA580C)          // Orange Primary
        PrimaryVariant = Color(0xFFC2410C)
        PrimaryContainer = Color(0xFFFEF3C7)
        Secondary = Color(0xFFD97706)
        SecondaryContainer = Color(0xFFFEF3C7)
        Background = Color(0xFFFFFBEB)
        SurfaceVariant = Color(0xFFFEF3C7)
        OnBackground = Color(0xFF7C2D12)
        OnSurface = Color(0xFF9A3412)
        CardGradientStart = Color(0xFFEA580C)
        CardGradientEnd = Color(0xFFD97706)
    }

    fun applyUserPalette() {
        Primary = Color(0xFF7C3AED)          // Purple Primary
        PrimaryVariant = Color(0xFF6D28D9)
        PrimaryContainer = Color(0xFFEDE9FE)
        Secondary = Color(0xFF4F46E5)
        SecondaryContainer = Color(0xFFE0E7FF)
        Background = Color(0xFFFAF5FF)
        SurfaceVariant = Color(0xFFF3E8FF)
        OnBackground = Color(0xFF3B0764)
        OnSurface = Color(0xFF5B21B6)
        CardGradientStart = Color(0xFF7C3AED)
        CardGradientEnd = Color(0xFF4F46E5)
    }
}

// ─────────────────────────────────────────────
// APP TYPOGRAPHY — clean, modern font scales
// ─────────────────────────────────────────────
val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
)

@Composable
fun MobileAccessoriesFYPTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    UserTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun AdminTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    AppColors.applyAdminPalette()

    val adminColorScheme = lightColorScheme(
        primary = AppColors.Primary,
        onPrimary = Color.White,
        primaryContainer = AppColors.PrimaryContainer,
        onPrimaryContainer = AppColors.PrimaryVariant,
        secondary = AppColors.Secondary,
        background = AppColors.Background,
        surface = AppColors.Surface,
        surfaceVariant = AppColors.SurfaceVariant,
        onBackground = AppColors.OnBackground,
        onSurface = AppColors.OnSurface,
        error = AppColors.Error
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = adminColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = adminColorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun ModeratorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    AppColors.applyModeratorPalette()

    val modColorScheme = lightColorScheme(
        primary = AppColors.Primary,
        onPrimary = Color.White,
        primaryContainer = AppColors.PrimaryContainer,
        onPrimaryContainer = AppColors.PrimaryVariant,
        secondary = AppColors.Secondary,
        background = AppColors.Background,
        surface = AppColors.Surface,
        surfaceVariant = AppColors.SurfaceVariant,
        onBackground = AppColors.OnBackground,
        onSurface = AppColors.OnSurface,
        error = AppColors.Error
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = modColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = modColorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun UserTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    AppColors.applyUserPalette()

    val userColorScheme = lightColorScheme(
        primary = AppColors.Primary,
        onPrimary = Color.White,
        primaryContainer = AppColors.PrimaryContainer,
        onPrimaryContainer = AppColors.PrimaryVariant,
        secondary = AppColors.Secondary,
        background = AppColors.Background,
        surface = AppColors.Surface,
        surfaceVariant = AppColors.SurfaceVariant,
        onBackground = AppColors.OnBackground,
        onSurface = AppColors.OnSurface,
        error = AppColors.Error
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = userColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = userColorScheme,
        typography = AppTypography,
        content = content
    )
}
