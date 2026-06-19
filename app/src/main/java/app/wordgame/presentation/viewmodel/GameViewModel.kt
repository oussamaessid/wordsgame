package app.wordgame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class GameUiState(
    val currentGuess: String = "",
    val guesses: List<String> = emptyList(),
    val gameOver: Boolean = false,
    val won: Boolean = false,
    val stats: app.wordgame.domain.model.GameStats = app.wordgame.domain.model.GameStats(),
    val showStats: Boolean = false,
    val targetWord: String = "",
    val isLoading: Boolean = true,
    val gameStartTime: Long = 0L,
    val gameEndTime: Long = 0L,
    val showRewardedAdDialog: Boolean = false,
    val extraTriesGranted: Int = 0,
    val noInternetError: Boolean = false,
    val invalidWordError: Boolean = false
) {
    /** Nombre total de lignes visibles dans la grille (4, 5 ou 6) */
    val maxAttempts: Int get() = GameViewModel.BASE_ATTEMPTS + extraTriesGranted
}

class GameViewModel(
    private val getDailyWordUseCase: app.wordgame.domain.usecase.GetDailyWordUseCase,
    private val saveGameStateUseCase: app.wordgame.domain.usecase.SaveGameStateUseCase,
    private val loadGameStateUseCase: app.wordgame.domain.usecase.LoadGameStateUseCase,
    private val saveStatsUseCase: app.wordgame.domain.usecase.SaveStatsUseCase,
    private val loadStatsUseCase: app.wordgame.domain.usecase.LoadStatsUseCase,
    private val validateGuessUseCase: app.wordgame.domain.usecase.ValidateGuessUseCase,
    private val updateStatsUseCase: app.wordgame.domain.usecase.UpdateStatsUseCase,
    private val isValidWordUseCase: app.wordgame.domain.usecase.IsValidWordUseCase,
    private val repository: app.wordgame.data.repository.GameRepositoryImpl
) : ViewModel() {

    companion object {
        const val WORD_LENGTH = 5
        const val BASE_ATTEMPTS = 4
        const val MAX_EXTRA_TRIES = 2
        const val MAX_TOTAL_ATTEMPTS = BASE_ATTEMPTS + MAX_EXTRA_TRIES  // = 6
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentLanguage: app.wordgame.domain.model.Language =
        app.wordgame.domain.model.Language.ENGLISH
    private val keyboardStates =
        mutableMapOf<app.wordgame.domain.model.Language,
                MutableMap<String, app.wordgame.domain.model.LetterState>>()

    // ─────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────

    fun initializeGame(language: app.wordgame.domain.model.Language) {
        currentLanguage = language
        keyboardStates.putIfAbsent(language, mutableMapOf())

        viewModelScope.launch {
            val success = repository.loadWordsFromUrl()

            if (!success) {
                _uiState.value = GameUiState(isLoading = false, noInternetError = true)
                return@launch
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val targetWord = getDailyWordUseCase(language, today)
            val savedState = loadGameStateUseCase(language, today)
            val stats = loadStatsUseCase(language)

            if (savedState != null &&
                savedState.word == targetWord &&
                savedState.date == today
            ) {
                _uiState.value = GameUiState(
                    guesses = savedState.guesses,
                    gameOver = savedState.gameOver,
                    won = savedState.won,
                    stats = stats,
                    targetWord = targetWord,
                    showStats = savedState.gameOver,
                    isLoading = false,
                    gameStartTime = savedState.gameStartTime,
                    gameEndTime = savedState.gameEndTime,
                    extraTriesGranted = savedState.extraTriesGranted
                )
                updateKeyboardFromGuesses(savedState.guesses, targetWord)
            } else {
                _uiState.value = GameUiState(
                    stats = stats,
                    targetWord = targetWord,
                    isLoading = false,
                    gameStartTime = System.currentTimeMillis(),
                    gameEndTime = 0L
                )
                keyboardStates[language]?.clear()
            }
        }
    }

    // ─────────────────────────────────────────────
    //  CLAVIER
    // ─────────────────────────────────────────────

    fun onKeyPressed(key: String) {
        val state = _uiState.value
        if (state.gameOver) return

        when (key.uppercase()) {
            "ENTER"           -> handleEnter()
            "⌫", "BACKSPACE" -> handleDelete()
            else              -> if (key.length == 1 && key[0].isLetter()) handleLetterInput(key.uppercase())
        }
    }

    private fun handleLetterInput(letter: String) {
        val state = _uiState.value
        if (state.currentGuess.length < WORD_LENGTH) {
            _uiState.value = state.copy(currentGuess = state.currentGuess + letter)
        }
    }

    private fun handleDelete() {
        val state = _uiState.value
        if (state.currentGuess.isNotEmpty()) {
            _uiState.value = state.copy(currentGuess = state.currentGuess.dropLast(1))
        }
    }

    // ─────────────────────────────────────────────
    //  VALIDATION D'UN ESSAI
    // ─────────────────────────────────────────────

    fun clearInvalidWordError() {
        _uiState.value = _uiState.value.copy(invalidWordError = false)
    }

    private fun handleEnter() {
        val state = _uiState.value
        if (state.currentGuess.length != WORD_LENGTH) return

        if (!isValidWordUseCase(state.currentGuess, currentLanguage)) {
            _uiState.value = state.copy(invalidWordError = true)
            return
        }

        _uiState.value = state.copy(invalidWordError = false)
        val result = validateGuessUseCase(state.currentGuess, state.targetWord)
        val newGuesses = state.guesses + result.guess

        _uiState.value = state.copy(guesses = newGuesses, currentGuess = "")
        updateKeyboardFromGuesses(listOf(state.currentGuess), state.targetWord)

        // ✅ VICTOIRE
        if (result.isCorrect) {
            val endTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                won = true,
                gameOver = true,
                gameEndTime = endTime
            )
            handleGameOver(true)
            return
        }

        val triesUsed = newGuesses.size
        val currentExtra = state.extraTriesGranted

        when {
            triesUsed >= MAX_TOTAL_ATTEMPTS -> {
                finishLost()
            }
            triesUsed >= BASE_ATTEMPTS + currentExtra && currentExtra < MAX_EXTRA_TRIES -> {
                if (app.wordgame.ads.AdManager.isRewardedAdExtraTryAvailable()) {
                    _uiState.value = _uiState.value.copy(showRewardedAdDialog = true)
                    saveGameState()
                } else {
                    finishLost()
                }
            }
            else -> saveGameState()
        }
    }

    // ─────────────────────────────────────────────
    //  FIN DE PARTIE
    // ─────────────────────────────────────────────

    private fun finishLost() {
        val endTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            gameOver = true,
            won = false,
            gameEndTime = endTime
        )
        handleGameOver(false)
    }

    private fun handleGameOver(won: Boolean) {
        viewModelScope.launch {
            val newStats = updateStatsUseCase(_uiState.value.stats, won)
            _uiState.value = _uiState.value.copy(stats = newStats, showStats = true)
            saveStatsUseCase(newStats, currentLanguage)
            saveGameState()
        }
    }

    // ─────────────────────────────────────────────
    //  GESTION DES ESSAIS BONUS (VIDÉOS)
    // ─────────────────────────────────────────────

    /**
     * FIX: Wrapped in viewModelScope.launch pour garantir l'exécution
     * sur le Main thread, même si appelé depuis un callback publicitaire.
     */
    fun addExtraTry() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.extraTriesGranted >= MAX_EXTRA_TRIES) return@launch

            _uiState.value = state.copy(
                showRewardedAdDialog = false,
                extraTriesGranted = state.extraTriesGranted + 1,
                currentGuess = ""
            )
            saveGameState()
        }
    }

    /**
     * FIX: Wrapped in viewModelScope.launch pour garantir l'exécution
     * sur le Main thread, même si appelé depuis un callback publicitaire.
     */
    fun finishGameAsLost() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.gameOver || state.won) return@launch

            _uiState.value = state.copy(showRewardedAdDialog = false)
            finishLost()
        }
    }

    fun hideRewardedAdDialog() {
        _uiState.value = _uiState.value.copy(showRewardedAdDialog = false)
    }

    fun toggleStatsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStats = show)
    }

    // ─────────────────────────────────────────────
    //  CLAVIER — ÉTATS DES LETTRES
    // ─────────────────────────────────────────────

    private fun updateKeyboardFromGuesses(guesses: List<String>, targetWord: String) {
        val map = keyboardStates[currentLanguage] ?: return

        guesses.forEach { guess ->
            if (guess.length != WORD_LENGTH) return@forEach

            val validation = validateGuessUseCase(guess, targetWord)
            guess.forEachIndexed { index, char ->
                val state = validation.letterStates.getOrNull(index)
                    ?: app.wordgame.domain.model.LetterState.EMPTY
                val key = char.uppercase()
                val previous = map[key] ?: app.wordgame.domain.model.LetterState.EMPTY

                when {
                    state == app.wordgame.domain.model.LetterState.CORRECT ->
                        map[key] = app.wordgame.domain.model.LetterState.CORRECT

                    state == app.wordgame.domain.model.LetterState.PRESENT &&
                            previous != app.wordgame.domain.model.LetterState.CORRECT ->
                        map[key] = app.wordgame.domain.model.LetterState.PRESENT

                    previous == app.wordgame.domain.model.LetterState.EMPTY ->
                        map[key] = app.wordgame.domain.model.LetterState.WRONG
                }
            }
        }
    }

    fun getLetterState(
        letter: Char,
        position: Int,
        guessIndex: Int
    ): app.wordgame.domain.model.LetterState {
        val state = _uiState.value
        if (guessIndex >= state.guesses.size) return app.wordgame.domain.model.LetterState.EMPTY

        val guess = state.guesses[guessIndex]
        val result = validateGuessUseCase(guess, state.targetWord)
        return result.letterStates.getOrNull(position)
            ?: app.wordgame.domain.model.LetterState.EMPTY
    }

    fun getKeyState(key: String): app.wordgame.domain.model.LetterState {
        if (key.length != 1) return app.wordgame.domain.model.LetterState.EMPTY
        return keyboardStates[currentLanguage]?.get(key.uppercase())
            ?: app.wordgame.domain.model.LetterState.EMPTY
    }

    // ─────────────────────────────────────────────
    //  SAUVEGARDE
    // ─────────────────────────────────────────────

    private fun saveGameState() {
        viewModelScope.launch {
            val s = _uiState.value
            val gs = app.wordgame.domain.model.GameState(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                word = s.targetWord,
                guesses = s.guesses,
                gameOver = s.gameOver,
                won = s.won,
                gameStartTime = s.gameStartTime,
                gameEndTime = s.gameEndTime,
                extraTriesGranted = s.extraTriesGranted
            )
            saveGameStateUseCase(gs, currentLanguage)
        }
    }
}