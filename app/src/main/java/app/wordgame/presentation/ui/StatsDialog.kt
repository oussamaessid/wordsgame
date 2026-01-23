package app.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    gameEndTime: Long = 0L
) {
    val config = LocalConfiguration.current
    val screenHeightDp = config.screenHeightDp

    val isVerySmall = screenHeightDp < 520
    val isSmall     = screenHeightDp < 680
    val isMedium    = screenHeightDp >= 680

    // Compte à rebours jusqu'à minuit
    var timeUntilMidnight by remember { mutableStateOf(calculateTimeUntilMidnight()) }

    // Calculer le temps écoulé pour terminer le jeu
    val timeElapsed = if (gameStartTime > 0 && gameEndTime > 0) {
        val diffMs = gameEndTime - gameStartTime
        val minutes = (diffMs / (1000 * 60)) % 60
        val seconds = (diffMs / 1000) % 60
        String.format("%02d:%02d", minutes, seconds)
    } else {
        "00:00"
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Mettre à jour chaque seconde
            timeUntilMidnight = calculateTimeUntilMidnight()
        }
    }

    val translations = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) {
        mapOf(
            "victory"     to "🎉 Félicitations ! 🎉",
            "defeat"      to "😢 Dommage !",
            "correct_word" to "Le mot était",
            "attempts"    to "Temps",
            "play_again"  to "Prochain mot dans",
            "statistics"  to "Statistiques",
            "played"      to "Parties",
            "win_rate"    to "Victoires",
            "streak"      to "Série",
            "max_streak"  to "Record",
            "close"       to "FERMER"
        )
    } else {
        mapOf(
            "victory"     to "🎉 Congratulations! 🎉",
            "defeat"      to "😢 Too bad!",
            "correct_word" to "The word was",
            "attempts"    to "Time",
            "play_again"  to "Next word in",
            "statistics"  to "Statistics",
            "played"      to "Played",
            "win_rate"    to "Win Rate",
            "streak"      to "Streak",
            "max_streak"  to "Max Streak",
            "close"       to "CLOSE"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(
                    when {
                        isVerySmall -> 0.96f
                        isSmall     -> 0.92f
                        else        -> 0.88f
                    }
                )
                .wrapContentHeight()
                .defaultMinSize(minHeight = if (isVerySmall) 360.dp else if (isSmall) 400.dp else 460.dp)
                .padding(vertical = if (isVerySmall) 12.dp else 20.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = when {
                            isVerySmall -> 16.dp
                            isSmall     -> 20.dp
                            else        -> 24.dp
                        }
                    )
                    .padding(top = if (isVerySmall) 16.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    if (isVerySmall) 8.dp else if (isSmall) 12.dp else 16.dp
                )
            ) {
                // Titre victoire / défaite
                Text(
                    text = if (won) translations["victory"]!! else translations["defeat"]!!,
                    fontSize = when {
                        isVerySmall -> 19.sp
                        isSmall     -> 22.sp
                        else        -> 26.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (won) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(if (isVerySmall) 4.dp else 8.dp))

                // Afficher le temps écoulé (victoire ou défaite)
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
                                fontSize = if (isVerySmall) 16.sp else 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${translations["attempts"]}: ",
                                fontSize = if (isVerySmall) 14.sp else 16.sp,
                                color = Color(0xFF616161),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = timeElapsed,
                                fontSize = if (isVerySmall) 16.sp else 18.sp,
                                color = if (won) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (isVerySmall) 8.dp else 12.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                Text(
                    text = translations["statistics"]!!,
                    fontSize = if (isVerySmall) 18.sp else if (isSmall) 20.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )

                // Stats – 2 lignes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        title = translations["played"]!!,
                        value = stats.totalPlayed.toString(),
                        color = Color(0xFF1976D2),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall
                    )
                    StatBox(
                        title = translations["win_rate"]!!,
                        value = "${stats.winRate}%",
                        color = Color(0xFF388E3C),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall
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
                        isSmall = isSmall
                    )
                    StatBox(
                        title = translations["max_streak"]!!,
                        value = stats.maxStreak.toString(),
                        color = Color(0xFFF57C00),
                        isVerySmall = isVerySmall,
                        isSmall = isSmall
                    )
                }

                Spacer(modifier = Modifier.height(if (isVerySmall) 12.dp else 16.dp))

                // Compte à rebours
                CountdownTimer(
                    timeText = translations["play_again"]!!,
                    timeRemaining = timeUntilMidnight,
                    isVerySmall = isVerySmall,
                    isSmall = isSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .height(if (isVerySmall) 44.dp else if (isSmall) 48.dp else 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(
                        text = translations["close"]!!,
                        fontSize = if (isVerySmall) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(if (isVerySmall) 8.dp else 12.dp))
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
            .padding(vertical = if (isVerySmall) 12.dp else 16.dp)
    ) {
        Text(
            text = timeText,
            fontSize = if (isVerySmall) 13.sp else 15.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                fontSize = if (isVerySmall) 20.sp else 24.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = timeRemaining,
                fontSize = when {
                    isVerySmall -> 22.sp
                    isSmall     -> 26.sp
                    else        -> 30.sp
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
    isSmall: Boolean = false
) {
    val boxSize = when {
        isVerySmall -> 72.dp
        isSmall     -> 86.dp
        else        -> 100.dp
    }

    val valueFontSize = when {
        isVerySmall -> 22.sp
        isSmall     -> 28.sp
        else        -> 34.sp
    }

    val titleFontSize = when {
        isVerySmall -> 10.sp
        isSmall     -> 11.sp
        else        -> 13.sp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(16.dp))
                .border(2.dp, color, RoundedCornerShape(16.dp)),
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

// Fonction utilitaire pour calculer le temps jusqu'à minuit
private fun calculateTimeUntilMidnight(): String {
    val now = Calendar.getInstance()
    val midnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    // Si on est déjà passé minuit, on calcule pour le lendemain
    if (now.after(midnight)) {
        midnight.add(Calendar.DAY_OF_MONTH, 1)
    }

    val diff = midnight.timeInMillis - now.timeInMillis
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}