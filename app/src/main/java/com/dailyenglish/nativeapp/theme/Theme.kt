package com.dailyenglish.nativeapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AccentBlue = Color(0xFF2F6BFF)

/** The white baseline keeps one user-selectable accent; the neutral roles never change. */
enum class AccentChoice(
  val key: String,
  val label: String,
  val light: Color,
  val lightContainer: Color,
  val onLightContainer: Color,
  val dark: Color,
  val darkContainer: Color,
  val onDarkContainer: Color,
) {
  Blue(
    "blue", "蓝色",
    Color(0xFF2F6BFF), Color(0xFFE8F0FF), Color(0xFF0B2A66),
    Color(0xFF7AA2FF), Color(0xFF163A75), Color(0xFFD9E5FF),
  ),
  Green(
    "green", "绿色",
    Color(0xFF0F8A54), Color(0xFFE2F4EB), Color(0xFF073A24),
    Color(0xFF67D7A4), Color(0xFF103B29), Color(0xFFC8F3DC),
  ),
  Graphite(
    "graphite", "石墨",
    Color(0xFF3A3D44), Color(0xFFEFF1F4), Color(0xFF111318),
    Color(0xFFD5D9E0), Color(0xFF2B2F36), Color(0xFFF4F6F9),
  ),
  Amber(
    "amber", "琥珀",
    Color(0xFFB26A00), Color(0xFFFFF1DC), Color(0xFF4A2800),
    Color(0xFFF4B860), Color(0xFF4A3210), Color(0xFFFFE3B8),
  );

  companion object {
    fun fromKey(key: String?): AccentChoice = entries.firstOrNull { it.key == key } ?: Blue
  }
}

private val LightColors = lightColorScheme(
  primary = AccentBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE8F0FF),
  onPrimaryContainer = Color(0xFF0B2A66),
  secondary = Color(0xFF111318),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF1F3F6),
  onSecondaryContainer = Color(0xFF111318),
  tertiary = Color(0xFF5F6672),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFEFF2F7),
  onTertiaryContainer = Color(0xFF1F2329),
  background = Color(0xFFF6F7F9),
  onBackground = Color(0xFF0B0C0E),
  surface = Color.White,
  onSurface = Color(0xFF0B0C0E),
  surfaceVariant = Color(0xFFF1F3F6),
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = Color(0xFFFBFCFD),
  surfaceContainer = Color(0xFFF5F7FA),
  surfaceContainerHigh = Color(0xFFEFF2F6),
  surfaceContainerHighest = Color(0xFFE9EDF2),
  surfaceBright = Color.White,
  surfaceDim = Color(0xFFE7EBF0),
  onSurfaceVariant = Color(0xFF5F6672),
  outline = Color(0xFF9AA1AB),
  outlineVariant = Color(0xFFE3E7EC),
  error = Color(0xFFD93025),
  onError = Color.White,
  errorContainer = Color(0xFFFFEDEA),
  onErrorContainer = Color(0xFF5F150E),
  inverseSurface = Color(0xFF1B1D21),
  inverseOnSurface = Color(0xFFF4F6F9),
  inversePrimary = Color(0xFF9FBEFF),
  scrim = Color.Black,
)

private val DarkColors = darkColorScheme(
  primary = Color(0xFF7AA2FF),
  onPrimary = Color(0xFF001A44),
  primaryContainer = Color(0xFF163A75),
  onPrimaryContainer = Color(0xFFD9E5FF),
  secondary = Color(0xFFE7EAF0),
  onSecondary = Color(0xFF111318),
  secondaryContainer = Color(0xFF1B1F26),
  onSecondaryContainer = Color(0xFFE7EAF0),
  tertiary = Color(0xFFB8C0CC),
  onTertiary = Color(0xFF1B1F26),
  tertiaryContainer = Color(0xFF252B35),
  onTertiaryContainer = Color(0xFFE7EAF0),
  background = Color(0xFF08090B),
  onBackground = Color(0xFFF4F6F9),
  surface = Color(0xFF111318),
  onSurface = Color(0xFFF4F6F9),
  surfaceVariant = Color(0xFF1B1F26),
  surfaceContainerLowest = Color(0xFF0C0E11),
  surfaceContainerLow = Color(0xFF14171C),
  surfaceContainer = Color(0xFF181B21),
  surfaceContainerHigh = Color(0xFF1E2229),
  surfaceContainerHighest = Color(0xFF252A32),
  surfaceBright = Color(0xFF252A32),
  surfaceDim = Color(0xFF0C0E11),
  onSurfaceVariant = Color(0xFFAAB2BE),
  outline = Color(0xFF7C8490),
  outlineVariant = Color(0xFF2B3038),
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6),
  inverseSurface = Color(0xFFE7EAF0),
  inverseOnSurface = Color(0xFF1B1D21),
  inversePrimary = Color(0xFF2F6BFF),
  scrim = Color.Black,
)

private val AppShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun DailyEnglishTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  accent: AccentChoice = AccentChoice.Blue,
  content: @Composable () -> Unit,
) {
  val base = if (darkTheme) DarkColors else LightColors
  val scheme = base.copy(
    primary = if (darkTheme) accent.dark else accent.light,
    onPrimary = if (darkTheme) Color(0xFF071018) else Color.White,
    primaryContainer = if (darkTheme) accent.darkContainer else accent.lightContainer,
    onPrimaryContainer = if (darkTheme) accent.onDarkContainer else accent.onLightContainer,
    inversePrimary = if (darkTheme) accent.light else accent.dark,
  )
  MaterialTheme(
    colorScheme = scheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
