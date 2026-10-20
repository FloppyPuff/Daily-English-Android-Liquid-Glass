package com.dailyenglish.nativeapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

data class LiquidTabItem(
  val label: String,
  val icon: ImageVector,
)

/**
 * Floating iOS-style liquid glass dock. The content behind the dock is captured
 * by the caller with rememberLayerBackdrop/layerBackdrop; this component only
 * draws the blurred, refractive capsule and its flat ink selection pill.
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
  val tint = if (isSystemInDarkTheme()) Color(0xFF111318).copy(alpha = 0.34f)
  else Color.White.copy(alpha = 0.42f)
  val hasSelection = selectedIndex in tabs.indices

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(76.dp)
      .drawBackdrop(
        backdrop = backdrop,
        shape = { CircleShape },
        effects = {
          vibrancy()
          blur(14.dp.toPx())
          lens(24.dp.toPx(), 22.dp.toPx(), depthEffect = true)
        },
        highlight = { Highlight.Default.copy(alpha = 0.75f) },
        shadow = { Shadow.Default },
        onDrawSurface = { drawRect(tint) },
      ),
  ) {
    val outerPadding = 6.dp
    val tabWidth = (maxWidth - outerPadding * 2) / tabs.size
    val restingIndex = if (selectedIndex in tabs.indices) selectedIndex else 0
    val offset by animateDpAsState(
      targetValue = tabWidth * restingIndex,
      animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
      label = "dock-pill",
    )

    Box(Modifier.fillMaxSize().padding(outerPadding)) {
      Box(
        Modifier
          .offset(x = offset)
          .width(tabWidth)
          .fillMaxHeight()
          .clip(CircleShape)
          .background(
            if (hasSelection) MaterialTheme.colorScheme.onSurface
            else Color.Transparent,
          ),
      )
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
              tint = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(2.dp))
            Text(
              tab.label,
              style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
              color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}
