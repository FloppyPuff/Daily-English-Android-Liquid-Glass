package com.dailyenglish.nativeapp.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
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

private val BarHeight = 70.dp
private val BubbleHeight = 84.dp
private val TabInset = 4.dp
private const val PressedScale = 1.16f

/**
 * Floating liquid-glass dock modeled on Kyant0/AndroidLiquidGlass.
 *
 * Three stacked glass layers produce the lens instead of a flat frost:
 *  1. the visible bar samples the page layer with a light blur and a constant
 *     refraction, so page content warps at the capsule edge;
 *  2. a hidden, accent-tinted duplicate of the tab row is recorded into its own
 *     layer backdrop, giving the lens colorful content to magnify even on the
 *     app's white pages;
 *  3. the moving selection bubble samples both layers through a combined
 *     backdrop and drives its lens, highlight, shadows and squash from the
 *     press/drag progress, so it melts into a magnifier while sliding.
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
  val ink = MaterialTheme.colorScheme.onSurface
  val containerColor =
    if (dark) Color(0xFF121212).copy(alpha = 0.42f) else Color(0xFFFAFAFA).copy(alpha = 0.42f)
  val scope = rememberCoroutineScope()
  val lastIndex = tabs.lastIndex
  val tabsBackdrop = rememberLayerBackdrop()
  val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)

  val position = remember { Animatable(selectedIndex.coerceIn(0, lastIndex).toFloat()) }
  val pressProgress = remember { Animatable(0f) }
  val pressScaleX = remember { Animatable(1f) }
  val pressScaleY = remember { Animatable(1f) }
  val velocity = remember { Animatable(0f) }
  val velocityTracker = remember { VelocityTracker() }

  fun press() {
    scope.launch {
      launch { pressProgress.animateTo(1f, spring(1f, 1000f, 0.001f)) }
      launch { pressScaleX.animateTo(PressedScale, spring(0.6f, 250f, 0.001f)) }
      launch { pressScaleY.animateTo(PressedScale, spring(0.7f, 250f, 0.001f)) }
    }
  }

  fun release() {
    scope.launch {
      launch { pressProgress.animateTo(0f, spring(1f, 1000f, 0.001f)) }
      launch { pressScaleX.animateTo(1f, spring(0.6f, 250f, 0.001f)) }
      launch { pressScaleY.animateTo(1f, spring(0.7f, 250f, 0.001f)) }
      launch { velocity.animateTo(0f, spring(0.5f, 300f, 0.05f)) }
    }
  }

  fun trackVelocity() {
    val target = velocityTracker.calculateVelocity().x / lastIndex.coerceAtLeast(1)
    scope.launch { velocity.animateTo(target, spring(0.5f, 300f, 0.05f)) }
  }

  fun glideTo(target: Float) {
    scope.launch {
      press()
      position.animateTo(target, spring(1f, 900f, 0.001f)) {
        velocityTracker.addPosition(SystemClock.uptimeMillis(), Offset(value, 0f))
        trackVelocity()
      }
      release()
    }
  }

  var bootstrapped by remember { mutableStateOf(false) }
  LaunchedEffect(selectedIndex) {
    val target = selectedIndex.coerceIn(0, lastIndex).toFloat()
    if (!bootstrapped) {
      bootstrapped = true
      position.snapTo(target)
    } else {
      glideTo(target)
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(BubbleHeight),
    contentAlignment = Alignment.Center,
  ) {
    val density = LocalDensity.current
    val tabInsetPx = with(density) { TabInset.toPx() }
    val tabWidth = (maxWidth - TabInset * 2) / tabs.size
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val progress = pressProgress.value

    // Soft white bloom that follows the bubble while it is pressed or dragged.
    val glow = Modifier.drawWithContent {
      drawContent()
      val p = pressProgress.value
      if (p > 0.01f) {
        val cx = tabInsetPx + (position.value + 0.5f) * tabWidthPx
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.22f * p), Color.White.copy(alpha = 0f)),
            center = Offset(cx, size.height / 2f),
            radius = size.minDimension * 1.5f,
          ),
          blendMode = BlendMode.Plus,
        )
      }
    }

    Row(
      Modifier
        .align(Alignment.Center)
        .drawBackdrop(
          backdrop = backdrop,
          shape = { CircleShape },
          effects = {
            vibrancy()
            blur(8.dp.toPx())
            lens(24.dp.toPx(), 24.dp.toPx())
          },
          layerBlock = {
            val scale = 1f + 16.dp.toPx() / size.width * progress
            scaleX = scale
            scaleY = scale
          },
          onDrawSurface = { drawRect(containerColor) },
        )
        .then(glow)
        .height(BarHeight)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      DockTabs(tabs, selectedIndex, accent, ink, interactive = true, onSelect = onSelect)
    }

    // Invisible twin of the tab row, tinted with the accent and recorded into
    // its own layer so the selection lens has something liquid to magnify.
    Row(
      Modifier
        .align(Alignment.Center)
        .clearAndSetSemantics { }
        .alpha(0f)
        .layerBackdrop(tabsBackdrop)
        .drawBackdrop(
          backdrop = backdrop,
          shape = { CircleShape },
          effects = {
            vibrancy()
            blur(8.dp.toPx())
            lens(24.dp.toPx() * progress, 24.dp.toPx() * progress)
          },
          highlight = { Highlight.Default.copy(alpha = progress) },
          onDrawSurface = { drawRect(containerColor) },
        )
        .then(glow)
        .height(BarHeight)
        .fillMaxWidth()
        .graphicsLayer { colorFilter = ColorFilter.tint(accent) },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      DockTabs(tabs, selectedIndex, accent, ink, interactive = false, onSelect = {})
    }

    Box(
      Modifier
        .align(Alignment.CenterStart)
        .graphicsLayer { translationX = tabInsetPx + position.value * tabWidthPx }
        .drawBackdrop(
          backdrop = combinedBackdrop,
          shape = { CircleShape },
          effects = {
            lens(
              10.dp.toPx() * progress,
              14.dp.toPx() * progress,
              chromaticAberration = true,
            )
          },
          highlight = { Highlight.Default.copy(alpha = progress) },
          shadow = { Shadow.Default.copy(alpha = 0.6f * progress) },
          innerShadow = { InnerShadow.Default.copy(radius = 8.dp * progress, alpha = progress) },
          layerBlock = {
            var sx = pressScaleX.value
            var sy = pressScaleY.value
            val v = velocity.value / 10f
            sx /= 1f - (v * 0.75f).coerceIn(-0.2f, 0.2f)
            sy *= 1f - (v * 0.25f).coerceIn(-0.2f, 0.2f)
            scaleX = sx
            scaleY = sy
          },
          onDrawSurface = {
            drawRect(
              if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.10f),
              alpha = 1f - progress,
            )
            drawRect(Color.Black.copy(alpha = 0.03f * progress))
          },
        )
        .height(BubbleHeight)
        .width(tabWidth),
    )

    Box(
      Modifier
        .fillMaxSize()
        .pointerInput(tabs.size, tabWidthPx) {
          detectHorizontalDragGestures(
            onDragStart = {
              velocityTracker.resetTracking()
              press()
            },
            onHorizontalDrag = { change, dragAmount ->
              change.consume()
              val next = (position.value + dragAmount / tabWidthPx)
                .coerceIn(0f, lastIndex.toFloat())
              scope.launch { position.snapTo(next) }
              velocityTracker.addPosition(change.uptimeMillis, Offset(next, 0f))
              trackVelocity()
            },
            onDragEnd = {
              val target = position.value.roundToInt().coerceIn(0, lastIndex)
              if (target != selectedIndex) onSelect(target) else glideTo(target.toFloat())
            },
            onDragCancel = {
              glideTo(selectedIndex.coerceIn(0, lastIndex).toFloat())
            },
          )
        },
    )
  }
}

@Composable
private fun DockTabs(
  tabs: List<LiquidTabItem>,
  selectedIndex: Int,
  accent: Color,
  ink: Color,
  interactive: Boolean,
  onSelect: (Int) -> Unit,
) {
  Row(
    Modifier
      .fillMaxSize()
      .padding(horizontal = TabInset)
      .then(if (interactive) Modifier.selectableGroup() else Modifier),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    tabs.forEachIndexed { index, tab ->
      val selected = selectedIndex == index
      val color = if (selected) accent else ink
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clip(CircleShape)
          .then(
            if (interactive) {
              Modifier.selectable(
                selected = selected,
                role = Role.Tab,
                onClick = { onSelect(index) },
              )
            } else {
              Modifier
            },
          ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Icon(
          tab.icon,
          contentDescription = if (interactive) tab.label else null,
          tint = color,
          modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
          tab.label,
          style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
          color = color,
        )
      }
    }
  }
}
