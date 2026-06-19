package app.wordgame.presentation.ui

import android.content.Context
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
            if (savedLanguage != null) Screen.DailyWordGame else Screen.LanguageSelection
        )
    }

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
                    onBackToLanguageSelection = {
                        currentScreen = Screen.LanguageSelection
                    }
                )
            }
        }
    }
}