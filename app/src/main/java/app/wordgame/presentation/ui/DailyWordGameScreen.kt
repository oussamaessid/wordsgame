package app.wordgame.presentation.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun DailyWordGameScreen(
    language: app.wordgame.domain.model.Language,
    onBackToLanguageSelection: () -> Unit,
    onRanking: () -> Unit = {}
) {

    BackHandler(enabled = true) {
        onBackToLanguageSelection()
    }

    val viewModel: app.wordgame.presentation.viewmodel.GameViewModel = viewModel(
        factory = app.wordgame.di.AppContainer.provideViewModelFactory(),
        key = "game_vm_${language.name}"
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(language) {
        viewModel.initializeGame(language)
    }

    // Le minuteur ne tourne que lorsque l'écran de jeu est au premier plan et a le focus :
    // il s'arrête en arrière-plan, écran éteint, app fermée, volet de notifications ou capture
    // d'écran ouverts, et reprend au retour sur le jeu.
    val lifecycleState by androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val windowFocused = androidx.compose.ui.platform.LocalWindowInfo.current.isWindowFocused
    val screenActive = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) && windowFocused
    LaunchedEffect(screenActive, viewModel) {
        if (screenActive) viewModel.resumeTimer() else viewModel.pauseTimer()
    }
    DisposableEffect(viewModel) { onDispose { viewModel.pauseTimer() } }

    LaunchedEffect(uiState.invalidWordError) {
        if (uiState.invalidWordError) {
            delay(1800)
            viewModel.clearInvalidWordError()
        }
    }

    var showCompletionDialog by remember(language) { mutableStateOf(false) }
    LaunchedEffect(uiState.gameOver, uiState.showRewardedAdDialog) {
        showCompletionDialog = false
        if (uiState.gameOver && !uiState.showRewardedAdDialog) {
            // Keep the completed grid visible long enough to read the correct answer.
            delay(4000)
            showCompletionDialog = true
        }
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

    if (uiState.noInternetError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp)
            ) {
                Text(text = "📡", fontSize = 64.sp)
                Text(
                    text = if (language == app.wordgame.domain.model.Language.FRENCH)
                        "Pas de connexion internet"
                    else "No internet connection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (language == app.wordgame.domain.model.Language.FRENCH)
                        "Une connexion est nécessaire pour\nrécupérer le mot du jour."
                    else "A connection is required\nto get today's word.",
                    fontSize = 15.sp,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.initializeGame(language) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(
                        text = if (language == app.wordgame.domain.model.Language.FRENCH)
                            "Réessayer" else "Retry",
                        color = Color.White
                    )
                }
                Button(
                    onClick = onBackToLanguageSelection,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCCCCC))
                ) {
                    Text(
                        text = if (language == app.wordgame.domain.model.Language.FRENCH)
                            "Retour" else "Back",
                        color = Color(0xFF333333)
                    )
                }
            }
        }
        return
    }

    if (uiState.gameOver && !uiState.showRewardedAdDialog && showCompletionDialog) {
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
                gameEndTime = uiState.gameEndTime,
                onRanking = onRanking,
                day = viewModel.gameDay, guesses = uiState.guesses, maxAttempts = uiState.maxAttempts
            )
        }
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.White).navigationBarsPadding()) {
        val landscape = maxWidth > maxHeight && maxWidth >= 480.dp
        val fullHeader = !landscape && maxHeight >= 440.dp
        val compact = maxHeight < 560.dp
        val spacing = if (compact) 4.dp else 6.dp
        var elapsed by remember { mutableLongStateOf(viewModel.elapsedMs()) }
        LaunchedEffect(uiState.gameStartTime) { while (true) { elapsed = viewModel.elapsedMs(); delay(250) } }
        val board: @Composable (Modifier) -> Unit = { modifier ->
            BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
                val cell = responsiveCellSize(maxWidth.value, maxHeight.value, uiState.maxAttempts, spacing.value).dp
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    GameGrid(currentGuess = uiState.currentGuess, guesses = uiState.guesses, viewModel = viewModel,
                        cellSize = cell, cellSpacing = spacing, maxAttempts = uiState.maxAttempts,
                        modifier = Modifier.fillMaxWidth().padding(4.dp))
                }
            }
        }
        val keyboard: @Composable () -> Unit = {
            GameKeyboard(onKeyPress = viewModel::onKeyPressed, viewModel = viewModel, gameOver = uiState.gameOver,
                language = language, currentGuessLength = uiState.currentGuess.length, compact = compact || landscape)
        }
        Column(Modifier.fillMaxSize()) {
            if (fullHeader) GameHeader(
                title = if (language == app.wordgame.domain.model.Language.FRENCH) "MOT DU JOUR" else "DAILY WORD",
                subtitle = "", backButtonText = if (language == app.wordgame.domain.model.Language.FRENCH) "Retour" else "Back",
                onBackClick = onBackToLanguageSelection, onStatsClick = { viewModel.toggleStatsDialog(true) },
                isSmallScreen = compact, language = language)
            else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.TextButton(onClick = onBackToLanguageSelection) {
                    Text(if (language == app.wordgame.domain.model.Language.FRENCH) "Retour" else "Back")
                }
                Text("DailyWord", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                androidx.compose.material3.TextButton(onClick = { viewModel.toggleStatsDialog(true) }) {
                    Text(if (language == app.wordgame.domain.model.Language.FRENCH) "Statistiques" else "Statistics")
                }
            }
            Text(if (uiState.invalidWordError) {
                if (language == app.wordgame.domain.model.Language.FRENCH) "Mot non reconnu !" else "Word not found!"
            } else "⏱ " + app.wordgame.domain.model.GameTimer.format(elapsed),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = if (uiState.invalidWordError) Color(0xFFD32F2F) else Color(0xFF1976D2), fontSize = 13.sp)
            if (landscape) Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                board(Modifier.weight(1f).fillMaxHeight())
                Column(Modifier.weight(1.4f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) { keyboard() }
            } else {
                board(Modifier.fillMaxWidth().weight(1f))
                keyboard()
            }
        }

        if (uiState.showStats && (!(uiState.gameOver || uiState.won) || showCompletionDialog)) {
            StatsDialog(
                won = uiState.won,
                gameOver = false,
                attempts = uiState.guesses.size,
                targetWord = uiState.targetWord,
                stats = uiState.stats,
                onDismiss = { viewModel.toggleStatsDialog(false) },
                language = language,
                gameStartTime = uiState.gameStartTime,
                gameEndTime = uiState.gameEndTime,
                onRanking = onRanking,
                day = viewModel.gameDay, guesses = uiState.guesses, maxAttempts = uiState.maxAttempts
            )
        }

        if (uiState.showRewardedAdDialog) {
            RewardedAdChoiceDialog(
                onWatchExtraTryAd = {
                    // FIX: hideRewardedAdDialog() supprimé ici — c'est addExtraTry()
                    // qui masque le dialog, évitant ainsi la race condition qui
                    // empêchait la ligne bonus d'apparaître correctement.
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
