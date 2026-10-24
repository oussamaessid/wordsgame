package app.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun StatsDialog(
    won: Boolean,
    gameOver: Boolean,
    attempts: Int,
    targetWord: String,
    stats: app.wordgame.domain.model.GameStats,
    onDismiss: () -> Unit,
    language: app.wordgame.domain.model.Language,
    gameStartTime: Long = 0L,
    gameEndTime: Long = 0L,
    onRanking: (() -> Unit)? = null,
    day: String = app.wordgame.domain.model.PlayerPeriods.today().toString(),
    guesses: List<String> = emptyList(),
    maxAttempts: Int = 6
) {
    val config = LocalConfiguration.current
    val screenHeightDp = config.screenHeightDp
    val screenWidthDp  = config.screenWidthDp

    val isVerySmall = screenHeightDp < 520
    val isSmall     = screenHeightDp < 680

    // Compte à rebours jusqu'à minuit
    var timeUntilMidnight by remember { mutableStateOf(calculateTimeUntilMidnight()) }

    // Temps écoulé
    val timeElapsed = if (gameStartTime > 0 && gameEndTime > 0) {
        val diffMs  = gameEndTime - gameStartTime
        val minutes = diffMs.coerceAtLeast(0) / (1000 * 60)
        val seconds = (diffMs / 1000) % 60
        String.format("%02d:%02d", minutes, seconds)
    } else {
        "00:00"
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeUntilMidnight = calculateTimeUntilMidnight()
        }
    }

    val translations = if (language == app.wordgame.domain.model.Language.FRENCH) {
        mapOf(
            "victory"      to "🎉 Félicitations ! 🎉",
            "defeat"       to "😢 Dommage !",
            "correct_word" to "Le mot était",
            "attempts"     to "Temps",
            "play_again"   to "Prochain mot dans",
            "statistics"   to "Statistiques",
            "played"       to "Parties",
            "win_rate"     to "Réussite",
            "streak"       to "Série",
            "max_streak"   to "Record",
            "close"        to "FERMER"
        )
    } else {
        mapOf(
            "victory"      to "🎉 Congratulations! 🎉",
            "defeat"       to "😢 Too bad!",
            "correct_word" to "The word was",
            "attempts"     to "Time",
            "play_again"   to "Next word in",
            "statistics"   to "Statistics",
            "played"       to "Played",
            "win_rate"     to "Win Rate",
            "streak"       to "Streak",
            "max_streak"   to "Max Streak",
            "close"        to "CLOSE"
        )
    }

    val contentPadding  = if (isVerySmall) 12.dp else if (isSmall) 16.dp else 20.dp
    val itemSpacing     = if (isVerySmall) 6.dp  else if (isSmall) 10.dp else 14.dp
    val titleFontSize   = if (isVerySmall) 18.sp else if (isSmall) 21.sp else 25.sp
    val sectionFontSize = if (isVerySmall) 15.sp else if (isSmall) 17.sp else 20.sp
    val buttonHeight    = if (isVerySmall) 42.dp else if (isSmall) 46.dp else 52.dp
    val buttonFontSize  = if (isVerySmall) 13.sp else 15.sp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(
                    when {
                        isVerySmall          -> 0.97f
                        isSmall              -> 0.93f
                        screenWidthDp < 380  -> 0.95f
                        else                 -> 0.88f
                    }
                )
                .heightIn(max = (screenHeightDp * 0.90f).dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {

                if (onRanking != null) {
                    FilledTonalIconButton(onClick = onRanking, modifier = Modifier.align(Alignment.End)) {
                        Icon(androidx.compose.ui.res.painterResource(app.wordgame.R.drawable.ic_trophy),
                            contentDescription = if (language == app.wordgame.domain.model.Language.FRENCH) "Classements" else "Leaderboards",
                            tint = Color(0xFFBA8518))
                    }
                }
                // ── Titre victoire / défaite ─────────────────────────────────
                if (gameOver) {
                    Text(
                        text = if (won) translations["victory"]!! else translations["defeat"]!!,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        color = if (won) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        textAlign = TextAlign.Center,
                        lineHeight = (titleFontSize.value + 4).sp
                    )
                }

                if (gameOver && !won && targetWord.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${translations["correct_word"]!!} : ${targetWord.uppercase()}",
                            fontSize = if (isVerySmall) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (gameOver && gameStartTime > 0 && gameEndTime > 0) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (won) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⏱️",
                                fontSize = if (isVerySmall) 14.sp else 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${translations["attempts"]!!} : ",
                                fontSize = if (isVerySmall) 13.sp else 15.sp,
                                color = Color(0xFF616161),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = timeElapsed,
                                fontSize = if (isVerySmall) 15.sp else 17.sp,
                                color = if (won) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                // ── Titre statistiques ───────────────────────────────────────
                Text(
                    text = translations["statistics"]!!,
                    fontSize = sectionFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )

                Text(if (language == app.wordgame.domain.model.Language.FRENCH) "${stats.wins} victoires" else "${stats.wins} wins")

                // ── Stats ligne 1 ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        title = translations["played"]!!,
                        value = stats.totalPlayed.toString(),
                        color = Color(0xFF1976D2),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = translations["win_rate"]!!,
                        value = "${stats.winRate}%",
                        color = Color(0xFF388E3C),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        title = translations["streak"]!!,
                        value = stats.currentStreak.toString(),
                        color = Color(0xFFFFB300),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = translations["max_streak"]!!,
                        value = stats.maxStreak.toString(),
                        color = Color(0xFFF57C00),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (gameOver) {
                    val profile = app.wordgame.di.AppContainer.players.currentProfile
                    val validator = app.wordgame.domain.usecase.ValidateGuessUseCase()
                    val grid = guesses.filter { it.length == targetWord.length && it.length == 5 }.map { guess ->
                        validator(guess, targetWord).letterStates.map { state -> when (state) {
                            app.wordgame.domain.model.LetterState.CORRECT -> app.wordgame.sharing.ShareTile.CORRECT
                            app.wordgame.domain.model.LetterState.PRESENT -> app.wordgame.sharing.ShareTile.PRESENT
                            else -> app.wordgame.sharing.ShareTile.ABSENT
                        } }
                    }
                    ShareResultButtons(app.wordgame.sharing.ShareResult(
                        game = "DailyWord", player = profile.name, day = day,
                        category = if (language == app.wordgame.domain.model.Language.FRENCH) "Mot du jour · Français" else "Daily word · English",
                        won = won, attempts = attempts, maxAttempts = maxAttempts,
                        durationMs = (gameEndTime - gameStartTime).takeIf { gameStartTime > 0 && gameEndTime > 0 && it > 0 },
                        english = language == app.wordgame.domain.model.Language.ENGLISH, wordGame = true,
                        grid = grid, avatarResource = AvatarGallery.icons.getOrElse(profile.avatar) { AvatarGallery.icons[11] }))
                }

                CountdownTimer(
                    timeText = translations["play_again"]!!,
                    timeRemaining = timeUntilMidnight,
                    isVerySmall = isVerySmall,
                    isSmall = isSmall
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(
                        text = translations["close"]!!,
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun CountdownTimer(
    timeText: String,
    timeRemaining: String,
    isVerySmall: Boolean,
    isSmall: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
            .padding(vertical = if (isVerySmall) 10.dp else 14.dp)
    ) {
        Text(
            text = timeText,
            fontSize = if (isVerySmall) 12.sp else 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                fontSize = if (isVerySmall) 18.sp else 22.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeRemaining,
                fontSize = when {
                    isVerySmall -> 20.sp
                    isSmall     -> 24.sp
                    else        -> 28.sp
                },
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    isVerySmall: Boolean = false,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    val boxSize = when {
        isVerySmall -> 66.dp
        isSmall     -> 80.dp
        else        -> 96.dp
    }
    val valueFontSize = when {
        isVerySmall -> 20.sp
        isSmall     -> 26.sp
        else        -> 32.sp
    }
    val titleFontSize = when {
        isVerySmall -> 9.sp
        isSmall     -> 10.sp
        else        -> 12.sp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = boxSize)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(14.dp))
                .border(2.dp, color, RoundedCornerShape(14.dp))
                .padding(horizontal = 4.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    lineHeight = (valueFontSize.value + 4).sp
                )
                Text(
                    text = title,
                    fontSize = titleFontSize,
                    color = Color(0xFF616161),
                    textAlign = TextAlign.Center,
                    lineHeight = (titleFontSize.value + 2).sp
                )
            }
        }
    }
}


private fun calculateTimeUntilMidnight(): String {
    val now = Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Tunis"))
    val midnight = Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Tunis")).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    if (now.after(midnight)) {
        midnight.add(Calendar.DAY_OF_MONTH, 1)
    }
    val diff    = midnight.timeInMillis - now.timeInMillis
    val hours   = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}