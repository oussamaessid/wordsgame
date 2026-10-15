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

    var selectedLanguage by remember { mutableStateOf(savedLanguage) }
    var currentScreen by remember {
        mutableStateOf<Screen>(
            Screen.LanguageSelection
        )
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
        PlayerToolbar(players, onProfile = { playerPage = "profile" }, onRanking = { playerPage = "ranking" })
        androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.weight(1f)) {
    when (currentScreen) {
        Screen.LanguageSelection -> {
            LanguageSelectionScreen(
                onLanguageSelected = { language ->
                    saveLanguage(context, language)
                    selectedLanguage = language
                    currentScreen = Screen.DailyWordGame
                }
            )
        }
        Screen.DailyWordGame -> {
            selectedLanguage?.let { language ->
                DailyWordGameScreen(
                    language = language,
                    onRanking = { playerPage = "ranking" },
                    onBackToLanguageSelection = {
                        currentScreen = Screen.LanguageSelection
                    }
                )
            }
        }
    }
}
        }
    }
