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
    var showStats by rememberSaveable { mutableStateOf(false) }
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
        val showDate = maxHeight >= 260.dp
        val landscape = maxWidth > maxHeight && maxWidth >= 480.dp
        val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
        val condensed = maxHeight < 300.dp || (fontScale > 1.3f && maxHeight < 560.dp)
        val compact = maxHeight < 640.dp || fontScale > 1.2f
        val tight = maxHeight < 520.dp
        val minimal = maxHeight < 380.dp
        val gap = if (tight) 6.dp else if (compact) 12.dp else 20.dp
        val challenge: @Composable () -> Unit = {
            Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 6.dp, color = blue) {
                Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1263DD), Color(0xFF3549AD))))
                    .padding(if (tight) 12.dp else if (compact) 16.dp else 22.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp)) {
                    if (showDate && !condensed) Text(when { today?.gameOver == true -> label("✓  DÉFI TERMINÉ", "✓  CHALLENGE COMPLETE")
                        today?.guesses?.isNotEmpty() == true -> label("●  EN COURS", "●  IN PROGRESS")
                        else -> label("●  DISPONIBLE AUJOURD’HUI", "●  AVAILABLE TODAY") },
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color(0xFFDCE7FF))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(label("Le mot du jour", "Today’s word"), fontSize = if (tight) 22.sp else 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        if (showDate) Text(dateLabel.replaceFirstChar { it.titlecase(locale) }, color = Color(0xFFDCE7FF), fontSize = 12.sp)
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
                    if (!tight && !landscape) Text(when { today?.gameOver == true -> if (today.won) label("Bien joué ! Votre résultat vous attend.", "Well done! Your result is ready.") else label("Demain, une nouvelle chance !", "A fresh chance tomorrow!")
                        today?.guesses?.isNotEmpty() == true -> label("${today.guesses.size} essai(s) déjà joué(s).", "${today.guesses.size} guess(es) made.")
                        else -> label("5 lettres à trouver. À vous de jouer.", "5 letters to find. Your turn.") }, color = Color.White, fontSize = 13.sp)
                    Button(onClick = { onLanguageSelected(language) }, shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = blue), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(when { today?.gameOver == true -> label("Consulter le résultat", "View result"); today?.guesses?.isNotEmpty() == true -> label("Reprendre", "Resume"); else -> label("Jouer", "Play") }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (!compact) Text(label("Nouveau mot à minuit · 4 essais + 2 bonus vidéo", "New word at midnight · 4 guesses + 2 video bonuses"), color = Color(0xFFDCE7FF), fontSize = 11.sp)
                }
            }
        }
        val progress: @Composable () -> Unit = {
            if (condensed) {
                OutlinedButton(onClick = { showStats = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(label("Statistiques", "Statistics"))
                }
            } else Column(verticalArrangement = Arrangement.spacedBy(if (tight) 6.dp else 10.dp)) {
                if (!minimal || landscape) Text(label("Votre progression", "Your progress"), color = ink, fontWeight = FontWeight.Bold, fontSize = if (tight) 15.sp else 18.sp)
                if (minimal && !landscape) {
                    Text(label("${stats.totalPlayed} parties · ${stats.wins} victoires · ${stats.winRate}%", "${stats.totalPlayed} played · ${stats.wins} wins · ${stats.winRate}%"), color = ink, fontSize = 12.sp)
                } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Column(Modifier.fillMaxSize().padding(horizontal = if (tight) 12.dp else 22.dp, vertical = if (tight) 6.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(gap)) {
            if (!minimal && !condensed) Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
            if (landscape) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1.15f)) { challenge() }
                    Box(Modifier.weight(1f)) { progress() }
                }
            } else {
                challenge()
                progress()
            }
        }
    }
    if (showStats) StatsDialog(won = false, gameOver = false, attempts = 0, targetWord = "", stats = stats,
        onDismiss = { showStats = false }, language = language)
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
fun TutorialScreen(language: Language = Language.FRENCH, onPlay: (() -> Unit)? = null, onDone: () -> Unit) {
    var step by rememberSaveable(language) { mutableIntStateOf(0) }
    val english = language == Language.ENGLISH
    fun label(fr: String, en: String) = if (english) en else fr
    val sample = if (english) "TRAIN" else "CARTE"
    val solution = if (english) "PLANT" else "PLAGE"
    val blue = Color(0xFF1769E0)
    val ink = Color(0xFF18243B)
    androidx.activity.compose.BackHandler { onDone() }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF0F5FF), Color(0xFFF8FAFF))))
        .navigationBarsPadding().padding(16.dp), contentAlignment = Alignment.Center) {
        val cardHeight = maxHeight
        Surface(Modifier.widthIn(max = 440.dp).fillMaxWidth().heightIn(max = cardHeight),
            shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 8.dp) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color(0xFFEDF3FF)) {
                    Text(label("COMMENT JOUER", "HOW TO PLAY"), Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(3) { index ->
                        Box(Modifier.weight(1f).height(4.dp).background(if (index <= step) blue else Color(0xFFE9EEF7), RoundedCornerShape(50)))
                    }
                }
                Text(label("Étape ${step + 1} sur 3", "Step ${step + 1} of 3"), color = Color(0xFF69758A), fontSize = 12.sp)
                Text(when (step) {
                    0 -> label("Bienvenue dans DailyWord", "Welcome to DailyWord")
                    1 -> label("Les couleurs vous guident", "Follow the colours")
                    else -> label("À vous de jouer !", "You’re ready to play!")
                }, color = ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(when (step) {
                    0 -> label("Un mot de 5 lettres. Quelques essais. Un nouveau défi chaque jour.", "One five-letter word. A few guesses. A fresh challenge every day.")
                    1 -> label("Chaque essai vous rapproche du mot secret.", "Every guess brings you closer to the secret word.")
                    else -> label("Vous avez trouvé le mot d’exemple.", "You found the practice word.")
                }, color = Color(0xFF69758A), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp)
                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF5F7FC)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (step == 0) {
                            PracticeRow(sample, List(5) { LetterState.EMPTY })
                            Text(label("Essayons $sample ensemble.", "Let’s try $sample together."), color = ink, fontSize = 13.sp)
                        } else {
                            PracticeRow(sample, app.wordgame.domain.usecase.ValidateGuessUseCase()(sample, solution).letterStates)
                            if (step == 2) PracticeRow(solution, List(5) { LetterState.CORRECT })
                        }
                    }
                }
                if (step == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TutorialHint(Color(0xFF4CAF50), label("Bien placée", "Right place"), label("E est à la bonne place.", "A is in the right place."))
                        TutorialHint(Color(0xFFFFC107), label("À déplacer", "Move it"), label("A est présent, mais ailleurs.", "T is in the word, somewhere else."))
                        TutorialHint(Color(0xFF9E9E9E), label("Absente", "Not in the word"), label("C, R et T ne sont pas dans le mot.", "R and I are not in the word."))
                        Text(label("Les lettres répétées respectent leur nombre dans le mot.", "Repeated letters are coloured according to how often they appear."), color = Color(0xFF69758A), fontSize = 12.sp)
                    }
                }
                if (step == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TutorialHint(blue, label("Votre rendez-vous quotidien", "Your daily challenge"), label("4 essais, jusqu’à 2 bonus vidéo. Un nouveau mot à minuit dans chaque langue.", "4 guesses, up to 2 video bonuses. A new word at midnight in each language."))
                        TutorialHint(Color(0xFF258160), label("Partagez votre réussite", "Share your achievement"), label("Votre grille de couleurs se partage sans révéler le mot.", "Share your coloured grid without giving away the answer."))
                        Text(label("Le chronomètre s’arrête hors du jeu. Le classement compare les victoires, puis le temps total.", "The timer pauses when you leave the game. Rankings compare wins, then total time."), color = Color(0xFF69758A), fontSize = 12.sp)
                    }
                }
                Text(label("Entraînement indépendant · aucun effet sur votre défi ou vos statistiques", "Separate practice · your daily challenge and statistics stay untouched"),
                    color = Color(0xFF69758A), fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Button(onClick = { if (step < 2) step++ else (onPlay ?: onDone)() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                    Text(when (step) { 0 -> label("Essayer $sample", "Try $sample"); 1 -> label("Essayer $solution", "Try $solution"); else -> label("Jouer au défi du jour", "Play today’s challenge") }, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(label("Passer le tutoriel", "Skip tutorial"), color = Color(0xFF69758A))
                }
            }
        }
    }
}

@Composable
private fun TutorialHint(color: Color, title: String, detail: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 3.dp).size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF18243B), fontSize = 13.sp)
            Text(detail, color = Color(0xFF69758A), fontSize = 12.sp)
        }
    }
}

@Composable
private fun PracticeRow(word: String, states: List<LetterState>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cell = minOf(36.dp, ((maxWidth - 16.dp) / 5).coerceAtLeast(1.dp))
        Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            word.forEachIndexed { index, letter -> LetterCell(letter.toString(), states[index], cell) }
        }
    }
}
