package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerProfileEntity
import com.example.engine.UnoGameViewModel
import com.example.model.GameMode
import com.example.model.HouseRules
import com.example.model.Player
import com.example.multiplayer.RoomInfo
import java.util.UUID

@Composable
fun MainScreen(
    viewModel: UnoGameViewModel,
    profile: PlayerProfileEntity?,
    activeHouseRules: HouseRules,
    onNavigateLobby: () -> Unit,
    onNavigateGame: () -> Unit,
    onNavigateHouseRules: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateRulebook: () -> Unit
) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }
    var showHostDialog by remember { mutableStateOf(false) }
    var hostRoomNameInput by remember { mutableStateOf("Challenger Arena") }
    var hostMaxPlayers by remember { mutableStateOf(4) }
    var showBotMatchDialog by remember { mutableStateOf(false) }
    var botPlayerCount by remember { mutableStateOf(4) }
    var showPassAndPlayDialog by remember { mutableStateOf(false) }
    var passAndPlayCount by remember { mutableStateOf(3) }

    val myPlayer = remember(profile) {
        Player(
            id = UUID.randomUUID().toString(),
            name = profile?.nickname ?: "Player 1",
            avatar = profile?.avatar ?: "🦊",
            isBot = false,
            isHost = true
        )
    }

    val publicRooms = remember { viewModel.multiplayerManager.getPublicRooms() }

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
            .testTag("main_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Chip
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                        modifier = Modifier
                            .clickable { onNavigateProfile() }
                            .testTag("profile_badge_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = profile?.avatar ?: "🦊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = profile?.nickname ?: "Player 1",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${profile?.gamesWon ?: 0} Wins • 🪙 ${profile?.coins ?: 500}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Settings & Rulebook
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onNavigateHouseRules,
                            modifier = Modifier.size(38.dp).testTag("rules_settings_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Rules Settings",
                                tint = Color(0xFFFFD700)
                            )
                        }
                        IconButton(
                            onClick = onNavigateRulebook,
                            modifier = Modifier.size(38.dp).testTag("rulebook_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Rulebook",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x55FFD700)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("hero_banner_card")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_uno_banner),
                            contentDescription = "UNO Arena Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC0F0C20))
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "UNO ARENA",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Custom House Rules • Online Lobbies • Smart AI",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Active House Rules Quick Pill
            item {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD700)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateHouseRules() }
                        .testTag("active_rules_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Active House Rules",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                val rulesText = buildList {
                                    if (activeHouseRules.stackingDraw) add("Stacking")
                                    if (activeHouseRules.sevenZeroRule) add("7-0 Swap")
                                    if (activeHouseRules.jumpIn) add("Jump-In")
                                    if (activeHouseRules.drawToMatch) add("Draw-To-Match")
                                }.joinToString(" • ").ifEmpty { "Official Standard" }
                                Text(
                                    text = rulesText,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "Customize >",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Online Multiplayer Actions
            item {
                Text(
                    text = "ONLINE MULTIPLAYER",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Match
                    PlayModeCard(
                        title = "Quick Match",
                        subtitle = "Instant Matchmaking",
                        icon = Icons.Default.PlayArrow,
                        accentColor = Color(0xFF22C55E),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.multiplayerManager.quickMatch(myPlayer)
                            onNavigateLobby()
                        }
                    )

                    // Host Room
                    PlayModeCard(
                        title = "Host Room",
                        subtitle = "Create with Code",
                        icon = Icons.Default.Add,
                        accentColor = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f),
                        onClick = { showHostDialog = true }
                    )
                }
            }

            item {
                // Join With Code Button
                OutlinedButton(
                    onClick = { showJoinDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("join_with_code_btn")
                ) {
                    Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Private Room with Code", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            }

            // Public Lobbies Browser
            item {
                Text(
                    text = "ACTIVE ROOMS",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(publicRooms) { room ->
                        PublicRoomCard(
                            room = room,
                            onJoin = {
                                viewModel.multiplayerManager.joinRoom(room.code, myPlayer, room.rules)
                                onNavigateLobby()
                            }
                        )
                    }
                }
            }

            // Offline & Local Modes
            item {
                Text(
                    text = "OFFLINE & LOCAL PLAY",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Practice vs Bots
                    PlayModeCard(
                        title = "Solo vs AI Bots",
                        subtitle = "Smart AI Practice",
                        icon = Icons.Default.SmartToy,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = { showBotMatchDialog = true }
                    )

                    // Pass & Play
                    PlayModeCard(
                        title = "Pass & Play",
                        subtitle = "Local Hand Conceal",
                        icon = Icons.Default.Group,
                        accentColor = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f),
                        onClick = { showPassAndPlayDialog = true }
                    )
                }
            }
        }

        // Join Room Dialog
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Join Room", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter the 6-character room code:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { joinCodeInput = it.uppercase() },
                            placeholder = { Text("e.g. UNO-4821") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("join_code_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (joinCodeInput.isNotBlank()) {
                                viewModel.multiplayerManager.joinRoom(joinCodeInput, myPlayer, activeHouseRules)
                                showJoinDialog = false
                                onNavigateLobby()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.testTag("confirm_join_btn")
                    ) {
                        Text("Join Room")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Host Room Dialog
        if (showHostDialog) {
            AlertDialog(
                onDismissRequest = { showHostDialog = false },
                title = { Text("Host Custom Room", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Room Name:")
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = hostRoomNameInput,
                            onValueChange = { hostRoomNameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("host_room_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Max Players:")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 3, 4).forEach { count ->
                                val selected = hostMaxPlayers == count
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFFFD700) else Color(0xFF334155),
                                    modifier = Modifier
                                        .clickable { hostMaxPlayers = count }
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "$count Players",
                                        color = if (selected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.multiplayerManager.hostRoom(
                                hostPlayer = myPlayer,
                                roomName = hostRoomNameInput,
                                rules = activeHouseRules,
                                maxPlayers = hostMaxPlayers
                            )
                            showHostDialog = false
                            onNavigateLobby()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.testTag("create_room_btn")
                    ) {
                        Text("Create & Host")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHostDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bot Match Dialog
        if (showBotMatchDialog) {
            AlertDialog(
                onDismissRequest = { showBotMatchDialog = false },
                title = { Text("Play vs AI Bots", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Select table size:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 3, 4).forEach { count ->
                                val selected = botPlayerCount == count
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFF38BDF8) else Color(0xFF334155),
                                    modifier = Modifier.clickable { botPlayerCount = count }
                                ) {
                                    Text(
                                        text = "$count Players",
                                        color = if (selected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AI will follow your active house rules (${if (activeHouseRules.stackingDraw) "Stacking, 7-0" else "Official"}).",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bots = (1 until botPlayerCount).map { i ->
                                Player(
                                    id = "bot_$i",
                                    name = "Bot ${listOf("Ace", "Luna", "Blaze")[i - 1]}",
                                    avatar = listOf("🤖", "🐼", "🦁")[i - 1],
                                    isBot = true
                                )
                            }
                            val allPlayers = listOf(myPlayer) + bots
                            viewModel.startNewGame(
                                mode = GameMode.BOT_MATCH,
                                playersList = allPlayers,
                                rules = activeHouseRules
                            )
                            showBotMatchDialog = false
                            onNavigateGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        modifier = Modifier.testTag("start_bot_game_btn")
                    ) {
                        Text("Start Game")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBotMatchDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Pass and play dialog
        if (showPassAndPlayDialog) {
            AlertDialog(
                onDismissRequest = { showPassAndPlayDialog = false },
                title = { Text("Pass & Play (Local)", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Number of local players on this device:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 3, 4).forEach { count ->
                                val selected = passAndPlayCount == count
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFEC4899) else Color(0xFF334155),
                                    modifier = Modifier.clickable { passAndPlayCount = count }
                                ) {
                                    Text(
                                        text = "$count Players",
                                        color = if (selected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hands are concealed between turns until the device is handed over and tapped to reveal.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val players = (1..passAndPlayCount).map { i ->
                                Player(
                                    id = "local_player_$i",
                                    name = "Player $i",
                                    avatar = listOf("🦊", "🦁", "🐼", "🐯")[i - 1],
                                    isBot = false
                                )
                            }
                            viewModel.startNewGame(
                                mode = GameMode.PASS_AND_PLAY,
                                playersList = players,
                                rules = activeHouseRules
                            )
                            showPassAndPlayDialog = false
                            onNavigateGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        modifier = Modifier.testTag("start_pass_play_btn")
                    ) {
                        Text("Start Match")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPassAndPlayDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun PlayModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier
            .height(98.dp)
            .clickable { onClick() }
            .testTag("play_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun PublicRoomCard(
    room: RoomInfo,
    onJoin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier
            .width(180.dp)
            .clickable { onJoin() }
            .testTag("public_room_${room.code}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0x33FFD700),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = room.code,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Text(
                    text = "${room.currentPlayers}/${room.maxPlayers} 👥",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = room.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )

            Text(
                text = "Host: ${room.hostPlayerName} • ${room.pingMs}ms",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(32.dp)
            ) {
                Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
