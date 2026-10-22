package com.dailyenglish.nativeapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class LiquidTabItem(
  val label: String,
  val icon: ImageVector,
)

/**
 * Floating liquid-glass dock with a large frosted selection lens.
 *
 * The content behind the dock is captured by the caller with
 * rememberLayerBackdrop/layerBackdrop. The selected lens samples the same layer
 * for a stronger blur and chromatic refraction, follows horizontal drags, and
 * snaps to the nearest tab. The page content can scroll underneath.
 */
@Composable
fun LiquidGlassDock(
  tabs: List<LiquidTabItem>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  backdrop: Backdrop,
  modifier: Modifier = Modifier,
) {
  if (tabs.isEmpty()) return
  val dark = isSystemInDarkTheme()
  val accent = MaterialTheme.colorScheme.primary
  val barTint = if (dark) Color(0xFF0B0D11).copy(alpha = 0.26f) else Color.White.copy(alpha = 0.20f)
  val bubbleTint = if (dark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.34f)
  val bubbleAccent = accent.copy(alpha = if (dark) 0.14f else 0.10f)
  val scope = rememberCoroutineScope()
  val lastIndex = tabs.lastIndex
  val initialIndex = selectedIndex.coerceIn(0, lastIndex)
  val position = remember { Animatable(initialIndex.toFloat()) }
  var dragging by remember { mutableStateOf(false) }
  val bubbleScale by animateFloatAsState(
    targetValue = if (dragging) 1.08f else 1f,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
    label = "dock-bubble-scale",
  )

  LaunchedEffect(selectedIndex) {
    if (selectedIndex in tabs.indices) {
      position.animateTo(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
      )
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(78.dp)
      .drawBackdrop(
        backdrop = backdrop,
        shape = { CircleShape },
        effects = {
          vibrancy()
          blur(20.dp.toPx())
          lens(28.dp.toPx(), 24.dp.toPx(), depthEffect = true)
        },
        highlight = { Highlight.Default.copy(alpha = 0.85f) },
        shadow = { Shadow.Default },
        onDrawSurface = { drawRect(barTint) },
      ),
  ) {
    val outerPadding = 6.dp
    val tabWidth = (maxWidth - outerPadding * 2) / tabs.size
    val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(tabs.size, tabWidthPx) {
          detectHorizontalDragGestures(
            onDragStart = { dragging = true },
            onHorizontalDrag = { change, dragAmount ->
              change.consume()
              val next = (position.value + dragAmount / tabWidthPx)
                .coerceIn(0f, lastIndex.toFloat())
              scope.launch { position.snapTo(next) }
            },
            onDragEnd = {
              dragging = false
              val target = position.value.roundToInt().coerceIn(0, lastIndex)
              if (target != selectedIndex) onSelect(target)
              scope.launch {
                position.animateTo(target.toFloat(), spring(dampingRatio = 0.8f, stiffness = 420f))
              }
            },
            onDragCancel = {
              dragging = false
              scope.launch {
                position.animateTo(
                  selectedIndex.coerceIn(0, lastIndex).toFloat(),
                  spring(dampingRatio = 0.8f, stiffness = 420f),
                )
              }
            },
          )
        },
    ) {
      Box(Modifier.fillMaxSize().padding(outerPadding)) {
        if (selectedIndex in tabs.indices) {
          Box(
            Modifier
              .offset(x = tabWidth * position.value)
              .width(tabWidth)
              .fillMaxHeight()
              .graphicsLayer {
                scaleX = bubbleScale
                scaleY = bubbleScale
              }
              .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                  vibrancy()
                  blur(22.dp.toPx())
                  lens(
                    26.dp.toPx(),
                    22.dp.toPx(),
                    depthEffect = true,
                    chromaticAberration = true,
                  )
                },
                highlight = { Highlight.Default.copy(alpha = 0.9f) },
                shadow = { Shadow.Default.copy(alpha = 0.55f) },
                innerShadow = { InnerShadow(radius = 8.dp, alpha = 0.18f) },
                onDrawSurface = {
                  drawRect(bubbleTint)
                  drawRect(bubbleAccent)
                },
              ),
          )
        }

        Row(Modifier.fillMaxSize().selectableGroup()) {
          tabs.forEachIndexed { index, tab ->
            val selected = selectedIndex == index
            Column(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .selectable(
                  selected = selected,
                  role = Role.Tab,
                  onClick = { onSelect(index) },
                ),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
            ) {
              Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
              )
              Spacer(Modifier.height(2.dp))
              Text(
                tab.label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}
