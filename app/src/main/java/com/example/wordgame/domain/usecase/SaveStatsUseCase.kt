package com.example.wordgame.domain.usecase

import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language
import com.example.wordgame.domain.repository.GameRepository

class SaveStatsUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(stats: GameStats, language: Language) {
        repository.saveStats(stats, language)
    }
}

