package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.repository.GameRepository

class LoadStatsUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(language: Language): GameStats {
        return repository.loadStats(language)
    }
}