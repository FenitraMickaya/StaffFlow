package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppScreens
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StaffFlowViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        // Instantiate our main ViewModel reactively
        val sViewModel: StaffFlowViewModel = viewModel()
        MainAppScreens(viewModel = sViewModel)
      }
    }
  }
}
