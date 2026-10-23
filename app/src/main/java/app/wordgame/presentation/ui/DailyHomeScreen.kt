package app.wordgame.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.wordgame.domain.model.*
import app.wordgame.data.datasource.SharedPreferencesDataSource
import kotlinx.coroutines.delay

@Composable
fun DailyHomeScreen(onLanguageSelected: (Language) -> Unit) {
    val context = LocalContext.current
    val local = remember { SharedPreferencesDataSource(context) }
    var languageCode by rememberSaveable { mutableStateOf(context.getSharedPreferences("WordGame_Settings", 0).getString("last_language", "fr") ?: "fr") }
    val language = Language.values().first { it.code == languageCode }
    var day by remember { mutableStateOf(PlayerPeriods.today().toString()) }
    var game by remember { mutableStateOf<GameState?>(null) }
    var stats by remember { mutableStateOf(GameStats()) }
    var history by remember { mutableStateOf<List<GameState>>(emptyList()) }
    var selected by remember { mutableStateOf<GameState?>(null) }
    var help by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(language, day) {
        game = local.loadGameState(language)
        // Migrate an existing terminal save into the archive without counting it twice.
        game?.takeIf { it.gameOver }?.let { local.saveGameState(it, language) }
        stats = local.loadStats(language).asOf(day)
        history = local.history(language)
    }
    LaunchedEffect(Unit) { while (true) { delay(1000); day = PlayerPeriods.today().toString() } }
    val today = game?.takeIf { it.date == day }
    Column(Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("DailyWord", style = MaterialTheme.typography.headlineLarge, color = androidx.compose.ui.graphics.Color(0xFF1976D2))
        Text("Un mot, un rendez-vous chaque jour.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Language.values().forEach { item -> FilterChip(selected = item == language, onClick = { languageCode = item.code }, label = { Text(item.displayName) }) }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Défi du jour · $day", style = MaterialTheme.typography.titleLarge)
                Text(when { today?.gameOver == true -> if (today.won) "Terminé · Bravo !" else "Terminé · Revenez demain !"
                    today?.guesses?.isNotEmpty() == true -> "Commencé · ${today.guesses.size} essai(s)"
                    else -> "Disponible · 5 lettres à trouver" })
                Button(onClick = { onLanguageSelected(language) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(when { today?.gameOver == true -> "Consulter le résultat"; today?.guesses?.isNotEmpty() == true -> "Reprendre"; else -> "Jouer" })
                }
                Text("Un nouveau défi à minuit. 4 essais, jusqu’à 2 essais bonus avec une vidéo.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Vos statistiques · ${language.displayName}", style = MaterialTheme.typography.titleMedium)
        Text("${stats.totalPlayed} parties jouées · ${stats.wins} victoires · ${stats.winRate} % de réussite")
        Text("Série de victoires : ${stats.currentStreak} jour(s) · Record : ${stats.maxStreak}")
        OutlinedButton(onClick = { help = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Comment jouer") }
        if (history.isNotEmpty()) Text("Résultats sauvegardés · disponibles hors connexion", style = MaterialTheme.typography.titleMedium)
        history.forEach { result ->
            OutlinedButton(onClick = { selected = result }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("${result.date} · ${if (result.won) "Gagné" else "Terminé"} · ${result.guesses.size} essai(s)")
            }
        }
    }
    if (help) HowToPlayDialog { help = false }
    selected?.let { result -> StatsDialog(won = result.won, gameOver = true, attempts = result.guesses.size,
        targetWord = result.word, stats = stats, onDismiss = { selected = null }, language = language,
        gameStartTime = result.gameStartTime, gameEndTime = result.gameEndTime, day = result.date,
        guesses = result.guesses, maxAttempts = 4 + result.extraTriesGranted) }
}

@Composable
fun TutorialScreen(onDone: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    androidx.activity.compose.BackHandler { onDone() }
    Surface(shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Bienvenue dans DailyWord", style = MaterialTheme.typography.headlineSmall)
            Text("Entraînement ${minOf(step + 1, 3)}/3 · sans effet sur votre défi ni vos statistiques.")
            Text("Trouvez un mot de 5 lettres. Essayons CARTE pour découvrir les indices.")
            if (step >= 1) {
                val result = app.wordgame.domain.usecase.ValidateGuessUseCase()("CARTE", "PLAGE")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    "CARTE".forEachIndexed { index, char -> LetterCell(char.toString(), result.letterStates[index], 36.dp) }
                }
                Text("Vert : E est à la bonne place. Jaune : A est présent, à une autre place. Gris : C, R et T sont absents.")
                Text("Les lettres répétées sont colorées selon leur nombre dans le mot.")
            }
            if (step >= 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { "PLAGE".forEach { LetterCell(it.toString(), LetterState.CORRECT, 36.dp) } }
                Text("Bravo ! PLAGE est le mot d’exemple. Dans le défi : saisissez un mot puis validez. Vous avez 4 essais, avec jusqu’à 2 essais bonus proposés par vidéo.")
                Text("Un nouveau mot chaque jour dans chaque langue. Partagez votre grille sans révéler la réponse !")
                Text("Le chronomètre s’arrête lorsque vous quittez le jeu. Le classement compare les mots trouvés puis le temps total, par jour, semaine ou mois.")
            }
            Button(onClick = { if (step < 2) step++ else onDone() }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(when (step) { 0 -> "Essayer CARTE"; 1 -> "Essayer PLAGE"; else -> "Jouer au défi du jour" })
            }
            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Passer le tutoriel") }
        }
    }
}
