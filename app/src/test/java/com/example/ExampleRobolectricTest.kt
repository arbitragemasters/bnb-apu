package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Binance", appName)
  }

  @Test
  fun `render main app and simulate screens`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppContainer()
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `navigate to home and verify no crashes`() {
    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppContainer()
      }
    }
    composeTestRule.waitForIdle()
    // Click on the bottom navigation tab labeled "Home"
    composeTestRule.onNodeWithText("Home").performClick()
    composeTestRule.waitForIdle()
  }
}
