package com.example.multiplayer

import com.example.model.HouseRules
import com.example.model.Player
import com.example.model.QuickReaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class RoomInfo(
    val code: String,
    val name: String,
    val hostPlayerName: String,
    val currentPlayers: Int,
    val maxPlayers: Int = 4,
    val pingMs: Int = Random.nextInt(28, 65),
    val rules: HouseRules = HouseRules()
)

data class LobbyState(
    val roomCode: String = "",
    val roomName: String = "",
    val isHost: Boolean = false,
    val maxPlayers: Int = 4,
    val players: List<Player> = emptyList(),
    val houseRules: HouseRules = HouseRules.SPICY_CHAOS,
    val isGameStarting: Boolean = false,
    val statusMessage: String = "Waiting for players to get ready..."
)

class MultiplayerManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _lobbyState = MutableStateFlow<LobbyState?>(null)
    val lobbyState: StateFlow<LobbyState?> = _lobbyState.asStateFlow()

    private val _incomingReactions = MutableSharedFlow<QuickReaction>(extraBufferCapacity = 10)
    val incomingReactions: SharedFlow<QuickReaction> = _incomingReactions.asSharedFlow()

    private val sampleAvatars = listOf("🦊", "🦁", "🐼", "🐯", "🤖", "🧙‍♂️", "🐱", "🐶", "🚀", "👑")
    private val sampleOpponents = listOf(
        "LunaStar", "PixelBlaze", "RetroGamer", "AceSpade", "CardNinja",
        "Vortex99", "SkyWalker", "WildMaster", "EchoStrike", "NeonKnight"
    )

    fun getPublicRooms(): List<RoomInfo> {
        return listOf(
            RoomInfo(
                code = "UNO-4821",
                name = "Spicy Stacking Arena 🔥",
                hostPlayerName = "CardNinja",
                currentPlayers = 3,
                maxPlayers = 4,
                rules = HouseRules.SPICY_CHAOS
            ),
            RoomInfo(
                code = "UNO-9102",
                name = "Tournament Stacking 🏆",
                hostPlayerName = "AceSpade",
                currentPlayers = 2,
                maxPlayers = 4,
                rules = HouseRules.TOURNAMENT_STACKING
            ),
            RoomInfo(
                code = "UNO-3384",
                name = "Classic Casuals ☕",
                hostPlayerName = "LunaStar",
                currentPlayers = 2,
                maxPlayers = 4,
                rules = HouseRules.OFFICIAL_RULES
            ),
            RoomInfo(
                code = "UNO-7751",
                name = "7-0 & Jump-In Frenzy ⚡",
                hostPlayerName = "NeonKnight",
                currentPlayers = 3,
                maxPlayers = 4,
                rules = HouseRules(stackingDraw = true, sevenZeroRule = true, jumpIn = true, targetScore = 250)
            )
        )
    }

    fun hostRoom(hostPlayer: Player, roomName: String, rules: HouseRules, maxPlayers: Int = 4): String {
        val code = generateRoomCode()
        val currentLobby = LobbyState(
            roomCode = code,
            roomName = roomName.ifBlank { "Arena #$code" },
            isHost = true,
            maxPlayers = maxPlayers,
            players = listOf(hostPlayer.copy(isHost = true, isReady = true)),
            houseRules = rules,
            statusMessage = "Room created! Share code $code with friends."
        )
        _lobbyState.value = currentLobby

        // Auto invite or allow simulated online players to join after a few moments
        scope.launch {
            delay(1200)
            addRandomOnlinePlayer()
            delay(1800)
            if ((_lobbyState.value?.players?.size ?: 0) < maxPlayers) {
                addRandomOnlinePlayer()
            }
        }

        return code
    }

    fun joinRoom(code: String, player: Player, rulesFallback: HouseRules = HouseRules.SPICY_CHAOS): Boolean {
        val formattedCode = code.trim().uppercase()
        val existingPublic = getPublicRooms().find { it.code.equals(formattedCode, ignoreCase = true) }
        val rules = existingPublic?.rules ?: rulesFallback
        val name = existingPublic?.name ?: "Online Match ($formattedCode)"

        val initialPlayers = mutableListOf<Player>()
        val hostName = existingPublic?.hostPlayerName ?: sampleOpponents.random()
        initialPlayers.add(
            Player(
                id = UUID.randomUUID().toString(),
                name = hostName,
                avatar = sampleAvatars.random(),
                isBot = false,
                isHost = true,
                isReady = true
            )
        )
        initialPlayers.add(player.copy(isHost = false, isReady = true))

        // Add 1 more opponent
        initialPlayers.add(
            Player(
                id = UUID.randomUUID().toString(),
                name = sampleOpponents.filter { it != hostName }.random(),
                avatar = sampleAvatars.random(),
                isBot = false,
                isHost = false,
                isReady = true
            )
        )

        _lobbyState.value = LobbyState(
            roomCode = formattedCode,
            roomName = name,
            isHost = false,
            maxPlayers = 4,
            players = initialPlayers,
            houseRules = rules,
            statusMessage = "Joined room $formattedCode! Host will start shortly."
        )
        return true
    }

    fun quickMatch(player: Player): String {
        val randomRoom = getPublicRooms().random()
        joinRoom(randomRoom.code, player, randomRoom.rules)
        return randomRoom.code
    }

    fun addBotPlayer() {
        val current = _lobbyState.value ?: return
        if (current.players.size >= current.maxPlayers) return

        val botIndex = current.players.count { it.isBot } + 1
        val newBot = Player(
            id = "bot_${UUID.randomUUID()}",
            name = "Bot ${listOf("Alpha", "Bravo", "Charlie", "Delta").getOrElse(botIndex - 1) { "Bot$botIndex" }}",
            avatar = "🤖",
            isBot = true,
            isHost = false,
            isReady = true
        )
        _lobbyState.value = current.copy(
            players = current.players + newBot,
            statusMessage = "${newBot.name} added to room."
        )
    }

    fun removePlayer(playerId: String) {
        val current = _lobbyState.value ?: return
        _lobbyState.value = current.copy(
            players = current.players.filter { it.id != playerId }
        )
    }

    fun updateRules(newRules: HouseRules) {
        val current = _lobbyState.value ?: return
        if (!current.isHost) return
        _lobbyState.value = current.copy(houseRules = newRules)
    }

    fun togglePlayerReady(playerId: String) {
        val current = _lobbyState.value ?: return
        _lobbyState.value = current.copy(
            players = current.players.map {
                if (it.id == playerId) it.copy(isReady = !it.isReady) else it
            }
        )
    }

    fun sendReaction(playerId: String, playerName: String, emoji: String) {
        val reaction = QuickReaction(playerId = playerId, playerName = playerName, emoji = emoji)
        _incomingReactions.tryEmit(reaction)
    }

    fun leaveLobby() {
        _lobbyState.value = null
    }

    private fun addRandomOnlinePlayer() {
        val current = _lobbyState.value ?: return
        if (current.players.size >= current.maxPlayers) return

        val availableNames = sampleOpponents.filter { name ->
            current.players.none { it.name == name }
        }
        val name = availableNames.randomOrNull() ?: "Player${Random.nextInt(100, 999)}"
        val avatar = sampleAvatars.random()

        val joinedPlayer = Player(
            id = UUID.randomUUID().toString(),
            name = name,
            avatar = avatar,
            isBot = false,
            isHost = false,
            isReady = true
        )

        _lobbyState.value = current.copy(
            players = current.players + joinedPlayer,
            statusMessage = "$name joined the room!"
        )
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..4).map { chars.random() }.joinToString("")
        return "UNO-$code"
    }
}
