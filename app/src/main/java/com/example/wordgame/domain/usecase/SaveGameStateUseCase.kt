package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.repository.GameRepository

class SaveGameStateUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(state: GameState, language: Language) {
        repository.saveGameState(state, language)
    }
}
