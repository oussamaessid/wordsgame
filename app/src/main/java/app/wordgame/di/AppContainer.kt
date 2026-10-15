package app.wordgame.di

import android.content.Context

object AppContainer {

    private lateinit var localDataSource: app.wordgame.data.datasource.LocalDataSource

    // Interface (pour les use cases)
    private lateinit var gameRepository: app.wordgame.domain.repository.GameRepository

    // Impl concrète (pour le ViewModel)
    private lateinit var gameRepositoryImpl: app.wordgame.data.repository.GameRepositoryImpl

    lateinit var players: app.wordgame.data.repository.PlayerRepository
        private set

    fun initialize(context: Context) {
        players = app.wordgame.data.repository.PlayerRepository(context.getSharedPreferences("players", Context.MODE_PRIVATE), app.wordgame.data.repository.FirebasePlayers(context.applicationContext))
        localDataSource =
            app.wordgame.data.datasource.SharedPreferencesDataSource(context)

        gameRepositoryImpl =
            app.wordgame.data.repository.GameRepositoryImpl(localDataSource)
        gameRepository = gameRepositoryImpl
    }

    fun provideViewModelFactory(): app.wordgame.presentation.viewmodel.GameViewModelFactory {
        return app.wordgame.presentation.viewmodel.GameViewModelFactory(
            repository = gameRepositoryImpl,
            getDailyWordUseCase = app.wordgame.domain.usecase.GetDailyWordUseCase(
                gameRepository
            ),
            saveGameStateUseCase = app.wordgame.domain.usecase.SaveGameStateUseCase(
                gameRepository
            ),
            loadGameStateUseCase = app.wordgame.domain.usecase.LoadGameStateUseCase(
                gameRepository
            ),
            saveStatsUseCase = app.wordgame.domain.usecase.SaveStatsUseCase(
                gameRepository
            ),
            loadStatsUseCase = app.wordgame.domain.usecase.LoadStatsUseCase(
                gameRepository
            ),
            validateGuessUseCase = app.wordgame.domain.usecase.ValidateGuessUseCase(),
            updateStatsUseCase = app.wordgame.domain.usecase.UpdateStatsUseCase(),
            isValidWordUseCase = app.wordgame.domain.usecase.IsValidWordUseCase(
                gameRepository
            )
        )
    }
}
