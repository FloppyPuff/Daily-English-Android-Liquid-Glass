package com.dailyenglish.nativeapp.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dailyenglish.nativeapp.ui.components.LiquidGlassCircleButton
import com.dailyenglish.nativeapp.ui.components.LiquidGlassPillButton

/** Liquid glass circular action used by the top bar. */
@Composable
fun FlatCircleButton(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
) {
  LiquidGlassCircleButton(
    onClick = onClick,
    icon = icon,
    contentDescription = contentDescription,
    modifier = modifier,
    size = size,
  )
}

/** Liquid glass pill action used by the focus timer and similar controls. */
@Composable
fun FlatPillButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
  content: @Composable RowScope.() -> Unit,
) {
  LiquidGlassPillButton(
    onClick = onClick,
    modifier = modifier,
    contentPadding = contentPadding,
    content = content,
  )
}
