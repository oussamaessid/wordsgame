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
    val stats: app.wordgame.domain.model.GameStats = _root_ide_package_.app.wordgame.domain.model.GameStats(),
    val showStats: Boolean = false,
    val targetWord: String = "",
    val isLoading: Boolean = true,
    val gameStartTime: Long = 0L,
    val gameEndTime: Long = 0L,
    val showRewardedAdDialog: Boolean = false  // NOUVEAU
)

class GameViewModel(
    private val getDailyWordUseCase: app.wordgame.domain.usecase.GetDailyWordUseCase,
    private val saveGameStateUseCase: app.wordgame.domain.usecase.SaveGameStateUseCase,
    private val loadGameStateUseCase: app.wordgame.domain.usecase.LoadGameStateUseCase,
    private val saveStatsUseCase: app.wordgame.domain.usecase.SaveStatsUseCase,
    private val loadStatsUseCase: app.wordgame.domain.usecase.LoadStatsUseCase,
    private val validateGuessUseCase: app.wordgame.domain.usecase.ValidateGuessUseCase,
    private val updateStatsUseCase: app.wordgame.domain.usecase.UpdateStatsUseCase,
    private val repository: app.wordgame.data.repository.GameRepositoryImpl
) : ViewModel() {

    companion object {
        const val WORD_LENGTH = 5
        const val MAX_ATTEMPTS = 5  // 5 tentatives normales (pas 6)
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentLanguage: app.wordgame.domain.model.Language = _root_ide_package_.app.wordgame.domain.model.Language.ENGLISH
    private val keyboardStates = mutableMapOf<app.wordgame.domain.model.Language, MutableMap<String, app.wordgame.domain.model.LetterState>>()

    fun initializeGame(language: app.wordgame.domain.model.Language) {
        currentLanguage = language
        keyboardStates.putIfAbsent(language, mutableMapOf())

        viewModelScope.launch {
            repository.loadWordsFromUrl()

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val targetWord = getDailyWordUseCase(language, today).takeIf { it.isNotEmpty() } ?: "ABCDE"

            val savedState = loadGameStateUseCase(language, today)
            val stats = loadStatsUseCase(language)

            if (savedState != null && savedState.word == targetWord && savedState.date == today) {
                _uiState.value = GameUiState(
                    guesses = savedState.guesses,
                    gameOver = savedState.gameOver,
                    won = savedState.won,
                    stats = stats,
                    targetWord = targetWord,
                    showStats = savedState.gameOver,
                    isLoading = false,
                    gameStartTime = savedState.gameStartTime,
                    gameEndTime = savedState.gameEndTime
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

    private fun updateKeyboardFromGuesses(guesses: List<String>, targetWord: String) {
        val map = keyboardStates[currentLanguage] ?: return

        guesses.forEach { guess ->
            if (guess.length != WORD_LENGTH) return@forEach

            val validation = validateGuessUseCase(guess, targetWord)
            guess.forEachIndexed { index, char ->
                val state = validation.letterStates.getOrNull(index) ?: _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY
                val key = char.uppercase()

                val previous = map[key] ?: _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY

                when {
                    state == _root_ide_package_.app.wordgame.domain.model.LetterState.CORRECT -> map[key] = _root_ide_package_.app.wordgame.domain.model.LetterState.CORRECT
                    state == _root_ide_package_.app.wordgame.domain.model.LetterState.PRESENT && previous != _root_ide_package_.app.wordgame.domain.model.LetterState.CORRECT -> map[key] = _root_ide_package_.app.wordgame.domain.model.LetterState.PRESENT
                    previous == _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY -> map[key] = _root_ide_package_.app.wordgame.domain.model.LetterState.WRONG
                }
            }
        }
    }

    fun onKeyPressed(key: String) {
        val state = _uiState.value
        if (state.gameOver) return

        when (key.uppercase()) {
            "ENTER" -> handleEnter()
            "⌫", "BACKSPACE" -> handleDelete()
            else -> if (key.length == 1 && key[0].isLetter()) handleLetterInput(key.uppercase())
        }
    }

    private fun handleEnter() {
        val state = _uiState.value
        if (state.currentGuess.length != WORD_LENGTH) return

        val result = validateGuessUseCase(state.currentGuess, state.targetWord)
        val newGuesses = state.guesses + result.guess
        val won = result.isCorrect

        _uiState.value = state.copy(
            guesses = newGuesses,
            currentGuess = ""
        )

        updateKeyboardFromGuesses(listOf(state.currentGuess), state.targetWord)

        // ⚠️ IMPORTANT: Vérifier la VICTOIRE en PREMIER
        when {
            won -> {
                // VICTOIRE (peut être à la ligne 1, 2, 3, 4, 5 ou 6) ✅
                val endTime = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    won = true,
                    gameOver = true,
                    gameEndTime = endTime
                )
                handleGameOver(true)
            }
            newGuesses.size > MAX_ATTEMPTS -> {
                // PERDU APRÈS 6 ESSAIS (ligne bonus utilisée et incorrecte) ❌
                val endTime = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    gameOver = true,
                    won = false,
                    gameEndTime = endTime
                )
                handleGameOver(false)
            }
            newGuesses.size >= MAX_ATTEMPTS -> {
                // PERDU APRÈS 5 ESSAIS → AFFICHER LE DIALOGUE (PAS gameOver encore) 🎁
                _uiState.value = _uiState.value.copy(
                    showRewardedAdDialog = true  // Juste le dialogue, pas gameOver
                )
                saveGameState()
            }
            else -> {
                // CONTINUER À JOUER (lignes 1, 2, 3, 4) ⏩
                saveGameState()
            }
        }
    }

    private fun handleDelete() {
        val state = _uiState.value
        if (state.currentGuess.isNotEmpty()) {
            _uiState.value = state.copy(currentGuess = state.currentGuess.dropLast(1))
        }
    }

    private fun handleLetterInput(letter: String) {
        val state = _uiState.value
        if (state.currentGuess.length < WORD_LENGTH) {
            _uiState.value = state.copy(currentGuess = state.currentGuess + letter)
        }
    }

    private fun handleGameOver(won: Boolean) {
        viewModelScope.launch {
            val newStats = updateStatsUseCase(_uiState.value.stats, won)
            _uiState.value = _uiState.value.copy(stats = newStats, showStats = true)
            saveStatsUseCase(newStats, currentLanguage)
            saveGameState()
        }
    }

    /**
     * Ajouter un essai bonus après avoir regardé la vidéo
     * AJOUTE physiquement une 6ème ligne vide
     */
    fun addExtraTry() {
        val state = _uiState.value
        if (state.guesses.size == MAX_ATTEMPTS && !state.gameOver) {
            // AJOUTER une ligne vide pour la 6ème tentative
            val emptyRow = ""  // Ligne vide de 5 caractères vides

            _uiState.value = state.copy(
                showRewardedAdDialog = false,
                currentGuess = ""  // Réinitialiser la saisie en cours
            )
            // gameOver reste false, le joueur peut jouer la ligne 6
            saveGameState()
        }
    }

    /**
     * Terminer le jeu comme perdu (appelé quand l'utilisateur refuse la vidéo)
     */
    fun finishGameAsLost() {
        val state = _uiState.value
        if (state.guesses.size == MAX_ATTEMPTS && !state.won) {
            val endTime = System.currentTimeMillis()
            _uiState.value = state.copy(
                showRewardedAdDialog = false,
                gameOver = true,
                gameEndTime = endTime
            )
            handleGameOver(false)
        }
    }

    fun toggleStatsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStats = show)
    }

    fun hideRewardedAdDialog() {
        _uiState.value = _uiState.value.copy(showRewardedAdDialog = false)
    }

    private fun saveGameState() {
        viewModelScope.launch {
            val s = _uiState.value
            val gs = _root_ide_package_.app.wordgame.domain.model.GameState(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                word = s.targetWord,
                guesses = s.guesses,
                gameOver = s.gameOver,
                won = s.won,
                gameStartTime = s.gameStartTime,
                gameEndTime = s.gameEndTime
            )
            saveGameStateUseCase(gs, currentLanguage)
        }
    }

    fun getLetterState(letter: Char, position: Int, guessIndex: Int): app.wordgame.domain.model.LetterState {
        val state = _uiState.value
        if (guessIndex >= state.guesses.size) return _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY

        val guess = state.guesses[guessIndex]
        val result = validateGuessUseCase(guess, state.targetWord)
        return result.letterStates.getOrNull(position) ?: _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY
    }

    fun getKeyState(key: String): app.wordgame.domain.model.LetterState {
        if (key.length != 1) return _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY
        return keyboardStates[currentLanguage]?.get(key.uppercase()) ?: _root_ide_package_.app.wordgame.domain.model.LetterState.EMPTY
    }
}