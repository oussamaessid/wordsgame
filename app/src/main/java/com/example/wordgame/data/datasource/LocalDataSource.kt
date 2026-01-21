package com.example.wordgame.data.datasource

import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language

interface LocalDataSource {
    suspend fun saveGameState(state: GameState, language: Language)
    suspend fun loadGameState(language: Language): GameState?
    suspend fun saveStats(stats: GameStats, language: Language)
    suspend fun loadStats(language: Language): GameStats
}