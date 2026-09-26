package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.UnoDeck
import com.example.engine.UnoGameState
import com.example.engine.UnoGameViewModel
import com.example.model.CardColor
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.UnoCard
import com.example.ui.components.ChallengeWildFourDialog
import com.example.ui.components.DirectionIndicatorView
import com.example.ui.components.OpponentAvatarView
import com.example.ui.components.QuickEmojiPickerRow
import com.example.ui.components.QuickReactionOverlay
import com.example.ui.components.RoundWinDialog
import com.example.ui.components.SevenSwapDialog
import com.example.ui.components.StackingBannerView
import com.example.ui.components.TurnTimerBar
import com.example.ui.components.UnoCardView
import com.example.ui.components.WildColorPickerDialog

@Composable
fun GameScreen(
    viewModel: UnoGameViewModel,
    gameState: UnoGameState,
    onNavigateBack: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showActionLogsDialog by remember { mutableStateOf(false) }
    var isSoundMuted by remember { mutableStateOf(false) }

    val currentPlayer = gameState.currentPlayer
    val activeDiscard = gameState.topDiscardCard
    val humanPlayer = gameState.humanPlayer ?: gameState.players.firstOrNull()

    // Determine if it is the local player's turn
    val isMyTurn = currentPlayer != null && !currentPlayer.isBot &&
            (gameState.mode != GameMode.PASS_AND_PLAY || currentPlayer.id == humanPlayer?.id || !currentPlayer.isConcealed)

    BackHandler {
        showExitDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF0F0C20),
                        Color(0xFF05030A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("game_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header: Controls & Turn Timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Leave Match",
                        tint = Color.White
                    )
                }

                // Room / Mode Info Pill
                Surface(
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (gameState.roomCode.isNotBlank()) gameState.roomCode else gameState.mode.title,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        if (gameState.houseRules.stackingDraw) {
                            Text(
                                text = " • Stacking",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showActionLogsDialog = true },
                        modifier = Modifier.size(36.dp).testTag("action_logs_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Match Logs",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(
                        onClick = {
                            isSoundMuted = !isSoundMuted
                            viewModel.soundManager.setSoundEnabled(!isSoundMuted)
                        },
                        modifier = Modifier.size(36.dp).testTag("mute_btn")
                    ) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Sound Toggle",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Turn Timer Bar
            if (gameState.houseRules.turnTimeLimitSeconds > 0) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TurnTimerBar(
                        remainingSeconds = gameState.turnSecondsRemaining,
                        totalSeconds = gameState.houseRules.turnTimeLimitSeconds
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Opponents Roster Row
            val opponents = gameState.players.filter { it.id != (humanPlayer?.id ?: "") }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(opponents) { opponent ->
                    OpponentAvatarView(
                        player = opponent,
                        isCurrentTurn = gameState.currentPlayer?.id == opponent.id,
                        onCatchUno = {
                            humanPlayer?.let { hp -> viewModel.catchUnoPenalty(hp.id, opponent.id) }
                        },
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Center Table Arena (Discard Pile + Draw Pile + Direction indicator + Stacking Banner)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Stacking Banner
                    if (gameState.accumulatedDrawStack > 0) {
                        StackingBannerView(
                            accumulatedDrawCount = gameState.accumulatedDrawStack,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Card Table Area
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Draw Pile
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = isMyTurn) {
                                    humanPlayer?.let { viewModel.drawCard(it.id) }
                                }
                                .testTag("draw_pile")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Stack depth illusion
                                UnoCardView(
                                    card = null,
                                    isFaceUp = false,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                                UnoCardView(
                                    card = null,
                                    isFaceUp = false
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0x66000000),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (gameState.accumulatedDrawStack > 0) "Draw +${gameState.accumulatedDrawStack}" else "Draw (${gameState.drawPile.size})",
                                    color = if (isMyTurn) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        // Direction Indicator
                        DirectionIndicatorView(
                            direction = gameState.direction
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        // Discard Pile with Active Color Glow
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .border(
                                        width = 3.dp,
                                        color = gameState.activeColor.composeColor,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                UnoCardView(
                                    card = activeDiscard,
                                    isFaceUp = true,
                                    modifier = Modifier.testTag("discard_pile_top")
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = gameState.activeColor.composeColor,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Active: ${gameState.activeColor.displayName}",
                                    color = if (gameState.activeColor == CardColor.YELLOW) Color.Black else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Latest Action Log Banner
                    gameState.logs.lastOrNull()?.let { lastLog ->
                        Surface(
                            color = Color(0x99000000),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                                .testTag("latest_action_banner")
                        ) {
                            Text(
                                text = lastLog.message,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Quick Reaction Floating Animation
                QuickReactionOverlay(
                    reaction = gameState.activeReaction,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )
            }

            // Bottom Player Hand Area
            val playerToShow = if (gameState.mode == GameMode.PASS_AND_PLAY) currentPlayer else humanPlayer

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x55000000))
                    .padding(vertical = 8.dp)
            ) {
                // Hand header: Player name, cards count, and action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = playerToShow?.avatar ?: "🦊", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${playerToShow?.name ?: "You"} (${playerToShow?.cardCount ?: 0})",
                            color = if (isMyTurn) Color(0xFFFFD700) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isMyTurn) {
                            Surface(
                                color = Color(0xFF22C55E),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "YOUR TURN",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Action Controls: UNO Call & Emoji
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showEmojiPicker = !showEmojiPicker },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .testTag("emoji_toggle_btn")
                        ) {
                            Text(text = "💬", fontSize = 16.sp)
                        }

                        // Big UNO Button
                        UnoCallButton(
                            isEligible = (playerToShow?.cardCount ?: 0) <= 2,
                            hasCalled = playerToShow?.hasCalledUno == true,
                            onClick = {
                                playerToShow?.let { viewModel.callUno(it.id) }
                            }
                        )
                    }
                }

                // Emoji Picker Row if toggled
                if (showEmojiPicker) {
                    QuickEmojiPickerRow(
                        onEmojiSelected = { emoji ->
                            playerToShow?.let { viewModel.sendEmojiReaction(it.id, emoji) }
                            showEmojiPicker = false
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 4.dp)
                    )
                }

                // Pass and play concealment
                if (playerToShow?.isConcealed == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { viewModel.revealPassAndPlayHand() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("reveal_hand_btn")
                        ) {
                            Text("Pass Device to ${playerToShow.name} & Tap to Reveal", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Cards LazyRow
                    val hand = playerToShow?.hand ?: emptyList()
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp)
                            .testTag("player_hand_row"),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy((-16).dp), // Slight overlap for natural fan feel
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(hand, key = { it.id }) { card ->
                            val isPlayable = isMyTurn && activeDiscard != null &&
                                    UnoDeck.isCardPlayable(
                                        card = card,
                                        activeCard = activeDiscard,
                                        activeColor = gameState.activeColor,
                                        accumulatedDrawStack = gameState.accumulatedDrawStack,
                                        rules = gameState.houseRules
                                    )

                            val canJump = !isMyTurn && gameState.houseRules.jumpIn && activeDiscard != null &&
                                    UnoDeck.canJumpIn(card, activeDiscard, gameState.houseRules)

                            UnoCardView(
                                card = card,
                                isFaceUp = true,
                                isPlayable = isPlayable || canJump,
                                isSelected = isPlayable || canJump,
                                onClick = {
                                    if (isPlayable) {
                                        playerToShow?.let { p ->
                                            viewModel.playCard(p.id, card)
                                        }
                                    } else if (canJump) {
                                        playerToShow?.let { p ->
                                            viewModel.triggerJumpIn(p.id, card)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Wild Color Picker Dialog
        if (gameState.pendingWildColorPlayerId != null && gameState.pendingWildColorPlayerId == humanPlayer?.id) {
            WildColorPickerDialog(
                onColorSelected = { selectedColor ->
                    viewModel.selectWildColor(selectedColor)
                }
            )
        }

        // 7-Swap Hand Dialog
        if (gameState.pendingSevenSwapPlayerId != null && gameState.pendingSevenSwapPlayerId == humanPlayer?.id) {
            SevenSwapDialog(
                opponents = gameState.players.filter { it.id != humanPlayer.id },
                onOpponentSelected = { targetOpponentId ->
                    viewModel.resolveSevenSwap(targetOpponentId)
                }
            )
        }

        // Challenge Wild Draw 4 Dialog
        if (gameState.pendingWildDrawFourChallengerId != null && gameState.pendingWildDrawFourChallengerId == humanPlayer?.id) {
            val wildPlayerName = gameState.players.find { it.id == gameState.wildPlayedByPlayerId }?.name ?: "Opponent"
            ChallengeWildFourDialog(
                wildPlayerName = wildPlayerName,
                onAccept = {
                    viewModel.resolveWildDrawFourChallenge(humanPlayer.id, doChallenge = false)
                },
                onChallenge = {
                    viewModel.resolveWildDrawFourChallenge(humanPlayer.id, doChallenge = true)
                }
            )
        }

        // Round Win / Game Over Dialog
        if (gameState.status == GameStatus.ROUND_OVER || gameState.status == GameStatus.GAME_OVER) {
            gameState.roundWinner?.let { winner ->
                RoundWinDialog(
                    winner = winner,
                    status = gameState.status,
                    targetScore = gameState.houseRules.targetScore,
                    players = gameState.players,
                    onNextRound = { viewModel.startNextRound() },
                    onExitGame = onNavigateBack
                )
            }
        }

        // Exit Match Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Leave Match?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to exit to the main menu? Current match progress will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("confirm_exit_btn")
                    ) {
                        Text("Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Stay")
                    }
                }
            )
        }

        // Match Logs Modal
        if (showActionLogsDialog) {
            AlertDialog(
                onDismissRequest = { showActionLogsDialog = false },
                title = { Text("Match Event Log", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        gameState.logs.reversed().forEach { log ->
                            Text(
                                text = "• ${log.message}",
                                fontSize = 12.sp,
                                color = when (log.tag) {
                                    "UNO" -> Color(0xFFEF4444)
                                    "ACTION" -> Color(0xFF38BDF8)
                                    "SWAP" -> Color(0xFFFFD700)
                                    "WIN" -> Color(0xFF22C55E)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showActionLogsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun UnoCallButton(
    isEligible: Boolean,
    hasCalled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "uno_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "uno_pulse_scale"
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (hasCalled) Color(0xFF16A34A) else Color(0xFFDC2626)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .then(
                if (isEligible && !hasCalled) {
                    Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                } else Modifier
            )
            .testTag("call_uno_btn")
    ) {
        Text(
            text = if (hasCalled) "✓ UNO CALLED" else "UNO!",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}
