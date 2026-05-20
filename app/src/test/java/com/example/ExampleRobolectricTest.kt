package com.example

import android.content.Context
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.example.viewmodel.StaffFlowViewModel
import com.example.ui.screens.MainAppScreens
import com.example.ui.theme.MyApplicationTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StaffFlow", appName)
  }

  @Test
  fun `test viewModel initialization`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = StaffFlowViewModel(application)
    assert(viewModel != null)
  }

  @Test
  fun `test main UI composition`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = StaffFlowViewModel(application)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppScreens(viewModel = viewModel)
      }
    }
  }
}
