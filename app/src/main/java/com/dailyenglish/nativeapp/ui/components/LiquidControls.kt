package com.dailyenglish.nativeapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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

/**
 * Circular glass action. While pressed it stretches outwards from the touch
 * point, so the press reads as pulling the glass around.
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
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val press by animateFloatAsState(
    targetValue = if (pressed) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.5f, stiffness = 520f),
    label = "glass-button-press",
  )
  var pressPoint by remember { mutableStateOf(Offset.Unspecified) }
  var boxSize by remember { mutableStateOf(IntSize.Zero) }
  LaunchedEffect(interaction) {
    interaction.interactions.collect { event ->
      if (event is PressInteraction.Press) pressPoint = event.pressPosition
    }
  }
  Box(
    modifier = modifier
      .size(size)
      .onSizeChanged { boxSize = it }
      .graphicsLayer {
        val ox = if (pressPoint.isSpecified && boxSize.width > 0) {
          (pressPoint.x / boxSize.width).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        val oy = if (pressPoint.isSpecified && boxSize.height > 0) {
          (pressPoint.y / boxSize.height).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        transformOrigin = TransformOrigin(ox, oy)
        scaleX = 1f + 0.07f * press
        scaleY = 1f + 0.04f * press
      }
      .glassCapsule(backdrop, CircleShape, glassTint(), press)
      .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      icon,
      contentDescription = contentDescription,
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
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val press by animateFloatAsState(
    targetValue = if (pressed) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.5f, stiffness = 520f),
    label = "glass-pill-press",
  )
  var pressPoint by remember { mutableStateOf(Offset.Unspecified) }
  var boxSize by remember { mutableStateOf(IntSize.Zero) }
  LaunchedEffect(interaction) {
    interaction.interactions.collect { event ->
      if (event is PressInteraction.Press) pressPoint = event.pressPosition
    }
  }
  Row(
    modifier = modifier
      .onSizeChanged { boxSize = it }
      .graphicsLayer {
        val ox = if (pressPoint.isSpecified && boxSize.width > 0) {
          (pressPoint.x / boxSize.width).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        val oy = if (pressPoint.isSpecified && boxSize.height > 0) {
          (pressPoint.y / boxSize.height).coerceIn(0f, 1f)
        } else {
          0.5f
        }
        transformOrigin = TransformOrigin(ox, oy)
        scaleX = 1f + 0.06f * press
        scaleY = 1f + 0.03f * press
      }
      .glassCapsule(backdrop, CircleShape, glassTint(), press)
      .clickable(interactionSource = interaction, indication = null, onClick = onClick)
      .padding(contentPadding),
    verticalAlignment = Alignment.CenterVertically,
    content = content,
  )
}

/**
 * Liquid glass segmented switch shaped like the dock: a glass capsule with a
 * floating lens chip that follows taps and horizontal drags.
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

    Box(
      Modifier
        .fillMaxSize()
        .glassCapsule(backdrop, CircleShape, glassTint(), 0f, blurRadius = 10.dp, lensRadius = 16.dp),
    )

    Box(
      Modifier
        .align(Alignment.CenterStart)
        .graphicsLayer {
          translationX = insetPx + 2.dp.toPx() + position.value * tabWidthPx
          val scale = 1f + 0.05f * p
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
              shadow = { Shadow.Default.copy(alpha = 0.14f + 0.4f * p) },
              innerShadow = { InnerShadow.Default.copy(radius = 10.dp, alpha = 0.08f + 0.24f * p) },
              onDrawSurface = {
                drawRect(
                  if (dark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.09f),
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
