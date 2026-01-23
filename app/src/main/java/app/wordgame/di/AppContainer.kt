package app.wordgame.di

import android.content.Context

object AppContainer {

    private lateinit var localDataSource: app.wordgame.data.datasource.LocalDataSource

    // Interface (pour les use cases)
    private lateinit var gameRepository: app.wordgame.domain.repository.GameRepository

    // Impl concrète (pour le ViewModel)
    private lateinit var gameRepositoryImpl: app.wordgame.data.repository.GameRepositoryImpl

    fun initialize(context: Context) {
        localDataSource =
            _root_ide_package_.app.wordgame.data.datasource.SharedPreferencesDataSource(context)

        gameRepositoryImpl =
            _root_ide_package_.app.wordgame.data.repository.GameRepositoryImpl(localDataSource)
        gameRepository = gameRepositoryImpl
    }

    fun provideViewModelFactory(): app.wordgame.presentation.viewmodel.GameViewModelFactory {
        return _root_ide_package_.app.wordgame.presentation.viewmodel.GameViewModelFactory(
            repository = gameRepositoryImpl,
            getDailyWordUseCase = _root_ide_package_.app.wordgame.domain.usecase.GetDailyWordUseCase(
                gameRepository
            ),
            saveGameStateUseCase = _root_ide_package_.app.wordgame.domain.usecase.SaveGameStateUseCase(
                gameRepository
            ),
            loadGameStateUseCase = _root_ide_package_.app.wordgame.domain.usecase.LoadGameStateUseCase(
                gameRepository
            ),
            saveStatsUseCase = _root_ide_package_.app.wordgame.domain.usecase.SaveStatsUseCase(
                gameRepository
            ),
            loadStatsUseCase = _root_ide_package_.app.wordgame.domain.usecase.LoadStatsUseCase(
                gameRepository
            ),
            validateGuessUseCase = _root_ide_package_.app.wordgame.domain.usecase.ValidateGuessUseCase(),
            updateStatsUseCase = _root_ide_package_.app.wordgame.domain.usecase.UpdateStatsUseCase()
        )
    }
}
