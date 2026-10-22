package com.dailyenglish.nativeapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dailyenglish.nativeapp.theme.LocalGlassBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val GlassButtonSize = 44.dp
private val SegmentedHeight = 44.dp
private val SegmentInset = 3.dp

/** Glass capsule + rim shared by the liquid buttons and segmented bars. */
private fun Modifier.glassCapsule(
  backdrop: Backdrop?,
  shape: Shape,
  tint: Color,
  press: Float,
  blurRadius: Dp = 8.dp,
  lensRadius: Dp = 20.dp,
): Modifier =
  if (backdrop == null) {
    background(tint, shape)
  } else {
    drawBackdrop(
      backdrop = backdrop,
      shape = { shape },
      effects = {
        vibrancy()
        blur(blurRadius.toPx())
        val amount = lensRadius.toPx() * (0.35f + 0.65f * press)
        lens(amount, amount)
      },
      highlight = { Highlight.Default.copy(alpha = 0.45f + 0.55f * press) },
      shadow = { Shadow.Default.copy(alpha = 0.16f + 0.34f * press) },
      innerShadow = { InnerShadow.Default.copy(radius = 8.dp, alpha = 0.08f + 0.18f * press) },
      onDrawSurface = { drawRect(tint) },
    )
  }

@Composable
private fun glassTint(): Color {
  val dark = isSystemInDarkTheme()
  return if (dark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.42f)
}

/** Same container tint the dock bar uses, so switches read as one family. */
@Composable
private fun dockContainerColor(): Color {
  val dark = isSystemInDarkTheme()
  return if (dark) Color(0xFF121212).copy(alpha = 0.42f) else Color(0xFFFAFAFA).copy(alpha = 0.42f)
}

/**
 * Press + drag gesture for glass buttons. Dragging pulls the button towards
 * the finger (damped) and stretches it along the drag axis; releasing springs
 * it back. A release before touch slop counts as a tap.
 */
@Composable
private fun rememberGlassPress(
  scope: CoroutineScope,
  press: Animatable<Float, AnimationVector1D>,
  drag: Animatable<Offset, AnimationVector2D>,
  onOrigin: (Offset) -> Unit,
  onClick: () -> Unit,
): Modifier {
  val click by rememberUpdatedState(onClick)
  val origin by rememberUpdatedState(onOrigin)
  return Modifier.pointerInput(Unit) {
    awaitEachGesture {
      val down = awaitFirstDown(requireUnconsumed = false)
      down.consume()
      origin(down.position)
      scope.launch {
        drag.snapTo(Offset.Zero)
        press.animateTo(1f, spring(dampingRatio = 1f, stiffness = 1000f))
      }
      var total = Offset.Zero
      var dragging = false
      var change = down
      while (change.pressed) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        change = event.changes.firstOrNull { it.id == down.id } ?: break
        if (!change.pressed) break
        val delta = change.position - change.previousPosition
        total += delta
        if (!dragging && total.getDistance() > viewConfiguration.touchSlop) dragging = true
        if (dragging) {
          change.consume()
          scope.launch { drag.snapTo(drag.value + delta) }
        }
      }
      if (!dragging) click()
      scope.launch { drag.animateTo(Offset.Zero, spring(dampingRatio = 0.55f, stiffness = 320f)) }
      scope.launch { press.animateTo(0f, spring(dampingRatio = 1f, stiffness = 1000f)) }
    }
  }
}

/**
 * Circular glass action. Pressing stretches it outwards from the touch point;
 * dragging pulls it along the finger with a directional squash, then it
 * springs back on release.
 */
@Composable
fun LiquidGlassCircleButton(
  onClick: () -> Unit,
  icon: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  size: Dp = GlassButtonSize,
) {
  val backdrop = LocalGlassBackdrop.current
  val scope = rememberCoroutineScope()
  val press = remember { Animatable(0f) }
  val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
  var origin by remember { mutableStateOf(Offset.Unspecified) }
  var boxSize by remember { mutableStateOf(IntSize.Zero) }
  val p = press.value

  Box(
    modifier = modifier
      .size(size)
      .zIndex(if (p > 0.01f) 1f else 0f)
      .onSizeChanged { boxSize = it }
      .graphicsLayer {
        val ox = if (origin.isSpecified && boxSize.width > 0) {
          (origin.x / boxSize.width).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        val oy = if (origin.isSpecified && boxSize.height > 0) {
          (origin.y / boxSize.height).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        transformOrigin = TransformOrigin(ox, oy)
        val d = drag.value
        translationX = d.x * 0.05f
        translationY = d.y * 0.05f
        val dxN = (d.x / boxSize.width.coerceAtLeast(1)).coerceIn(-0.4f, 0.4f)
        val dyN = (d.y / boxSize.height.coerceAtLeast(1)).coerceIn(-0.4f, 0.4f)
        scaleX = (1f + 0.06f * p) * (1f + abs(dxN) * 0.45f - abs(dyN) * 0.15f)
        scaleY = (1f + 0.04f * p) * (1f + abs(dyN) * 0.45f - abs(dxN) * 0.15f)
        rotationZ = dxN * -3f
      }
      .glassCapsule(backdrop, CircleShape, glassTint(), p)
      .then(
        rememberGlassPress(
          scope = scope,
          press = press,
          drag = drag,
          onOrigin = { origin = it },
          onClick = onClick,
        ),
      )
      .semantics {
        role = Role.Button
        if (contentDescription != null) this.contentDescription = contentDescription
        this.onClick {
          onClick()
          true
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(20.dp),
    )
  }
}

/** Pill version used by the focus timer and similar top-bar controls. */
@Composable
fun LiquidGlassPillButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
  content: @Composable RowScope.() -> Unit,
) {
  val backdrop = LocalGlassBackdrop.current
  val scope = rememberCoroutineScope()
  val press = remember { Animatable(0f) }
  val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
  var origin by remember { mutableStateOf(Offset.Unspecified) }
  var boxSize by remember { mutableStateOf(IntSize.Zero) }
  val p = press.value

  Row(
    modifier = modifier
      .zIndex(if (p > 0.01f) 1f else 0f)
      .onSizeChanged { boxSize = it }
      .graphicsLayer {
        val ox = if (origin.isSpecified && boxSize.width > 0) {
          (origin.x / boxSize.width).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        val oy = if (origin.isSpecified && boxSize.height > 0) {
          (origin.y / boxSize.height).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        transformOrigin = TransformOrigin(ox, oy)
        val d = drag.value
        translationX = d.x * 0.05f
        translationY = d.y * 0.05f
        val dxN = (d.x / boxSize.width.coerceAtLeast(1)).coerceIn(-0.4f, 0.4f)
        val dyN = (d.y / boxSize.height.coerceAtLeast(1)).coerceIn(-0.4f, 0.4f)
        scaleX = (1f + 0.05f * p) * (1f + abs(dxN) * 0.45f - abs(dyN) * 0.15f)
        scaleY = (1f + 0.03f * p) * (1f + abs(dyN) * 0.45f - abs(dxN) * 0.15f)
        rotationZ = dxN * -2f
      }
      .glassCapsule(backdrop, CircleShape, glassTint(), p)
      .then(
        rememberGlassPress(
          scope = scope,
          press = press,
          drag = drag,
          onOrigin = { origin = it },
          onClick = onClick,
        ),
      )
      .semantics {
        role = Role.Button
        this.onClick {
          onClick()
          true
        }
      }
      .padding(contentPadding),
    verticalAlignment = Alignment.CenterVertically,
    content = content,
  )
}

/**
 * Liquid glass segmented switch shaped like the dock: a glass capsule with a
 * floating lens chip that follows taps and horizontal drags. Like the dock,
 * the chip is inset at rest and grows 1.45x, overflowing the bar, only while
 * pressed or dragged.
 */
@Composable
fun LiquidGlassSegmented(
  options: List<Pair<String, String>>,
  selectedKey: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (options.isEmpty()) return
  val backdrop = LocalGlassBackdrop.current
  val dark = isSystemInDarkTheme()
  val accent = MaterialTheme.colorScheme.primary
  val ink = MaterialTheme.colorScheme.onSurface
  val scope = rememberCoroutineScope()
  val lastIndex = options.lastIndex
  val selectedIndex = options.indexOfFirst { it.first == selectedKey }.coerceIn(0, lastIndex)
  val position = remember { Animatable(selectedIndex.toFloat()) }
  val press = remember { Animatable(0f) }
  var bootstrapped by remember { mutableStateOf(false) }

  LaunchedEffect(selectedIndex) {
    val target = selectedIndex.toFloat()
    if (!bootstrapped) {
      bootstrapped = true
      position.snapTo(target)
    } else {
      scope.launch {
        launch { press.animateTo(1f, spring(1f, 1000f, 0.001f)) }
        position.animateTo(target, spring(1f, 900f, 0.001f))
        launch { press.animateTo(0f, spring(1f, 1000f, 0.001f)) }
      }
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(SegmentedHeight),
  ) {
    val density = LocalDensity.current
    val insetPx = with(density) { SegmentInset.toPx() }
    val tabWidth = (maxWidth - SegmentInset * 2) / options.size
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val p = press.value
    val barColor = dockContainerColor()

    // Same white bloom the dock floats over its bar, following the chip.
    val glow = Modifier.drawWithContent {
      drawContent()
      val current = press.value
      if (current > 0.01f) {
        val cx = insetPx + (position.value + 0.5f) * tabWidthPx
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.22f * current),
              Color.White.copy(alpha = 0f),
            ),
            center = Offset(cx, size.height / 2f),
            radius = size.minDimension * 1.5f,
          ),
          blendMode = BlendMode.Plus,
        )
      }
    }

    Box(
      Modifier
        .fillMaxSize()
        .then(
          if (backdrop == null) {
            Modifier.background(barColor, CircleShape)
          } else {
            Modifier.drawBackdrop(
              backdrop = backdrop,
              shape = { CircleShape },
              effects = {
                vibrancy()
                blur(8.dp.toPx())
                lens(24.dp.toPx(), 24.dp.toPx())
              },
              highlight = { Highlight.Default },
              onDrawSurface = { drawRect(barColor) },
            )
          },
        )
        .then(glow),
    )

    Box(
      Modifier
        .align(Alignment.CenterStart)
        .graphicsLayer {
          translationX = insetPx + 2.dp.toPx() + position.value * tabWidthPx
          val scale = 1f + 0.45f * p
          scaleX = scale
          scaleY = scale
        }
        .then(
          if (backdrop == null) {
            Modifier.background(Color.Black.copy(alpha = 0.09f), RoundedCornerShape(percent = 50))
          } else {
            Modifier.drawBackdrop(
              backdrop = backdrop,
              shape = { RoundedCornerShape(percent = 50) },
              effects = {
                vibrancy()
                blur(8.dp.toPx())
                lens(
                  10.dp.toPx() * p,
                  14.dp.toPx() * p,
                  chromaticAberration = true,
                )
              },
              highlight = { Highlight.Default.copy(alpha = 0.42f + 0.58f * p) },
              shadow = { Shadow.Default.copy(alpha = 0.16f + 0.44f * p) },
              innerShadow = { InnerShadow.Default.copy(radius = 12.dp, alpha = 0.08f + 0.32f * p) },
              onDrawSurface = {
                drawRect(
                  if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                  alpha = 1f - p,
                )
                drawRect(Color.Black.copy(alpha = 0.02f * p))
              },
            )
          },
        )
        .height(SegmentedHeight - SegmentInset * 2)
        .width(tabWidth - 4.dp),
    )

    Row(Modifier.fillMaxSize()) {
      options.forEachIndexed { index, option ->
        val selected = index == selectedIndex
        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            option.second,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) accent else ink,
          )
        }
      }
    }

    Box(
      Modifier
        .fillMaxSize()
        .pointerInput(options.size, tabWidthPx, insetPx, selectedIndex) {
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()
            val downX = down.position.x
            var dragging = false
            var accumulated = 0f
            var change = down
            while (change.pressed) {
              val event = awaitPointerEvent(PointerEventPass.Main)
              change = event.changes.firstOrNull { it.id == down.id } ?: break
              if (!change.pressed) break
              val dx = change.position.x - change.previousPosition.x
              accumulated += dx
              if (!dragging && abs(accumulated) > viewConfiguration.touchSlop) {
                dragging = true
                scope.launch { press.animateTo(1f, spring(1f, 1000f, 0.001f)) }
              }
              if (dragging) {
                change.consume()
                scope.launch {
                  position.snapTo(
                    (position.value + dx / tabWidthPx).coerceIn(0f, lastIndex.toFloat()),
                  )
                }
              }
            }
            if (dragging) {
              val target = position.value.roundToInt().coerceIn(0, lastIndex)
              scope.launch {
                position.animateTo(target.toFloat(), spring(1f, 900f, 0.001f))
                launch { press.animateTo(0f, spring(1f, 1000f, 0.001f)) }
              }
              if (target != selectedIndex) onSelect(options[target].first)
            } else {
              val index = floor((downX - insetPx) / tabWidthPx)
                .toInt()
                .coerceIn(0, lastIndex)
              if (index != selectedIndex) onSelect(options[index].first)
            }
          }
        },
    )
  }
}
