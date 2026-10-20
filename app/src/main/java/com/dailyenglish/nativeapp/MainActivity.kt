package com.dailyenglish.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyenglish.nativeapp.theme.DailyEnglishTheme
import com.dailyenglish.nativeapp.ui.AppRoot
import com.dailyenglish.nativeapp.ui.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val appViewModel: AppViewModel = hiltViewModel()
      DailyEnglishTheme {
        AppRoot(appViewModel = appViewModel)
      }
    }
  }
}
