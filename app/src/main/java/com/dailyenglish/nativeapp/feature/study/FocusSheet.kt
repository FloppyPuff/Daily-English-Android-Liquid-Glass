package com.dailyenglish.nativeapp.feature.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dailyenglish.nativeapp.theme.NumberStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSheet(
  ui: StudyUiState,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color.Transparent,
    scrimColor = Color.Black.copy(alpha = 0.38f),
  ) {
    Box(Modifier.fillMaxWidth().padding(12.dp)) {
      Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Outlined.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
          }
          Text("专注计时", style = MaterialTheme.typography.titleLarge)
          Text("一次只做一件事", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            formatClock(ui.focus.remainingSeconds),
            style = NumberStyle.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize),
            color = MaterialTheme.colorScheme.onSurface,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
              onClick = if (ui.focus.running) onPause else onStart,
              shape = CircleShape,
              modifier = Modifier.weight(1f).height(48.dp),
            ) { Text(if (ui.focus.running) "暂停专注" else "开始专注") }
            OutlinedButton(
              onClick = onReset,
              shape = CircleShape,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier.weight(1f).height(48.dp),
            ) { Text("重新计时") }
          }
          Spacer(Modifier.height(2.dp))
          Text("关闭应用会结束本次计时。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

fun formatClock(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
