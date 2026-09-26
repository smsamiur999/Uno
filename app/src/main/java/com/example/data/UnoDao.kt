package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UnoDao {

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllMatchHistory(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchHistory(match: MatchHistoryEntity)

    @Query("DELETE FROM match_history")
    suspend fun clearMatchHistory()

    @Query("SELECT * FROM custom_house_rules ORDER BY id DESC")
    fun getAllCustomHouseRules(): Flow<List<HouseRulePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomHouseRule(rule: HouseRulePresetEntity)

    @Query("DELETE FROM custom_house_rules WHERE id = :id")
    suspend fun deleteCustomHouseRule(id: Long)
}
