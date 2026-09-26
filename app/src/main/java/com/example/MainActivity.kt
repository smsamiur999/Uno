package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.UnoGameViewModel
import com.example.model.HouseRules
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HouseRulesScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.ProfileAndHistoryScreen
import com.example.ui.screens.RulebookScreen
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    MAIN,
    LOBBY,
    GAME,
    HOUSE_RULES,
    PROFILE_HISTORY,
    RULEBOOK
}

class MainActivity : ComponentActivity() {

    private val viewModel: UnoGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0C20)
                ) {
                    UnoApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun UnoApp(viewModel: UnoGameViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    var activeHouseRules by remember { mutableStateOf(HouseRules.SPICY_CHAOS) }

    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val lobbyState by viewModel.lobbyState.collectAsStateWithLifecycle()
    val playerProfile by viewModel.playerProfile.collectAsStateWithLifecycle()
    val matchHistory by viewModel.matchHistory.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            Screen.MAIN -> {
                MainScreen(
                    viewModel = viewModel,
                    profile = playerProfile,
                    activeHouseRules = activeHouseRules,
                    onNavigateLobby = { currentScreen = Screen.LOBBY },
                    onNavigateGame = { currentScreen = Screen.GAME },
                    onNavigateHouseRules = { currentScreen = Screen.HOUSE_RULES },
                    onNavigateProfile = { currentScreen = Screen.PROFILE_HISTORY },
                    onNavigateRulebook = { currentScreen = Screen.RULEBOOK }
                )
            }

            Screen.LOBBY -> {
                LobbyScreen(
                    viewModel = viewModel,
                    lobbyState = lobbyState,
                    onStartGame = { currentScreen = Screen.GAME },
                    onNavigateHouseRules = { currentScreen = Screen.HOUSE_RULES },
                    onLeaveLobby = { currentScreen = Screen.MAIN }
                )
            }

            Screen.GAME -> {
                GameScreen(
                    viewModel = viewModel,
                    gameState = gameState,
                    onNavigateBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.HOUSE_RULES -> {
                HouseRulesScreen(
                    initialRules = activeHouseRules,
                    onSaveRules = { newRules ->
                        activeHouseRules = newRules
                        viewModel.multiplayerManager.updateRules(newRules)
                    },
                    onNavigateBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.PROFILE_HISTORY -> {
                ProfileAndHistoryScreen(
                    viewModel = viewModel,
                    profile = playerProfile,
                    matchHistory = matchHistory,
                    onNavigateBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.RULEBOOK -> {
                RulebookScreen(
                    onNavigateBack = { currentScreen = Screen.MAIN }
                )
            }
        }
    }
}
