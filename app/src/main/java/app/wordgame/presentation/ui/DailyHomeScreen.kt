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
    val blue = Color(0xFF1769E0)
    val ink = Color(0xFF18243B)
    val muted = Color(0xFF69758A)
    val dateLabel = LocalDate.parse(day).format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F7FC)).navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("VOTRE PAUSE QUOTIDIENNE", fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = blue)
            Text("DailyWord", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = ink, letterSpacing = (-1).sp)
            Text("Un petit défi. Une belle habitude.", color = muted, style = MaterialTheme.typography.bodyMedium)
        }
        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFE9EEF7)) {
            Row(Modifier.fillMaxWidth().padding(5.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Language.values().forEach { item ->
                    val active = language == item
                    Surface(onClick = {
                        languageCode = item.code
                        context.getSharedPreferences("WordGame_Settings", 0).edit().putString("last_language", item.code).apply()
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                        color = if (active) Color.White else Color.Transparent) {
                        Box(Modifier.heightIn(min = 48.dp).padding(10.dp), contentAlignment = Alignment.Center) {
                            Text(item.displayName, color = if (active) blue else muted, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Surface(shape = RoundedCornerShape(28.dp), shadowElevation = 8.dp, color = blue) {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1263DD), Color(0xFF3549AD))))
                .padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.16f)) {
                    Text(when { today?.gameOver == true -> "✓  DÉFI TERMINÉ"
                        today?.guesses?.isNotEmpty() == true -> "●  EN COURS"
                        else -> "●  DISPONIBLE AUJOURD’HUI" },
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color.White)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Le mot du jour", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(dateLabel.replaceFirstChar { it.titlecase(Locale.FRENCH) }, color = Color(0xFFDCE7FF), fontSize = 13.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(5) { index ->
                        Surface(Modifier.weight(1f).aspectRatio(1f), shape = RoundedCornerShape(10.dp),
                            color = listOf(Color(0xFF71B591), Color(0xFFE2BC63), Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.18f))[index]) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("?", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Text(when { today?.gameOver == true -> if (today.won) "Bien joué ! Votre résultat vous attend." else "Défi relevé. Demain, une nouvelle chance !"
                    today?.guesses?.isNotEmpty() == true -> "${today.guesses.size} essai(s) déjà joué(s). À vous de continuer."
                    else -> "5 lettres à trouver. À vous de jouer." }, color = Color.White, fontSize = 14.sp)
                Button(onClick = { onLanguageSelected(language) }, shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = blue),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
                    Text(when { today?.gameOver == true -> "Consulter le résultat"; today?.guesses?.isNotEmpty() == true -> "Reprendre"; else -> "Jouer" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text("Nouveau mot à minuit · 4 essais + 2 bonus vidéo", color = Color(0xFFDCE7FF), fontSize = 11.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Votre progression", color = ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeMetric("Parties", stats.totalPlayed.toString(), blue, Modifier.weight(1f))
                HomeMetric("Victoires", stats.wins.toString(), Color(0xFF258160), Modifier.weight(1f))
                HomeMetric("Réussite", "${stats.winRate}%", blue, Modifier.weight(1f))
            }
            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFFFF1DC)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("✦", color = Color(0xFFAD6B11), fontSize = 26.sp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("${stats.currentStreak} jour(s) de victoires", fontWeight = FontWeight.Bold, color = ink)
                        Text("Votre record : ${stats.maxStreak} jour(s)", fontSize = 12.sp, color = Color(0xFF83643B))
                    }
                }
            }
            Text("Statistiques en ${language.displayName.lowercase(Locale.FRENCH)}", fontSize = 11.sp, color = muted)
        }
        Surface(onClick = { help = true }, shape = RoundedCornerShape(18.dp), color = Color.White) {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("?", color = blue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f)) {
                    Text("Comment jouer", color = ink, fontWeight = FontWeight.SemiBold)
                    Text("Découvrez les règles en quelques essais", color = muted, fontSize = 12.sp)
                }
                Text("›", fontSize = 26.sp, color = muted)
            }
        }
        if (history.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Vos derniers défis", color = ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Sauvegardés sur cet appareil · hors connexion", color = muted, fontSize = 12.sp)
                history.forEach { result ->
                    Surface(onClick = { selected = result }, shape = RoundedCornerShape(16.dp), color = Color.White) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (result.won) "✓" else "•", color = if (result.won) Color(0xFF258160) else muted, fontSize = 24.sp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(LocalDate.parse(result.date).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)), color = ink, fontWeight = FontWeight.SemiBold)
                                Text("${if (result.won) "Gagné" else "Terminé"} · ${result.guesses.size} essai(s)", color = muted, fontSize = 12.sp)
                            }
                            Text("›", color = muted, fontSize = 24.sp)
                        }
                    }
                }
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
private fun HomeMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(value, color = accent, fontSize = 25.sp, fontWeight = FontWeight.Bold)
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
