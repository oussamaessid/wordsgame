package app.wordgame.presentation.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

sealed class Screen {
    object LanguageSelection : Screen()
    object DailyWordGame : Screen()
}

private fun saveLanguage(context: Context, language: app.wordgame.domain.model.Language) {
    context.getSharedPreferences("WordGame_Settings", Context.MODE_PRIVATE)
        .edit().putString("last_language", language.code).apply()
}

private fun loadLanguage(context: Context): app.wordgame.domain.model.Language? {
    val code = context.getSharedPreferences("WordGame_Settings", Context.MODE_PRIVATE)
        .getString("last_language", null) ?: return null
    return app.wordgame.domain.model.Language.values().firstOrNull { it.code == code }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current

    val savedLanguage = remember { loadLanguage(context) }

    var selectedLanguageCode by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(savedLanguage?.code) }
    val selectedLanguage = app.wordgame.domain.model.Language.values().firstOrNull { it.code == selectedLanguageCode }
    var gameScreen by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val settings = remember { context.getSharedPreferences("WordGame_Settings", Context.MODE_PRIVATE) }
    var tutorial by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(!settings.getBoolean("tutorial_seen", false)) }
    if (tutorial) {
        val tutorialLanguage = savedLanguage ?: app.wordgame.domain.model.Language.FRENCH
        TutorialScreen(language = tutorialLanguage, onPlay = {
            saveLanguage(context, tutorialLanguage)
            selectedLanguageCode = tutorialLanguage.code
            gameScreen = true
            settings.edit().putBoolean("tutorial_seen", true).apply()
            tutorial = false
        }, onDone = {
            settings.edit().putBoolean("tutorial_seen", true).apply()
            tutorial = false
        })
        return
    }

    val players = app.wordgame.di.AppContainer.players
    var playerPage by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { players.syncScores() }
    androidx.activity.compose.BackHandler(enabled = playerPage.isNotEmpty() && playerPage != "setup") { playerPage = "" }
    if (playerPage.isNotEmpty()) {
        PlayerScreen(players, ranking = playerPage == "ranking", setup = playerPage == "setup", onBack = {
            players.completeSetup(); playerPage = ""
        })
        return
    }
    androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.fillMaxSize()) {
        if (!gameScreen) PlayerToolbar(players, onProfile = { playerPage = "profile" }, onRanking = { playerPage = "ranking" })
        androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.weight(1f)) {
    when (if (gameScreen) Screen.DailyWordGame else Screen.LanguageSelection) {
        Screen.LanguageSelection -> {
            DailyHomeScreen(
                onLanguageSelected = { language ->
                    saveLanguage(context, language)
                    selectedLanguageCode = language.code
                    gameScreen = true
                }
            )
        }
        Screen.DailyWordGame -> {
            selectedLanguage?.let { language ->
                DailyWordGameScreen(
                    language = language,
                    onRanking = { playerPage = "ranking" },
                    onBackToLanguageSelection = {
                        gameScreen = false
                    }
                )
            }
        }
    }
}
        }
    }
