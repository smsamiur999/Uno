package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.UnoGameViewModel
import com.example.model.GameMode
import com.example.model.HouseRules
import com.example.model.Player
import com.example.multiplayer.LobbyState

@Composable
fun LobbyScreen(
    viewModel: UnoGameViewModel,
    lobbyState: LobbyState?,
    onStartGame: () -> Unit,
    onNavigateHouseRules: () -> Unit,
    onLeaveLobby: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        viewModel.multiplayerManager.leaveLobby()
        onLeaveLobby()
    }

    if (lobbyState == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F0C20)),
            contentAlignment = Alignment.Center
        ) {
            Text("No active lobby.", color = Color.White)
        }
        return
    }

    val isHost = lobbyState.isHost
    val canStart = lobbyState.players.size >= 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1B4B), Color(0xFF0F0C20), Color(0xFF030206))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("lobby_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        viewModel.multiplayerManager.leaveLobby()
                        onLeaveLobby()
                    },
                    modifier = Modifier.testTag("leave_lobby_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Leave",
                        tint = Color.White
                    )
                }

                Text(
                    text = lobbyState.roomName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = onNavigateHouseRules,
                    modifier = Modifier.testTag("lobby_rules_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Rules Settings",
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room Code Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_code_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ROOM CODE",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = lobbyState.roomCode,
                            color = Color(0xFFFFD700),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("UNO Room Code", lobbyState.roomCode))
                            Toast.makeText(context, "Room Code Copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("copy_room_code_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // House Rules Quick Overview Bar
            Surface(
                color = Color(0x33FFFFFF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    RuleTag(name = "Stacking", enabled = lobbyState.houseRules.stackingDraw)
                    RuleTag(name = "7-0 Rule", enabled = lobbyState.houseRules.sevenZeroRule)
                    RuleTag(name = "Jump-In", enabled = lobbyState.houseRules.jumpIn)
                    RuleTag(name = "Timer", enabled = lobbyState.houseRules.turnTimeLimitSeconds > 0)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Players Roster Title & Add Bot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYERS (${lobbyState.players.size}/${lobbyState.maxPlayers})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (isHost && lobbyState.players.size < lobbyState.maxPlayers) {
                    OutlinedButton(
                        onClick = { viewModel.multiplayerManager.addBotPlayer() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_bot_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Bot", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Players List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lobbyState.players) { player ->
                    PlayerLobbyItem(
                        player = player,
                        canKick = isHost && !player.isHost,
                        onKick = { viewModel.multiplayerManager.removePlayer(player.id) },
                        onToggleReady = { viewModel.multiplayerManager.togglePlayerReady(player.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Start Match / Ready Check Button
            if (isHost) {
                Button(
                    onClick = {
                        viewModel.startNewGame(
                            mode = GameMode.ONLINE_ROOM,
                            playersList = lobbyState.players,
                            rules = lobbyState.houseRules,
                            roomCode = lobbyState.roomCode
                        )
                        onStartGame()
                    },
                    enabled = canStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E),
                        disabledContainerColor = Color(0x3322C55E)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_match_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (canStart) "START MATCH" else "WAITING FOR PLAYERS (MIN 2)",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            } else {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "⏳ Waiting for host to start the game...",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(14.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerLobbyItem(
    player: Player,
    canKick: Boolean,
    onKick: () -> Unit,
    onToggleReady: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.avatar, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (player.isHost) {
                        Surface(
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "HOST",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (player.isBot) {
                        Surface(
                            color = Color(0xFF38BDF8),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "BOT",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (player.isReady) "Ready" else "Preparing...",
                    color = if (player.isReady) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            if (canKick) {
                IconButton(
                    onClick = onKick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Player",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleTag(name: String, enabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFF22C55E) else Color(0xFF64748B))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            color = if (enabled) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
