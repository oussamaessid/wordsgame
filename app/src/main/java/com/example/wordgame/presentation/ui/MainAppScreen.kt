package com.example.wordgame.presentation.ui

import androidx.compose.runtime.*
import com.example.wordgame.domain.model.Language

sealed class Screen {
    object LanguageSelection : Screen()
    object DailyWordGame : Screen()
}

@Composable
fun MainAppScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.LanguageSelection) }
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }

    when (currentScreen) {
        Screen.LanguageSelection -> {
            LanguageSelectionScreen(
                onLanguageSelected = { language ->
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