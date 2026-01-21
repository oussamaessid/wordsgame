package com.example.wordgame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordgame.domain.usecase.*

class GameViewModelFactory(
    private val getDailyWordUseCase: GetDailyWordUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val loadGameStateUseCase: LoadGameStateUseCase,
    private val saveStatsUseCase: SaveStatsUseCase,
    private val loadStatsUseCase: LoadStatsUseCase,
    private val validateGuessUseCase: ValidateGuessUseCase,
    private val updateStatsUseCase: UpdateStatsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(
                getDailyWordUseCase,
                saveGameStateUseCase,
                loadGameStateUseCase,
                saveStatsUseCase,
                loadStatsUseCase,
                validateGuessUseCase,
                updateStatsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}