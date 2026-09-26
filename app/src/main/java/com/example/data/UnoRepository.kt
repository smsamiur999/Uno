package com.example.data

import kotlinx.coroutines.flow.Flow

class UnoRepository(private val unoDao: UnoDao) {

    val playerProfile: Flow<PlayerProfileEntity?> = unoDao.getPlayerProfile()
    val matchHistory: Flow<List<MatchHistoryEntity>> = unoDao.getAllMatchHistory()
    val customHouseRules: Flow<List<HouseRulePresetEntity>> = unoDao.getAllCustomHouseRules()

    suspend fun saveProfile(profile: PlayerProfileEntity) {
        unoDao.savePlayerProfile(profile)
    }

    suspend fun recordMatch(match: MatchHistoryEntity) {
        unoDao.insertMatchHistory(match)
    }

    suspend fun saveCustomHouseRule(preset: HouseRulePresetEntity) {
        unoDao.insertCustomHouseRule(preset)
    }

    suspend fun deleteCustomHouseRule(id: Long) {
        unoDao.deleteCustomHouseRule(id)
    }
}
