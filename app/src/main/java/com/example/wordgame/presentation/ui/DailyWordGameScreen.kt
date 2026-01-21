package com.example.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordgame.di.AppContainer
import com.example.wordgame.domain.model.Language
import com.example.wordgame.presentation.viewmodel.GameViewModel

@Composable
fun DailyWordGameScreen(
    language: Language,
    onBackToLanguageSelection: () -> Unit
) {
    val viewModel: GameViewModel = viewModel(
        factory = AppContainer.provideViewModelFactory(),
        key = "game_vm_${language.name}"  // Sépare vraiment par langue
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(language) {
        viewModel.initializeGame(language)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            GameHeader(
                title = if (language == Language.FRENCH) "MOT DU JOUR" else "DAILY WORD",
                subtitle = if (language == Language.FRENCH) "Aujourd'hui" else "Today",
                backButtonText = if (language == Language.FRENCH) "Retour" else "Back",
                onBackClick = onBackToLanguageSelection,
                onStatsClick = { viewModel.toggleStatsDialog(true) },
                isSmallScreen = LocalConfiguration.current.screenHeightDp < 500,
                language = language
            )

            GameGrid(
                currentGuess = uiState.currentGuess,
                guesses = uiState.guesses,
                viewModel = viewModel,
                cellSize = if (LocalConfiguration.current.screenHeightDp < 500) 40.dp else 56.dp,
                cellSpacing = if (LocalConfiguration.current.screenHeightDp < 500) 6.dp else 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )

            GameKeyboard(
                onKeyPress = { key -> viewModel.onKeyPressed(key) },
                viewModel = viewModel,
                gameOver = uiState.gameOver,
                language = language,
                currentGuessLength = uiState.currentGuess.length   // ← clé pour activer/désactiver
            )
        }

        if (uiState.showStats) {
            StatsDialog(
                won = uiState.won,
                gameOver = uiState.gameOver,
                attempts = uiState.guesses.size,
                targetWord = uiState.targetWord,
                stats = uiState.stats,
                onDismiss = { viewModel.toggleStatsDialog(false) },
                language = language
            )
        }
    }
}