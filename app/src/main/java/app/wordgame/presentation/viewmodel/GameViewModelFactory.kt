package app.wordgame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameViewModelFactory(
    private val repository: app.wordgame.data.repository.GameRepositoryImpl,
    private val getDailyWordUseCase: app.wordgame.domain.usecase.GetDailyWordUseCase,
    private val saveGameStateUseCase: app.wordgame.domain.usecase.SaveGameStateUseCase,
    private val loadGameStateUseCase: app.wordgame.domain.usecase.LoadGameStateUseCase,
    private val saveStatsUseCase: app.wordgame.domain.usecase.SaveStatsUseCase,
    private val loadStatsUseCase: app.wordgame.domain.usecase.LoadStatsUseCase,
    private val validateGuessUseCase: app.wordgame.domain.usecase.ValidateGuessUseCase,
    private val updateStatsUseCase: app.wordgame.domain.usecase.UpdateStatsUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(
                getDailyWordUseCase = getDailyWordUseCase,
                saveGameStateUseCase = saveGameStateUseCase,
                loadGameStateUseCase = loadGameStateUseCase,
                saveStatsUseCase = saveStatsUseCase,
                loadStatsUseCase = loadStatsUseCase,
                validateGuessUseCase = validateGuessUseCase,
                updateStatsUseCase = updateStatsUseCase,
                repository = repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
