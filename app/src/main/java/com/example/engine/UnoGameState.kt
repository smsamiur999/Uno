package com.example.engine

import com.example.model.CardColor
import com.example.model.GameActionLog
import com.example.model.GameDirection
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.HouseRules
import com.example.model.Player
import com.example.model.QuickReaction
import com.example.model.UnoCard
import java.util.UUID

data class UnoGameState(
    val gameId: String = UUID.randomUUID().toString(),
    val mode: GameMode = GameMode.ONLINE_ROOM,
    val roomCode: String = "",
    val players: List<Player> = emptyList(),
    val drawPile: List<UnoCard> = emptyList(),
    val discardPile: List<UnoCard> = emptyList(),
    val activeColor: CardColor = CardColor.RED,
    val currentPlayerIndex: Int = 0,
    val direction: GameDirection = GameDirection.CLOCKWISE,
    val accumulatedDrawStack: Int = 0,
    val status: GameStatus = GameStatus.LOBBY,
    val houseRules: HouseRules = HouseRules.SPICY_CHAOS,
    val roundWinner: Player? = null,
    val overallWinner: Player? = null,
    val logs: List<GameActionLog> = emptyList(),
    val activeReaction: QuickReaction? = null,

    // Interactivity dialog states:
    val pendingWildColorPlayerId: String? = null,
    val pendingWildDrawFourChallengerId: String? = null,
    val pendingSevenSwapPlayerId: String? = null,
    val previousColorBeforeWild: CardColor? = null, // for challenge resolution
    val wildPlayedByPlayerId: String? = null,

    // Turn timer
    val turnSecondsRemaining: Int = 15,
    val isTurnTimerActive: Boolean = true
) {
    val topDiscardCard: UnoCard? get() = discardPile.lastOrNull()
    val currentPlayer: Player? get() = players.getOrNull(currentPlayerIndex)
    val humanPlayer: Player? get() = players.firstOrNull { !it.isBot }
}
