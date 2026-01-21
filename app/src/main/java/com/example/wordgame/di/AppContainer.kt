package com.example.wordgame.di

import android.content.Context
import com.example.wordgame.data.datasource.LocalDataSource
import com.example.wordgame.data.datasource.SharedPreferencesDataSource
import com.example.wordgame.data.repository.GameRepositoryImpl
import com.example.wordgame.domain.repository.GameRepository
import com.example.wordgame.domain.usecase.*
import com.example.wordgame.presentation.viewmodel.GameViewModelFactory

object AppContainer {
    private lateinit var localDataSource: LocalDataSource
    private lateinit var gameRepository: GameRepository

    fun initialize(context: Context) {
        localDataSource = SharedPreferencesDataSource(context)
        gameRepository = GameRepositoryImpl(localDataSource)
    }

    fun provideViewModelFactory(): GameViewModelFactory {
        return GameViewModelFactory(
            getDailyWordUseCase = GetDailyWordUseCase(gameRepository),
            saveGameStateUseCase = SaveGameStateUseCase(gameRepository),
            loadGameStateUseCase = LoadGameStateUseCase(gameRepository),
            saveStatsUseCase = SaveStatsUseCase(gameRepository),
            loadStatsUseCase = LoadStatsUseCase(gameRepository),
            validateGuessUseCase = ValidateGuessUseCase(),
            updateStatsUseCase = UpdateStatsUseCase()
        )
    }
}