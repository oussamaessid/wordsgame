package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.repository.GameRepository

class LoadGameStateUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(language: Language, currentDate: String): GameState? {
        return repository.loadGameState(language, currentDate)
    }
}