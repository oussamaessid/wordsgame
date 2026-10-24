package app.wordgame.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    val settings = remember { context.getSharedPreferences("WordGame_Settings", 0) }
    var languageCode by rememberSaveable { mutableStateOf(settings.getString("last_language", "fr") ?: "fr") }
    val language = Language.values().firstOrNull { it.code == languageCode } ?: Language.FRENCH
    val english = language == Language.ENGLISH
    fun label(fr: String, en: String) = if (english) en else fr
    var day by remember { mutableStateOf(PlayerPeriods.today().toString()) }
    var game by remember(language) { mutableStateOf<GameState?>(null) }
    var stats by remember(language) { mutableStateOf(GameStats()) }
    LaunchedEffect(language, day) {
        game = local.loadGameState(language)
        // Keep migration and archives intact even though history is no longer shown here.
        game?.takeIf { it.gameOver }?.let { local.saveGameState(it, language) }
        stats = local.loadStats(language).asOf(day)
    }
    LaunchedEffect(Unit) { while (true) { delay(1000); day = PlayerPeriods.today().toString() } }
    val today = game?.takeIf { it.date == day }
    val blue = Color(0xFF1769E0)
    val ink = Color(0xFF18243B)
    val muted = Color(0xFF69758A)
    val locale = if (english) Locale.ENGLISH else Locale.FRENCH
    val dateLabel = LocalDate.parse(day).format(DateTimeFormatter.ofPattern(if (english) "EEEE, MMMM d" else "EEEE d MMMM", locale))
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFF5F7FC)).navigationBarsPadding()) {
        val compact = maxHeight < 640.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.2f
        val tight = maxHeight < 460.dp
        val gap = if (tight) 6.dp else if (compact) 12.dp else 20.dp
        Column(Modifier.fillMaxSize().padding(horizontal = if (tight) 16.dp else 22.dp, vertical = if (tight) 10.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(gap)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (!compact) Text(label("VOTRE PAUSE QUOTIDIENNE", "YOUR DAILY BREAK"), fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = blue)
                Text("DailyWord", fontSize = if (tight) 24.sp else 32.sp, fontWeight = FontWeight.ExtraBold, color = ink, letterSpacing = (-1).sp)
                if (!compact) Text(label("Un petit défi. Une belle habitude.", "A little challenge. A great habit."), color = muted, fontSize = 14.sp)
            }
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE9EEF7)) {
                Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Language.values().forEach { item ->
                        val active = language == item
                        Surface(onClick = { languageCode = item.code; settings.edit().putString("last_language", item.code).apply() },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = if (active) Color.White else Color.Transparent) {
                            Box(Modifier.heightIn(min = 48.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                                Text(item.displayName, color = if (active) blue else muted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 6.dp, color = blue) {
                Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1263DD), Color(0xFF3549AD))))
                    .padding(if (tight) 12.dp else if (compact) 16.dp else 22.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp)) {
                    Text(when { today?.gameOver == true -> label("✓  DÉFI TERMINÉ", "✓  CHALLENGE COMPLETE")
                        today?.guesses?.isNotEmpty() == true -> label("●  EN COURS", "●  IN PROGRESS")
                        else -> label("●  DISPONIBLE AUJOURD’HUI", "●  AVAILABLE TODAY") },
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color(0xFFDCE7FF))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(label("Le mot du jour", "Today’s word"), fontSize = if (tight) 22.sp else 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(dateLabel.replaceFirstChar { it.titlecase(locale) }, color = Color(0xFFDCE7FF), fontSize = 12.sp)
                    }
                    if (!compact) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            repeat(5) { index ->
                                Surface(Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(10.dp),
                                    color = when (index) { 0 -> Color(0xFF71B591); 1 -> Color(0xFFE2BC63); else -> Color.White.copy(alpha = 0.18f) }) {
                                    Box(contentAlignment = Alignment.Center) { Text("?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                    if (!tight) Text(when { today?.gameOver == true -> if (today.won) label("Bien joué ! Votre résultat vous attend.", "Well done! Your result is ready.") else label("Demain, une nouvelle chance !", "A fresh chance tomorrow!")
                        today?.guesses?.isNotEmpty() == true -> label("${today.guesses.size} essai(s) déjà joué(s).", "${today.guesses.size} guess(es) made.")
                        else -> label("5 lettres à trouver. À vous de jouer.", "5 letters to find. Your turn.") }, color = Color.White, fontSize = 13.sp)
                    Button(onClick = { onLanguageSelected(language) }, shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = blue), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(when { today?.gameOver == true -> label("Consulter le résultat", "View result"); today?.guesses?.isNotEmpty() == true -> label("Reprendre", "Resume"); else -> label("Jouer", "Play") }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (!compact) Text(label("Nouveau mot à minuit · 4 essais + 2 bonus vidéo", "New word at midnight · 4 guesses + 2 video bonuses"), color = Color(0xFFDCE7FF), fontSize = 11.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(if (tight) 6.dp else 10.dp)) {
                Text(label("Votre progression", "Your progress"), color = ink, fontWeight = FontWeight.Bold, fontSize = if (tight) 15.sp else 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeMetric(label("Parties", "Played"), stats.totalPlayed.toString(), blue, Modifier.weight(1f), compact)
                    HomeMetric(label("Victoires", "Wins"), stats.wins.toString(), Color(0xFF258160), Modifier.weight(1f), compact)
                    HomeMetric(label("Réussite", "Win rate"), "${stats.winRate}%", blue, Modifier.weight(1f), compact)
                }
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFFF1DC)) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = if (tight) 6.dp else 10.dp)) {
                        Text(label("Série : ${stats.currentStreak} jour(s) · Record : ${stats.maxStreak}", "Streak: ${stats.currentStreak} day(s) · Best: ${stats.maxStreak}"),
                            color = Color(0xFF83643B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier, compact: Boolean = false) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(Modifier.padding(horizontal = 6.dp, vertical = if (compact) 8.dp else 16.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, color = accent, fontSize = if (compact) 21.sp else 25.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF69758A), fontSize = 11.sp)
        }
    }
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
