package com.dailyenglish.nativeapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * Segmented choice that marks the selection with a solid ink capsule instead
 * of a checkmark. In light mode this is a black pill, in dark mode a white pill,
 * because the pill follows onSurface/surface.
 */
@Composable
fun SingleChoiceSegmentedButtonRowScope.AppSegmentedButton(
  selected: Boolean,
  onClick: () -> Unit,
  shape: Shape,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  label: @Composable () -> Unit,
) {
  SegmentedButton(
    selected = selected,
    onClick = onClick,
    shape = shape,
    modifier = modifier,
    enabled = enabled,
    colors = SegmentedButtonDefaults.colors(
      activeContainerColor = MaterialTheme.colorScheme.onSurface,
      activeContentColor = MaterialTheme.colorScheme.surface,
      inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
    icon = {},
    label = label,
  )
}
