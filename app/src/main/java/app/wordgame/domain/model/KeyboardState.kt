package app.wordgame.domain.model

data class KeyboardState(
    val letterStates: Map<String, LetterState> = emptyMap()
)