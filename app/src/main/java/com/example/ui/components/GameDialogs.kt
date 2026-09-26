package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CardColor
import com.example.model.GameStatus
import com.example.model.Player

@Composable
fun WildColorPickerDialog(
    onColorSelected: (CardColor) -> Unit
) {
    Dialog(onDismissRequest = { /* Must select a color */ }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x55FFFFFF)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag("wild_color_picker_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CHOOSE COLOR",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Pick the next active color for the table",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // 2x2 color picker grid
                Column(
                    modifier = Modifier.size(180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorChoiceButton(
                            color = CardColor.RED,
                            modifier = Modifier.weight(1f),
                            onClick = { onColorSelected(CardColor.RED) }
                        )
                        ColorChoiceButton(
                            color = CardColor.BLUE,
                            modifier = Modifier.weight(1f),
                            onClick = { onColorSelected(CardColor.BLUE) }
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorChoiceButton(
                            color = CardColor.YELLOW,
                            modifier = Modifier.weight(1f),
                            onClick = { onColorSelected(CardColor.YELLOW) }
                        )
                        ColorChoiceButton(
                            color = CardColor.GREEN,
                            modifier = Modifier.weight(1f),
                            onClick = { onColorSelected(CardColor.GREEN) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorChoiceButton(
    color: CardColor,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color.composeColor)
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("pick_color_${color.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = color.displayName.uppercase(),
            color = if (color == CardColor.YELLOW) Color(0xFF451A03) else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SevenSwapDialog(
    opponents: List<Player>,
    onOpponentSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = { /* Must select */ }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("seven_swap_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔄 7-RULE: SWAP HAND",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Select an opponent to trade your entire hand with!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opponents.forEach { opponent ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpponentSelected(opponent.id) }
                                .testTag("swap_target_${opponent.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = opponent.avatar, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = opponent.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${opponent.cardCount} cards in hand",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = { onOpponentSelected(opponent.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Swap", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeWildFourDialog(
    wildPlayerName: String,
    onAccept: () -> Unit,
    onChallenge: () -> Unit
) {
    Dialog(onDismissRequest = { /* Must choose */ }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("challenge_wild_four_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "WILD DRAW +4 CHALLENGE",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$wildPlayerName played a Wild Draw 4 on you!\nDo you suspect they illegally bluffed while holding the matching color?",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "• If guilty: They draw 4 cards instead!",
                            color = Color(0xFF4ADE80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• If innocent: You draw 6 cards (+2 penalty)!",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accept_draw_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Accept +4", color = Color.White)
                    }
                    Button(
                        onClick = onChallenge,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("challenge_bluff_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Challenge!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RoundWinDialog(
    winner: Player,
    status: GameStatus,
    targetScore: Int,
    players: List<Player>,
    onNextRound: () -> Unit,
    onExitGame: () -> Unit
) {
    val isGameOver = status == GameStatus.GAME_OVER

    Dialog(onDismissRequest = { /* Must tap a button */ }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("round_win_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isGameOver) "🏆 MATCH CHAMPION! 🏆" else "ROUND WINNER!",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "${winner.name} won with 0 cards left!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Scoreboard list
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Scoreboard (Goal: ${if (targetScore > 0) "$targetScore pts" else "Single Round"})",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        players.sortedByDescending { it.score }.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = p.avatar, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = p.name,
                                        color = if (p.id == winner.id) Color(0xFFFFD700) else Color.White,
                                        fontWeight = if (p.id == winner.id) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "${p.score} pts",
                                    color = if (p.id == winner.id) Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExitGame,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exit_game_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exit", color = Color.White)
                    }
                    Button(
                        onClick = onNextRound,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("next_round_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "Play Again" else "Next Round",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickEmojiPickerRow(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("🔥", "😂", "😱", "🃏", "🛑", "👑", "🥳", "💀")

    Surface(
        color = Color(0xDD0F172A),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = modifier.testTag("emoji_picker_bar")
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onEmojiSelected(emoji) }
                        .testTag("emoji_$emoji"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}
