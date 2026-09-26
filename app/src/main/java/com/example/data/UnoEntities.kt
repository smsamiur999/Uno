package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val nickname: String = "Player 1",
    val avatar: String = "🦊",
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val unoCallsMade: Int = 0,
    val unoPenaltiesCaught: Int = 0,
    val cardsPlayed: Int = 0,
    val coins: Int = 500
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String,
    val result: String, // "VICTORY" or "DEFEAT"
    val score: Int,
    val rulesSummary: String,
    val opponentNames: String
)

@Entity(tableName = "custom_house_rules")
data class HouseRulePresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetName: String,
    val stacking: Boolean,
    val sevenZero: Boolean,
    val jumpIn: Boolean,
    val drawToMatch: Boolean,
    val forcePlay: Boolean,
    val bluffChallenge: Boolean,
    val targetScore: Int,
    val turnTimer: Int
)
