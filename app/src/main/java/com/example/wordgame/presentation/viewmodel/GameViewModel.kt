// ========================================================================
// GameViewModel.kt   →  version corrigée + clavier séparé par langue
// ========================================================================

package com.example.wordgame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.model.LetterState
import com.example.wordgame.domain.usecase.*
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
    val stats: GameStats = GameStats(),
    val showStats: Boolean = false,
    val targetWord: String = "",
    val isLoading: Boolean = true
)

class GameViewModel(
    private val getDailyWordUseCase: GetDailyWordUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val loadGameStateUseCase: LoadGameStateUseCase,
    private val saveStatsUseCase: SaveStatsUseCase,
    private val loadStatsUseCase: LoadStatsUseCase,
    private val validateGuessUseCase: ValidateGuessUseCase,
    private val updateStatsUseCase: UpdateStatsUseCase
) : ViewModel() {

    companion object {
        const val WORD_LENGTH = 5
        const val MAX_ATTEMPTS = 6
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentLanguage: Language = Language.ENGLISH  // valeur par défaut peu importe

    // État du clavier **complètement séparé** par langue
    private val keyboardStates = mutableMapOf<Language, MutableMap<String, LetterState>>()

    fun initializeGame(language: Language) {
        currentLanguage = language

        // S'assurer que le map existe pour cette langue
        keyboardStates.putIfAbsent(language, mutableMapOf())

        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val targetWord = getDailyWordUseCase(language, today)
            val savedState = loadGameStateUseCase(language, today)
            val stats = loadStatsUseCase(language)

            if (savedState != null && savedState.word == targetWord) {
                // Partie déjà commencée → restaurer
                _uiState.value = GameUiState(
                    guesses = savedState.guesses,
                    gameOver = savedState.gameOver,
                    won = savedState.won,
                    stats = stats,
                    targetWord = targetWord,
                    showStats = savedState.gameOver,
                    isLoading = false
                )
                updateKeyboardFromGuesses(savedState.guesses, targetWord)
            } else {
                // Nouvelle partie → reset clavier pour cette langue
                _uiState.value = GameUiState(
                    stats = stats,
                    targetWord = targetWord,
                    isLoading = false
                )
                keyboardStates[language]?.clear()
            }
        }
    }

    private fun updateKeyboardFromGuesses(guesses: List<String>, targetWord: String) {
        val map = keyboardStates[currentLanguage] ?: return

        guesses.forEach { guess ->
            val validation = validateGuessUseCase(guess, targetWord)

            guess.forEachIndexed { index, char ->
                val state = validation.letterStates[index]
                val key = char.uppercase()

                val previous = map[key] ?: LetterState.EMPTY

                when {
                    state == LetterState.CORRECT -> {
                        map[key] = LetterState.CORRECT
                    }
                    state == LetterState.PRESENT && previous != LetterState.CORRECT -> {
                        map[key] = LetterState.PRESENT
                    }
                    previous == LetterState.EMPTY -> {
                        map[key] = LetterState.WRONG
                    }
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
            else -> if (key.length == 1 && key[0].isLetter()) {
                handleLetterInput(key.uppercase())
            }
        }
    }

    private fun handleEnter() {
        val state = _uiState.value
        if (state.currentGuess.length != WORD_LENGTH) return

        val result = validateGuessUseCase(state.currentGuess, state.targetWord)
        val newGuesses = state.guesses + result.guess
        val won = result.isCorrect
        val gameOver = won || newGuesses.size >= MAX_ATTEMPTS

        _uiState.value = state.copy(
            guesses = newGuesses,
            currentGuess = "",
            won = won,
            gameOver = gameOver
        )

        // Mise à jour clavier
        updateKeyboardFromGuesses(listOf(state.currentGuess), state.targetWord)

        if (gameOver) {
            handleGameOver(won)
        }

        saveGameState()
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
        }
    }

    fun toggleStatsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showStats = show)
    }

    private fun saveGameState() {
        viewModelScope.launch {
            val s = _uiState.value
            val gs = GameState(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                word = s.targetWord,
                guesses = s.guesses,
                gameOver = s.gameOver,
                won = s.won
            )
            saveGameStateUseCase(gs, currentLanguage)
        }
    }

    fun getLetterState(letter: Char, position: Int, guessIndex: Int): LetterState {
        val state = _uiState.value
        if (guessIndex >= state.guesses.size) return LetterState.EMPTY

        val guess = state.guesses[guessIndex]
        val result = validateGuessUseCase(guess, state.targetWord)
        return result.letterStates[position]
    }

    fun getKeyState(key: String): LetterState {
        if (key.length != 1) return LetterState.EMPTY
        return keyboardStates[currentLanguage]?.get(key.uppercase()) ?: LetterState.EMPTY
    }
}