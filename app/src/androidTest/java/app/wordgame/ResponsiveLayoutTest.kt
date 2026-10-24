package app.wordgame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import app.wordgame.presentation.ui.DailyHomeScreen
import app.wordgame.presentation.ui.GameKeyboard
import app.wordgame.presentation.viewmodel.GameViewModel
import app.wordgame.domain.model.Language
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class ResponsiveLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeKeepsPrimaryActionVisibleAcrossPhoneSizesAndTextScales() {
        var spec by mutableStateOf(Triple(280, 420, 1f))
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, spec.third)) {
                MaterialTheme { Box(Modifier.requiredSize(spec.first.dp, spec.second.dp)) { DailyHomeScreen {} } }
            }
        }
        compose.onNodeWithText("English").performClick()
        for (next in listOf(Triple(240, 320, 1f), Triple(280, 420, 1f), Triple(320, 480, 1.5f),
            Triple(360, 640, 2f), Triple(640, 280, 1.3f), Triple(800, 360, 2f))) {
            compose.runOnIdle { spec = next }
            compose.onNodeWithText("Today’s word").assertIsDisplayed()
            compose.onNodeWithText("Play").assertIsDisplayed()
            compose.onAllNodes(hasScrollAction()).assertCountEquals(0)
        }
    }

    @Test fun statisticsRemainAccessibleWithLargeTextOnSmallPhone() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                MaterialTheme { Box(Modifier.requiredSize(280.dp, 420.dp)) { DailyHomeScreen {} } }
            }
        }
        compose.onNodeWithText("English").performClick()
        compose.onNodeWithText("Statistics").assertIsDisplayed().performClick()
        compose.onNodeWithText("CLOSE").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Play").assertIsDisplayed()
    }

    @Test fun compactKeyboardKeepsSubmitAndDeleteAvailableInBothLanguages() {
        val vm = app.wordgame.di.AppContainer.provideViewModelFactory().create(GameViewModel::class.java)
        var language by mutableStateOf(Language.FRENCH)
        var lastKey = ""
        compose.setContent { MaterialTheme {
            Box(Modifier.requiredSize(240.dp, 190.dp)) {
                GameKeyboard({ lastKey = it }, vm, false, language, 5, compact = true)
            }
        } }
        compose.onNodeWithContentDescription("Valider").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("ENTER", lastKey); language = Language.ENGLISH }
        compose.onNodeWithContentDescription("Verify").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("⌫").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("⌫", lastKey) }
    }
}
