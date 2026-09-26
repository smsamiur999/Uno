package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class CardColor(val displayName: String, val composeColor: Color, val darkColor: Color) {
    RED("Red", Color(0xFFE52521), Color(0xFF99120F)),
    YELLOW("Yellow", Color(0xFFFFCC00), Color(0xFFB38F00)),
    GREEN("Green", Color(0xFF2DB84D), Color(0xFF1B6B2E)),
    BLUE("Blue", Color(0xFF007BC7), Color(0xFF004977)),
    WILD("Wild", Color(0xFF1E1B4B), Color(0xFF0F0C20));

    val isColored: Boolean get() = this != WILD
}

enum class CardType(val symbol: String, val baseScore: Int) {
    NUMBER("", 0), // Score is the number itself
    SKIP("⊘", 20),
    REVERSE("⇄", 20),
    DRAW_TWO("+2", 20),
    WILD("★", 50),
    WILD_DRAW_FOUR("+4", 50);

    val isAction: Boolean get() = this == SKIP || this == REVERSE || this == DRAW_TWO
    val isWild: Boolean get() = this == WILD || this == WILD_DRAW_FOUR
}

data class UnoCard(
    val id: String = UUID.randomUUID().toString(),
    val color: CardColor,
    val type: CardType,
    val number: Int? = null // 0..9 for NUMBER cards
) {
    val displayValue: String
        get() = when (type) {
            CardType.NUMBER -> number?.toString() ?: ""
            CardType.SKIP -> "SKIP"
            CardType.REVERSE -> "REV"
            CardType.DRAW_TWO -> "+2"
            CardType.WILD -> "WILD"
            CardType.WILD_DRAW_FOUR -> "+4"
        }

    val scoreValue: Int
        get() = when (type) {
            CardType.NUMBER -> number ?: 0
            else -> type.baseScore
        }

    fun isIdenticalTo(other: UnoCard): Boolean {
        return color == other.color && type == other.type && number == other.number
    }
}

enum class GameDirection {
    CLOCKWISE,
    COUNTER_CLOCKWISE;

    fun toggle(): GameDirection = when (this) {
        CLOCKWISE -> COUNTER_CLOCKWISE
        COUNTER_CLOCKWISE -> CLOCKWISE
    }
}

enum class GameStatus {
    LOBBY,
    PLAYING,
    ROUND_OVER,
    GAME_OVER
}

enum class GameMode(val title: String, val description: String) {
    ONLINE_ROOM("Online Multiplayer", "Host or join custom rooms with friends & global players"),
    BOT_MATCH("Solo vs Bots", "Practice with intelligent AI bots with custom house rules"),
    PASS_AND_PLAY("Pass & Play", "Local multiplayer on a single device with concealed hands")
}

data class HouseRules(
    val stackingDraw: Boolean = true,      // +2 on +2, +4 on +4, or +4 on +2! Cumulative draw penalties
    val sevenZeroRule: Boolean = true,     // Play 7 to swap hand with chosen player; Play 0 to shift hands in turn direction
    val jumpIn: Boolean = true,            // Play identical card out of turn anytime
    val drawToMatch: Boolean = false,      // Keep drawing until a playable card is found (vs draw 1)
    val forcePlay: Boolean = true,         // Must play drawn card immediately if it can be played
    val bluffChallenge: Boolean = true,    // Can challenge Wild Draw 4 for illegal bluff
    val targetScore: Int = 250,            // 0 = Single Round knockout; >0 = First player to reach points
    val turnTimeLimitSeconds: Int = 15     // 0 = Unlimited turn timer
) {
    companion object {
        val OFFICIAL_RULES = HouseRules(
            stackingDraw = false,
            sevenZeroRule = false,
            jumpIn = false,
            drawToMatch = false,
            forcePlay = false,
            bluffChallenge = true,
            targetScore = 500,
            turnTimeLimitSeconds = 30
        )

        val SPICY_CHAOS = HouseRules(
            stackingDraw = true,
            sevenZeroRule = true,
            jumpIn = true,
            drawToMatch = true,
            forcePlay = true,
            bluffChallenge = true,
            targetScore = 250,
            turnTimeLimitSeconds = 15
        )

        val TOURNAMENT_STACKING = HouseRules(
            stackingDraw = true,
            sevenZeroRule = false,
            jumpIn = true,
            drawToMatch = false,
            forcePlay = true,
            bluffChallenge = true,
            targetScore = 300,
            turnTimeLimitSeconds = 15
        )
    }
}

data class Player(
    val id: String,
    val name: String,
    val avatar: String = "🦊",
    val isBot: Boolean = false,
    val isHost: Boolean = false,
    val isReady: Boolean = true,
    val hand: List<UnoCard> = emptyList(),
    val hasCalledUno: Boolean = false,
    val score: Int = 0,
    val isConcealed: Boolean = false // Used for pass & play to hide hands until revealed
) {
    val cardCount: Int get() = hand.size
    val isVulnerableToUnoCall: Boolean get() = hand.size == 1 && !hasCalledUno
}

data class GameActionLog(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val tag: String = "INFO", // "UNO", "STACK", "SWAP", "WIN", "INFO"
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickReaction(
    val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val playerName: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)
