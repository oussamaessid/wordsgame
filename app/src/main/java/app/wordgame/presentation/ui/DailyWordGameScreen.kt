package app.wordgame.presentation.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DailyWordGameScreen(
    language: app.wordgame.domain.model.Language,
    onBackToLanguageSelection: () -> Unit
) {
    val viewModel: app.wordgame.presentation.viewmodel.GameViewModel = viewModel(
        factory = _root_ide_package_.app.wordgame.di.AppContainer.provideViewModelFactory(),
        key = "game_vm_${language.name}"
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(language) {
        viewModel.initializeGame(language)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF1976D2))
        }
        return
    }

    if (uiState.gameOver && !uiState.showRewardedAdDialog) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
        ) {
            StatsDialog(
                won = uiState.won,
                gameOver = uiState.gameOver,
                attempts = uiState.guesses.size,
                targetWord = uiState.targetWord,
                stats = uiState.stats,
                onDismiss = onBackToLanguageSelection,
                language = language,
                gameStartTime = uiState.gameStartTime,
                gameEndTime = uiState.gameEndTime
            )
        }
        return
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val isSmallScreen = screenHeight < 700

    val cellSize = when {
        screenHeight < 600 -> 38.dp
        screenHeight < 700 -> 45.dp
        screenHeight < 800 -> 52.dp
        else -> 56.dp
    }
    val cellSpacing = when {
        screenHeight < 600 -> 4.dp
        screenHeight < 700 -> 6.dp
        else -> 8.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding()
        ) {
            GameHeader(
                title = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) "MOT DU JOUR" else "DAILY WORD",
                subtitle = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) "Aujourd'hui" else "Today",
                backButtonText = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) "Retour" else "Back",
                onBackClick = onBackToLanguageSelection,
                onStatsClick = { viewModel.toggleStatsDialog(true) },
                isSmallScreen = isSmallScreen,
                language = language
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // 6ème ligne visible UNIQUEMENT après avoir regardé la vidéo
                val maxAttempts = if (uiState.hasExtraTry) 6 else 5

                GameGrid(
                    currentGuess = uiState.currentGuess,
                    guesses = uiState.guesses,
                    viewModel = viewModel,
                    cellSize = cellSize,
                    cellSpacing = cellSpacing,
                    maxAttempts = maxAttempts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GameKeyboard(
                onKeyPress = { key -> viewModel.onKeyPressed(key) },
                viewModel = viewModel,
                gameOver = uiState.gameOver,
                language = language,
                currentGuessLength = uiState.currentGuess.length
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(bottom = 8.dp)
            ) {
                BannerAdView(
                    isLanguageScreen = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.showStats) {
                Box(modifier = Modifier.fillMaxSize()) {
                    StatsDialog(
                        won = uiState.won,
                        gameOver = false,
                        attempts = uiState.guesses.size,
                        targetWord = uiState.targetWord,
                        stats = uiState.stats,
                        onDismiss = { viewModel.toggleStatsDialog(false) },
                        language = language,
                        gameStartTime = uiState.gameStartTime,
                        gameEndTime = uiState.gameEndTime
                    )
                }
            }
        }

        if (uiState.showRewardedAdDialog) {
            RewardedAdChoiceDialog(
                onWatchExtraTryAd = {
                    viewModel.hideRewardedAdDialog()

                    var wasRewarded = false

                    app.wordgame.ads.AdManager.showRewardedAdExtraTry(
                        activity = context as Activity,
                        onRewarded = {
                            wasRewarded = true
                            viewModel.addExtraTry()
                        },
                        onAdDismissed = {
                            if (!wasRewarded) {
                                viewModel.finishGameAsLost()
                            }
                        }
                    )
                },
                onDismiss = {
                    viewModel.finishGameAsLost()
                },
                language = language
            )
        }
    }
}