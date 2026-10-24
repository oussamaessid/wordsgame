package app.wordgame

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import app.wordgame.presentation.ui.TutorialScreen
import app.wordgame.presentation.ui.DailyHomeScreen

class DailyOnboardingTest {
    @get:Rule val compose = createComposeRule()
    @Test fun tutorialCanBeCompleted() {
        var finished = false
        compose.setContent { MaterialTheme { TutorialScreen { finished = true } } }
        compose.onNodeWithText("Essayer CARTE").performScrollTo().performClick()
        compose.onNodeWithText("Essayer PLAGE").performScrollTo().performClick()
        compose.onNodeWithText("Jouer au défi du jour").performScrollTo().performClick()
        compose.runOnIdle { org.junit.Assert.assertTrue(finished) }
    }
    @Test fun tutorialCanBeSkipped() {
        var finished = false
        compose.setContent { MaterialTheme { TutorialScreen { finished = true } } }
        compose.onNodeWithText("Passer le tutoriel").performScrollTo().performClick()
        compose.runOnIdle { org.junit.Assert.assertTrue(finished) }
    }
    @Test fun tutorialButtonsRemainReachableOnSmallScreen() {
        var finished = false
        compose.setContent { MaterialTheme {
            androidx.compose.foundation.layout.Box(Modifier.requiredSize(280.dp, 360.dp)) {
                TutorialScreen { finished = true }
            }
        } }
        compose.onNodeWithText("Essayer CARTE").performScrollTo().performClick()
        compose.onNodeWithText("Essayer PLAGE").performScrollTo().performClick()
        compose.onNodeWithText("Jouer au défi du jour").performScrollTo().performClick()
        compose.runOnIdle { org.junit.Assert.assertTrue(finished) }
    }
    @Test fun homeFitsSmallScreenWithoutScrolling() {
        var played = false
        compose.setContent { MaterialTheme {
            androidx.compose.foundation.layout.Box(Modifier.requiredSize(280.dp, 420.dp)) { DailyHomeScreen { played = true } }
        } }
        compose.onNodeWithText("English").performClick()
        compose.onNodeWithText("Today’s word").assertIsDisplayed()
        compose.onNodeWithText("Your progress").assertIsDisplayed()
        compose.onNodeWithText("Win rate").assertIsDisplayed()
        compose.onNodeWithText("Play").assertIsDisplayed().performClick()
        compose.runOnIdle { org.junit.Assert.assertTrue(played) }
        compose.onAllNodes(hasScrollAction()).assertCountEquals(0)
        compose.onNodeWithText("Comment jouer").assertDoesNotExist()
        compose.onNodeWithText("Vos derniers défis").assertDoesNotExist()
    }
    @Test fun homeLanguageChangesImmediately() {
        compose.setContent { MaterialTheme { DailyHomeScreen {} } }
        compose.onNodeWithText("English").performClick()
        compose.onNodeWithText("Today’s word").assertIsDisplayed()
        compose.onNodeWithText("Your progress").assertIsDisplayed()
        compose.onNodeWithText("Français").performClick()
        compose.onNodeWithText("Le mot du jour").assertIsDisplayed()
        compose.onNodeWithText("Votre progression").assertIsDisplayed()
        compose.onNodeWithText("Today’s word").assertDoesNotExist()
    }
}
