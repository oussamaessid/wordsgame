package com.example.wordgame.domain.repository

import com.example.wordgame.domain.model.GameState
import com.example.wordgame.domain.model.GameStats
import com.example.wordgame.domain.model.Language


interface GameRepository {
    suspend fun saveGameState(state: GameState, language: Language)
    suspend fun loadGameState(language: Language, currentDate: String): GameState?
    suspend fun saveStats(stats: GameStats, language: Language)
    suspend fun loadStats(language: Language): GameStats
    fun getDailyWord(language: Language, date: String): String
}