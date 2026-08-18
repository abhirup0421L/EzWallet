package com.example.ui.theme
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AmoledPrimaryDark,
    onPrimary = AmoledOnPrimaryDark,
    primaryContainer = AmoledPrimaryContainerDark,
    onPrimaryContainer = AmoledOnPrimaryContainerDark,
    secondary = AmoledSecondaryDark,
    onSecondary = AmoledOnSecondaryDark,
    secondaryContainer = AmoledSecondaryContainerDark,
    onSecondaryContainer = AmoledOnSecondaryContainerDark,
    tertiary = AmoledTertiaryDark,
    onTertiary = AmoledOnTertiaryDark,
    tertiaryContainer = AmoledTertiaryContainerDark,
    onTertiaryContainer = AmoledOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = AnimeSakuraPrimaryLight,
    onPrimary = AnimeOnPrimaryLight,
    primaryContainer = AnimeSakuraContainerLight,
    onPrimaryContainer = AnimeOnSakuraContainerLight,
    secondary = AnimeSkySecondaryLight,
    onSecondary = AnimeOnSecondaryLight,
    secondaryContainer = AnimeSkyContainerLight,
    onSecondaryContainer = AnimeOnSkyContainerLight,
    tertiary = AnimeVioletTertiaryLight,
    onTertiary = AnimeOnTertiaryLight,
    tertiaryContainer = AnimeVioletContainerLight,
    onTertiaryContainer = AnimeOnVioletContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)


@Composable
fun EzWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customBgColor: String? = null,
    customSelectionColor: String? = null,
    customFileColor: String? = null,
    content: @Composable () -> Unit
) {
    val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val bg = customBgColor?.let { try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { null } }
    val sel = customSelectionColor?.let { try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { null } }
    val file = customFileColor?.let { try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { null } }

    val colorScheme = baseColorScheme.copy(
        background = bg ?: baseColorScheme.background,
        surface = bg ?: baseColorScheme.surface,
        primary = sel ?: baseColorScheme.primary,
        surfaceVariant = file ?: baseColorScheme.surfaceVariant,
        primaryContainer = sel?.copy(alpha = 0.2f) ?: baseColorScheme.primaryContainer
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

