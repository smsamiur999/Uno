package com.example.engine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.MatchHistoryEntity
import com.example.data.PlayerProfileEntity
import com.example.data.UnoDatabase
import com.example.data.UnoRepository
import com.example.model.CardColor
import com.example.model.CardType
import com.example.model.GameActionLog
import com.example.model.GameDirection
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.HouseRules
import com.example.model.Player
import com.example.model.QuickReaction
import com.example.model.UnoCard
import com.example.multiplayer.LobbyState
import com.example.multiplayer.MultiplayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class UnoGameViewModel(application: Application) : AndroidViewModel(application) {

    val soundManager = SoundManager(application)
    val multiplayerManager = MultiplayerManager()
    private val repository: UnoRepository

    init {
        val database = UnoDatabase.getDatabase(application)
        repository = UnoRepository(database.unoDao())
    }

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.matchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lobbyState: StateFlow<LobbyState?> = multiplayerManager.lobbyState

    private val _gameState = MutableStateFlow(UnoGameState())
    val gameState: StateFlow<UnoGameState> = _gameState.asStateFlow()

    private var botJob: Job? = null
    private var timerJob: Job? = null

    init {
        // Initialize default player profile if none exists
        viewModelScope.launch {
            repository.playerProfile.collect { profile ->
                if (profile == null) {
                    repository.saveProfile(
                        PlayerProfileEntity(
                            id = 1,
                            nickname = "Player 1",
                            avatar = "🦊",
                            gamesPlayed = 0,
                            gamesWon = 0,
                            unoCallsMade = 0,
                            unoPenaltiesCaught = 0,
                            cardsPlayed = 0,
                            coins = 600
                        )
                    )
                }
            }
        }

        // Listen for reactions from multiplayer
        viewModelScope.launch {
            multiplayerManager.incomingReactions.collect { reaction ->
                _gameState.value = _gameState.value.copy(activeReaction = reaction)
                delay(3000)
                if (_gameState.value.activeReaction?.id == reaction.id) {
                    _gameState.value = _gameState.value.copy(activeReaction = null)
                }
            }
        }
    }

    fun startNewGame(
        mode: GameMode,
        playersList: List<Player>,
        rules: HouseRules,
        roomCode: String = ""
    ) {
        botJob?.cancel()
        timerJob?.cancel()

        var deck = UnoDeck.generateDeck().toMutableList()
        val dealtPlayers = playersList.map { player ->
            val hand = deck.take(7)
            deck = deck.drop(7).toMutableList()
            player.copy(
                hand = hand,
                hasCalledUno = false,
                isConcealed = mode == GameMode.PASS_AND_PLAY
            )
        }

        // Top card for discard pile must be a number card initially for clean start
        var firstCard = deck.removeAt(0)
        while (firstCard.color == CardColor.WILD || firstCard.type.isAction) {
            deck.add(firstCard)
            deck.shuffle()
            firstCard = deck.removeAt(0)
        }

        val initialLogs = listOf(
            GameActionLog(message = "Match started with ${rules.targetScore}pt goal!", tag = "INFO"),
            GameActionLog(message = "First card: ${firstCard.color.displayName} ${firstCard.displayValue}", tag = "INFO")
        )

        _gameState.value = UnoGameState(
            mode = mode,
            roomCode = roomCode,
            players = dealtPlayers,
            drawPile = deck,
            discardPile = listOf(firstCard),
            activeColor = firstCard.color,
            currentPlayerIndex = 0,
            direction = GameDirection.CLOCKWISE,
            accumulatedDrawStack = 0,
            status = GameStatus.PLAYING,
            houseRules = rules,
            logs = initialLogs,
            turnSecondsRemaining = if (rules.turnTimeLimitSeconds > 0) rules.turnTimeLimitSeconds else 15
        )

        soundManager.playCardSound()
        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    fun playCard(playerId: String, card: UnoCard, chosenColorIfWild: CardColor? = null) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return
        val player = state.players.find { it.id == playerId } ?: return
        if (state.currentPlayer?.id != playerId) return

        val activeCard = state.topDiscardCard ?: return
        val isValid = UnoDeck.isCardPlayable(
            card = card,
            activeCard = activeCard,
            activeColor = state.activeColor,
            accumulatedDrawStack = state.accumulatedDrawStack,
            rules = state.houseRules
        )

        if (!isValid) return

        // If card is wild and color not selected yet by human, request color picker dialog
        if (card.color == CardColor.WILD && chosenColorIfWild == null) {
            _gameState.value = state.copy(
                pendingWildColorPlayerId = playerId,
                previousColorBeforeWild = state.activeColor
            )
            return
        }

        // Execute card play
        executeCardPlay(player, card, chosenColorIfWild ?: CardColor.RED)
    }

    private fun executeCardPlay(player: Player, card: UnoCard, nextColor: CardColor) {
        timerJob?.cancel()
        val state = _gameState.value
        val updatedHand = player.hand.filter { it.id != card.id }
        val newDiscardPile = state.discardPile + card
        var newDrawPile = state.drawPile.toMutableList()

        soundManager.playCardSound()

        val logTag = if (card.type.isWild || card.type.isAction) "ACTION" else "PLAY"
        val cardName = if (card.color == CardColor.WILD) card.displayValue else "${card.color.displayName} ${card.displayValue}"
        val logs = (state.logs + GameActionLog(message = "${player.name} played $cardName", tag = logTag)).takeLast(25)

        // Check if player called UNO
        val hasCalledUno = if (updatedHand.size == 1) player.hasCalledUno else false

        val updatedPlayers = state.players.map {
            if (it.id == player.id) {
                it.copy(hand = updatedHand, hasCalledUno = hasCalledUno)
            } else it
        }

        // Check Round Win condition
        if (updatedHand.isEmpty()) {
            handleRoundVictory(player, updatedPlayers, newDiscardPile)
            return
        }

        // Determine next active color
        val effectiveColor = if (card.color == CardColor.WILD) nextColor else card.color

        // Handle Card Effects
        var newDirection = state.direction
        var newAccumulatedStack = state.accumulatedDrawStack
        var step = if (newDirection == GameDirection.CLOCKWISE) 1 else -1
        var nextPlayerIndex = Math.floorMod(state.currentPlayerIndex + step, state.players.size)
        var pendingSevenSwap: String? = null
        var pendingChallengeId: String? = null

        when (card.type) {
            CardType.REVERSE -> {
                if (state.players.size == 2) {
                    // In 2 player, reverse acts as a skip
                    nextPlayerIndex = state.currentPlayerIndex
                } else {
                    newDirection = newDirection.toggle()
                    step = if (newDirection == GameDirection.CLOCKWISE) 1 else -1
                    nextPlayerIndex = Math.floorMod(state.currentPlayerIndex + step, state.players.size)
                }
            }
            CardType.SKIP -> {
                // Skip the next player
                val skippedPlayer = state.players[nextPlayerIndex]
                logs.plus(GameActionLog(message = "${skippedPlayer.name} was skipped!", tag = "ACTION"))
                nextPlayerIndex = Math.floorMod(nextPlayerIndex + step, state.players.size)
            }
            CardType.DRAW_TWO -> {
                if (state.houseRules.stackingDraw) {
                    newAccumulatedStack += 2
                    soundManager.playStackAlertSound()
                } else {
                    // Non-stacking: immediate draw
                    val targetPlayer = updatedPlayers[nextPlayerIndex]
                    val newDiscardMutable = newDiscardPile.toMutableList()
                    val drawn = drawCardsFromPile(newDrawPile, newDiscardMutable, 2)
                    newDrawPile = drawn.first.toMutableList()
                    val afterDrawPlayers = updatedPlayers.map {
                        if (it.id == targetPlayer.id) it.copy(hand = it.hand + drawn.second) else it
                    }
                    nextPlayerIndex = Math.floorMod(nextPlayerIndex + step, state.players.size)
                    _gameState.value = state.copy(
                        players = afterDrawPlayers,
                        drawPile = newDrawPile,
                        discardPile = newDiscardPile,
                        activeColor = effectiveColor,
                        currentPlayerIndex = nextPlayerIndex,
                        direction = newDirection,
                        accumulatedDrawStack = 0,
                        logs = logs + GameActionLog(message = "${targetPlayer.name} drew 2 cards and was skipped!", tag = "ACTION"),
                        pendingWildColorPlayerId = null
                    )
                    startTurnTimer()
                    checkAndTriggerBotTurn()
                    return
                }
            }
            CardType.WILD_DRAW_FOUR -> {
                if (state.houseRules.stackingDraw) {
                    newAccumulatedStack += 4
                    soundManager.playStackAlertSound()
                } else if (state.houseRules.bluffChallenge && !state.players[nextPlayerIndex].isBot) {
                    // Give next human player a chance to challenge
                    pendingChallengeId = state.players[nextPlayerIndex].id
                } else {
                    // Non-stacking draw 4
                    val targetPlayer = updatedPlayers[nextPlayerIndex]
                    val newDiscardMutable = newDiscardPile.toMutableList()
                    val drawn = drawCardsFromPile(newDrawPile, newDiscardMutable, 4)
                    newDrawPile = drawn.first.toMutableList()
                    val afterDrawPlayers = updatedPlayers.map {
                        if (it.id == targetPlayer.id) it.copy(hand = it.hand + drawn.second) else it
                    }
                    nextPlayerIndex = Math.floorMod(nextPlayerIndex + step, state.players.size)
                    _gameState.value = state.copy(
                        players = afterDrawPlayers,
                        drawPile = newDrawPile,
                        discardPile = newDiscardPile,
                        activeColor = effectiveColor,
                        currentPlayerIndex = nextPlayerIndex,
                        direction = newDirection,
                        accumulatedDrawStack = 0,
                        logs = logs + GameActionLog(message = "${targetPlayer.name} drew 4 cards!", tag = "ACTION"),
                        pendingWildColorPlayerId = null
                    )
                    startTurnTimer()
                    checkAndTriggerBotTurn()
                    return
                }
            }
            CardType.NUMBER -> {
                // 7-0 Rule check
                if (state.houseRules.sevenZeroRule) {
                    if (card.number == 7) {
                        if (!player.isBot) {
                            pendingSevenSwap = player.id
                        } else {
                            // Bot swaps with player holding fewest cards
                            val target = state.players
                                .filter { it.id != player.id }
                                .minByOrNull { it.hand.size }
                            if (target != null) {
                                executeHandSwap(player.id, target.id)
                                return
                            }
                        }
                    } else if (card.number == 0) {
                        // Everyone rotates hands in turn direction!
                        executeAllRotateHands(newDirection)
                        return
                    }
                }
            }
            CardType.WILD -> {
                // Regular wild just changed color
            }
        }

        _gameState.value = state.copy(
            players = updatedPlayers,
            drawPile = newDrawPile,
            discardPile = newDiscardPile,
            activeColor = effectiveColor,
            currentPlayerIndex = nextPlayerIndex,
            direction = newDirection,
            accumulatedDrawStack = newAccumulatedStack,
            pendingWildColorPlayerId = null,
            pendingSevenSwapPlayerId = pendingSevenSwap,
            pendingWildDrawFourChallengerId = pendingChallengeId,
            wildPlayedByPlayerId = if (card.type == CardType.WILD_DRAW_FOUR) player.id else null,
            logs = logs,
            turnSecondsRemaining = if (state.houseRules.turnTimeLimitSeconds > 0) state.houseRules.turnTimeLimitSeconds else 15
        )

        // Pass and play conceal toggle
        if (state.mode == GameMode.PASS_AND_PLAY) {
            _gameState.value = _gameState.value.copy(
                players = _gameState.value.players.mapIndexed { idx, p ->
                    p.copy(isConcealed = idx != nextPlayerIndex)
                }
            )
        }

        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    fun drawCard(playerId: String) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return
        if (state.currentPlayer?.id != playerId) return

        timerJob?.cancel()
        val player = state.currentPlayer ?: return
        val activeCard = state.topDiscardCard ?: return

        var newDrawPile = state.drawPile.toMutableList()
        val newDiscardPile = state.discardPile.toMutableList()

        soundManager.playDrawSound()

        // If there is an accumulated draw penalty stack
        if (state.accumulatedDrawStack > 0) {
            val countToDraw = state.accumulatedDrawStack
            val drawn = drawCardsFromPile(newDrawPile, newDiscardPile, countToDraw)
            newDrawPile = drawn.first.toMutableList()

            val updatedPlayers = state.players.map {
                if (it.id == player.id) it.copy(hand = it.hand + drawn.second, hasCalledUno = false) else it
            }

            val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
            val nextIndex = Math.floorMod(state.currentPlayerIndex + step, state.players.size)

            _gameState.value = state.copy(
                players = updatedPlayers,
                drawPile = newDrawPile,
                discardPile = newDiscardPile,
                currentPlayerIndex = nextIndex,
                accumulatedDrawStack = 0,
                logs = state.logs + GameActionLog(
                    message = "${player.name} drew $countToDraw penalty cards!",
                    tag = "PENALTY"
                ),
                turnSecondsRemaining = if (state.houseRules.turnTimeLimitSeconds > 0) state.houseRules.turnTimeLimitSeconds else 15
            )
            soundManager.playPenaltyBuzz()
            startTurnTimer()
            checkAndTriggerBotTurn()
            return
        }

        // Draw to Match rule
        if (state.houseRules.drawToMatch) {
            val newlyDrawn = mutableListOf<UnoCard>()
            var foundPlayable = false
            var playableCard: UnoCard? = null

            while (!foundPlayable && (newDrawPile.isNotEmpty() || newDiscardPile.size > 1)) {
                val drawnSingle = drawCardsFromPile(newDrawPile, newDiscardPile, 1)
                newDrawPile = drawnSingle.first.toMutableList()
                val drawnCard = drawnSingle.second.firstOrNull() ?: break
                newlyDrawn.add(drawnCard)

                if (UnoDeck.isCardPlayable(drawnCard, activeCard, state.activeColor, 0, state.houseRules)) {
                    foundPlayable = true
                    playableCard = drawnCard
                }
            }

            val updatedPlayers = state.players.map {
                if (it.id == player.id) it.copy(hand = it.hand + newlyDrawn, hasCalledUno = false) else it
            }

            if (foundPlayable && state.houseRules.forcePlay && playableCard != null) {
                _gameState.value = state.copy(
                    players = updatedPlayers,
                    drawPile = newDrawPile,
                    discardPile = newDiscardPile,
                    logs = state.logs + GameActionLog(message = "${player.name} drew ${newlyDrawn.size} cards to match!", tag = "DRAW")
                )
                // Force play the playable card
                val chosenColor = if (playableCard.color == CardColor.WILD) CardColor.RED else playableCard.color
                executeCardPlay(updatedPlayers.first { it.id == player.id }, playableCard, chosenColor)
                return
            } else {
                val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
                val nextIndex = Math.floorMod(state.currentPlayerIndex + step, state.players.size)
                _gameState.value = state.copy(
                    players = updatedPlayers,
                    drawPile = newDrawPile,
                    discardPile = newDiscardPile,
                    currentPlayerIndex = nextIndex,
                    logs = state.logs + GameActionLog(message = "${player.name} drew ${newlyDrawn.size} cards.", tag = "DRAW")
                )
                startTurnTimer()
                checkAndTriggerBotTurn()
                return
            }
        }

        // Standard draw 1 card
        val drawn = drawCardsFromPile(newDrawPile, newDiscardPile, 1)
        newDrawPile = drawn.first.toMutableList()
        val drawnCard = drawn.second.firstOrNull()

        val updatedPlayers = state.players.map {
            if (it.id == player.id) it.copy(hand = it.hand + drawn.second, hasCalledUno = false) else it
        }

        // Check Force Play
        if (drawnCard != null && state.houseRules.forcePlay &&
            UnoDeck.isCardPlayable(drawnCard, activeCard, state.activeColor, 0, state.houseRules)
        ) {
            _gameState.value = state.copy(
                players = updatedPlayers,
                drawPile = newDrawPile,
                discardPile = newDiscardPile,
                logs = state.logs + GameActionLog(message = "${player.name} drew ${drawnCard.displayValue} and force-played it!", tag = "PLAY")
            )
            val chosenColor = if (drawnCard.color == CardColor.WILD) CardColor.RED else drawnCard.color
            executeCardPlay(updatedPlayers.first { it.id == player.id }, drawnCard, chosenColor)
            return
        }

        val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
        val nextIndex = Math.floorMod(state.currentPlayerIndex + step, state.players.size)

        _gameState.value = state.copy(
            players = updatedPlayers,
            drawPile = newDrawPile,
            discardPile = newDiscardPile,
            currentPlayerIndex = nextIndex,
            logs = state.logs + GameActionLog(message = "${player.name} drew a card.", tag = "DRAW")
        )

        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    fun callUno(playerId: String) {
        val state = _gameState.value
        val player = state.players.find { it.id == playerId } ?: return

        if (player.hand.size <= 2) {
            _gameState.value = state.copy(
                players = state.players.map {
                    if (it.id == playerId) it.copy(hasCalledUno = true) else it
                },
                logs = state.logs + GameActionLog(message = "💥 ${player.name} CALLED UNO!", tag = "UNO")
            )
            soundManager.playUnoCallSound()

            // Update stats
            if (!player.isBot) {
                viewModelScope.launch {
                    val profile = playerProfile.value ?: return@launch
                    repository.saveProfile(profile.copy(unoCallsMade = profile.unoCallsMade + 1))
                }
            }
        }
    }

    fun catchUnoPenalty(accuserId: String, targetPlayerId: String) {
        val state = _gameState.value
        val accuser = state.players.find { it.id == accuserId } ?: return
        val target = state.players.find { it.id == targetPlayerId } ?: return

        if (target.hand.size == 1 && !target.hasCalledUno) {
            var newDrawPile = state.drawPile.toMutableList()
            val drawn = drawCardsFromPile(newDrawPile, state.discardPile.toMutableList(), 2)
            newDrawPile = drawn.first.toMutableList()

            val updatedPlayers = state.players.map {
                if (it.id == target.id) it.copy(hand = it.hand + drawn.second, hasCalledUno = true) else it
            }

            _gameState.value = state.copy(
                players = updatedPlayers,
                drawPile = newDrawPile,
                logs = state.logs + GameActionLog(
                    message = "🚨 ${accuser.name} caught ${target.name} without calling UNO! (+2 Cards Penalty)",
                    tag = "PENALTY"
                )
            )
            soundManager.playPenaltyBuzz()

            if (!accuser.isBot) {
                viewModelScope.launch {
                    val profile = playerProfile.value ?: return@launch
                    repository.saveProfile(profile.copy(unoPenaltiesCaught = profile.unoPenaltiesCaught + 1))
                }
            }
        }
    }

    fun triggerJumpIn(playerId: String, card: UnoCard) {
        val state = _gameState.value
        if (!state.houseRules.jumpIn) return
        val activeCard = state.topDiscardCard ?: return
        if (!UnoDeck.canJumpIn(card, activeCard, state.houseRules)) return

        val player = state.players.find { it.id == playerId } ?: return
        val playerIdx = state.players.indexOfFirst { it.id == playerId }
        if (playerIdx == -1) return

        val updatedHand = player.hand.filter { it.id != card.id }
        val newDiscardPile = state.discardPile + card

        soundManager.playCardSound()

        val logs = state.logs + GameActionLog(
            message = "⚡ JUMP-IN! ${player.name} jumped in with ${card.color.displayName} ${card.displayValue}!",
            tag = "ACTION"
        )

        val updatedPlayers = state.players.map {
            if (it.id == player.id) it.copy(hand = updatedHand) else it
        }

        if (updatedHand.isEmpty()) {
            handleRoundVictory(player, updatedPlayers, newDiscardPile)
            return
        }

        val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
        val nextIndex = Math.floorMod(playerIdx + step, state.players.size)

        _gameState.value = state.copy(
            players = updatedPlayers,
            discardPile = newDiscardPile,
            activeColor = card.color,
            currentPlayerIndex = nextIndex,
            logs = logs
        )

        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    fun selectWildColor(color: CardColor) {
        val state = _gameState.value
        val playerId = state.pendingWildColorPlayerId ?: return
        val player = state.players.find { it.id == playerId } ?: return
        val card = player.hand.firstOrNull { it.color == CardColor.WILD } ?: return

        _gameState.value = state.copy(pendingWildColorPlayerId = null)
        executeCardPlay(player, card, color)
    }

    fun resolveSevenSwap(targetPlayerId: String) {
        val state = _gameState.value
        val initiatorId = state.pendingSevenSwapPlayerId ?: return
        _gameState.value = state.copy(pendingSevenSwapPlayerId = null)
        executeHandSwap(initiatorId, targetPlayerId)
    }

    private fun executeHandSwap(player1Id: String, player2Id: String) {
        val state = _gameState.value
        val p1 = state.players.find { it.id == player1Id } ?: return
        val p2 = state.players.find { it.id == player2Id } ?: return

        val updatedPlayers = state.players.map {
            when (it.id) {
                p1.id -> it.copy(hand = p2.hand, hasCalledUno = false)
                p2.id -> it.copy(hand = p1.hand, hasCalledUno = false)
                else -> it
            }
        }

        val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
        val nextIndex = Math.floorMod(state.currentPlayerIndex + step, state.players.size)

        _gameState.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = nextIndex,
            logs = state.logs + GameActionLog(
                message = "🔄 7-RULE: ${p1.name} swapped hands with ${p2.name}!",
                tag = "SWAP"
            )
        )
        soundManager.playStackAlertSound()
        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    private fun executeAllRotateHands(direction: GameDirection) {
        val state = _gameState.value
        val players = state.players
        val n = players.size
        val hands = players.map { it.hand }
        val shift = if (direction == GameDirection.CLOCKWISE) 1 else -1

        val updatedPlayers = players.mapIndexed { index, player ->
            val fromIndex = Math.floorMod(index - shift, n)
            player.copy(hand = hands[fromIndex], hasCalledUno = false)
        }

        val step = if (direction == GameDirection.CLOCKWISE) 1 else -1
        val nextIndex = Math.floorMod(state.currentPlayerIndex + step, n)

        _gameState.value = state.copy(
            players = updatedPlayers,
            currentPlayerIndex = nextIndex,
            logs = state.logs + GameActionLog(
                message = "🌀 0-RULE: All players rotated hands ${direction.name.lowercase()}!",
                tag = "SWAP"
            )
        )
        soundManager.playStackAlertSound()
        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    fun resolveWildDrawFourChallenge(challengerId: String, doChallenge: Boolean) {
        val state = _gameState.value
        val targetId = state.wildPlayedByPlayerId ?: return
        val challenger = state.players.find { it.id == challengerId } ?: return
        val target = state.players.find { it.id == targetId } ?: return

        var newDrawPile = state.drawPile.toMutableList()
        val newDiscardPile = state.discardPile.toMutableList()

        if (!doChallenge) {
            // Challenger accepts 4 cards
            val drawn = drawCardsFromPile(newDrawPile, newDiscardPile, 4)
            newDrawPile = drawn.first.toMutableList()

            val updatedPlayers = state.players.map {
                if (it.id == challenger.id) it.copy(hand = it.hand + drawn.second) else it
            }

            val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
            val nextIndex = Math.floorMod(state.players.indexOfFirst { it.id == challenger.id } + step, state.players.size)

            _gameState.value = state.copy(
                players = updatedPlayers,
                drawPile = newDrawPile,
                currentPlayerIndex = nextIndex,
                pendingWildDrawFourChallengerId = null,
                wildPlayedByPlayerId = null,
                logs = state.logs + GameActionLog(message = "${challenger.name} accepted 4 cards without challenge.", tag = "ACTION")
            )
        } else {
            // Resolve challenge: did target hold a card matching previous color?
            val previousColor = state.previousColorBeforeWild ?: CardColor.RED
            val wasBluffing = target.hand.any { it.color == previousColor }

            if (wasBluffing) {
                // Target was caught bluffing! Target draws 4 cards!
                val drawn = drawCardsFromPile(newDrawPile, newDiscardPile, 4)
                newDrawPile = drawn.first.toMutableList()
                val updatedPlayers = state.players.map {
                    if (it.id == target.id) it.copy(hand = it.hand + drawn.second) else it
                }
                _gameState.value = state.copy(
                    players = updatedPlayers,
                    drawPile = newDrawPile,
                    pendingWildDrawFourChallengerId = null,
                    wildPlayedByPlayerId = null,
                    logs = state.logs + GameActionLog(
                        message = "🎯 BLUFF CAUGHT! ${target.name} held ${previousColor.displayName} and draws 4 cards!",
                        tag = "ACTION"
                    )
                )
                soundManager.playPenaltyBuzz()
            } else {
                // Innocent! Challenger draws 6 cards (4 + 2 penalty)!
                val drawn = drawCardsFromPile(newDrawPile, newDiscardPile, 6)
                newDrawPile = drawn.first.toMutableList()
                val updatedPlayers = state.players.map {
                    if (it.id == challenger.id) it.copy(hand = it.hand + drawn.second) else it
                }
                val step = if (state.direction == GameDirection.CLOCKWISE) 1 else -1
                val nextIndex = Math.floorMod(state.players.indexOfFirst { it.id == challenger.id } + step, state.players.size)
                _gameState.value = state.copy(
                    players = updatedPlayers,
                    drawPile = newDrawPile,
                    currentPlayerIndex = nextIndex,
                    pendingWildDrawFourChallengerId = null,
                    wildPlayedByPlayerId = null,
                    logs = state.logs + GameActionLog(
                        message = "❌ FAILED CHALLENGE! ${target.name} was innocent. ${challenger.name} draws 6 cards!",
                        tag = "PENALTY"
                    )
                )
                soundManager.playPenaltyBuzz()
            }
        }

        startTurnTimer()
        checkAndTriggerBotTurn()
    }

    private fun handleRoundVictory(winner: Player, currentPlayers: List<Player>, discardPile: List<UnoCard>) {
        timerJob?.cancel()
        botJob?.cancel()
        soundManager.playVictorySound()

        val pointsEarned = currentPlayers
            .filter { it.id != winner.id }
            .sumOf { UnoDeck.calculateHandScore(it.hand) }

        val newScore = winner.score + pointsEarned
        val isOverallWon = stateIsGameOver(newScore, _gameState.value.houseRules.targetScore)

        val updatedPlayers = currentPlayers.map {
            if (it.id == winner.id) it.copy(score = newScore) else it
        }

        val updatedStatus = if (isOverallWon) GameStatus.GAME_OVER else GameStatus.ROUND_OVER
        val roundWinner = updatedPlayers.first { it.id == winner.id }

        _gameState.value = _gameState.value.copy(
            players = updatedPlayers,
            discardPile = discardPile,
            status = updatedStatus,
            roundWinner = roundWinner,
            overallWinner = if (isOverallWon) roundWinner else null,
            logs = _gameState.value.logs + GameActionLog(
                message = "🏆 ${winner.name} won the round with +$pointsEarned points!",
                tag = "WIN"
            )
        )

        // Save match stats in Room
        val human = updatedPlayers.firstOrNull { !it.isBot }
        if (human != null) {
            val isHumanWin = human.id == winner.id
            viewModelScope.launch {
                val profile = playerProfile.value ?: PlayerProfileEntity()
                repository.saveProfile(
                    profile.copy(
                        gamesPlayed = profile.gamesPlayed + 1,
                        gamesWon = if (isHumanWin) profile.gamesWon + 1 else profile.gamesWon,
                        coins = profile.coins + (if (isHumanWin) 100 else 20)
                    )
                )
                repository.recordMatch(
                    MatchHistoryEntity(
                        gameMode = _gameState.value.mode.title,
                        result = if (isHumanWin) "VICTORY" else "DEFEAT",
                        score = human.score,
                        rulesSummary = if (_gameState.value.houseRules.stackingDraw) "Stacking, 7-0" else "Standard",
                        opponentNames = updatedPlayers.filter { it.id != human.id }.joinToString(", ") { it.name }
                    )
                )
            }
        }
    }

    private fun stateIsGameOver(winnerScore: Int, targetScore: Int): Boolean {
        if (targetScore <= 0) return true
        return winnerScore >= targetScore
    }

    fun startNextRound() {
        val state = _gameState.value
        val players = state.players.map { it.copy(hand = emptyList(), hasCalledUno = false) }
        startNewGame(state.mode, players, state.houseRules, state.roomCode)
    }

    private fun drawCardsFromPile(
        drawPile: MutableList<UnoCard>,
        discardPile: MutableList<UnoCard>,
        count: Int
    ): Pair<List<UnoCard>, List<UnoCard>> {
        val drawn = mutableListOf<UnoCard>()
        repeat(count) {
            if (drawPile.isEmpty()) {
                if (discardPile.size > 1) {
                    val top = discardPile.removeAt(discardPile.size - 1)
                    drawPile.addAll(discardPile.shuffled())
                    discardPile.clear()
                    discardPile.add(top)
                }
            }
            if (drawPile.isNotEmpty()) {
                drawn.add(drawPile.removeAt(0))
            }
        }
        return Pair(drawPile, drawn)
    }

    private fun startTurnTimer() {
        timerJob?.cancel()
        val state = _gameState.value
        if (state.houseRules.turnTimeLimitSeconds <= 0 || state.status != GameStatus.PLAYING) return

        timerJob = viewModelScope.launch {
            var remaining = state.houseRules.turnTimeLimitSeconds
            while (remaining > 0 && _gameState.value.status == GameStatus.PLAYING) {
                _gameState.value = _gameState.value.copy(turnSecondsRemaining = remaining)
                delay(1000)
                remaining--
            }
            // Auto timeout: draw card if turn elapsed
            if (_gameState.value.status == GameStatus.PLAYING) {
                _gameState.value.currentPlayer?.let { drawCard(it.id) }
            }
        }
    }

    private fun checkAndTriggerBotTurn() {
        botJob?.cancel()
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return
        val currentPlayer = state.currentPlayer ?: return

        // Check if any opponent is vulnerable to UNO catch by bots
        val vulnerable = state.players.firstOrNull { it.isVulnerableToUnoCall && !it.isBot }
        if (vulnerable != null && Random.nextInt(100) < 65) {
            viewModelScope.launch {
                delay(Random.nextLong(1500, 3000))
                val bots = _gameState.value.players.filter { it.isBot }
                if (bots.isNotEmpty() && _gameState.value.status == GameStatus.PLAYING) {
                    catchUnoPenalty(bots.random().id, vulnerable.id)
                }
            }
        }

        if (currentPlayer.isBot) {
            botJob = viewModelScope.launch {
                delay(Random.nextLong(1000, 1900))
                executeBotMove(currentPlayer)
            }
        }
    }

    private fun executeBotMove(bot: Player) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING || state.currentPlayer?.id != bot.id) return

        val activeCard = state.topDiscardCard ?: return

        // 1. If bot has 2 cards and will play 1, call UNO!
        if (bot.hand.size == 2 && Random.nextInt(100) < 90) {
            callUno(bot.id)
        }

        // 2. Find playable cards
        val playableCards = bot.hand.filter { card ->
            UnoDeck.isCardPlayable(card, activeCard, state.activeColor, state.accumulatedDrawStack, state.houseRules)
        }

        if (playableCards.isNotEmpty()) {
            // Strategy:
            // - If stacking active, prioritize draw cards to avoid drawing!
            // - Prefer action cards (Skip, Reverse, Draw Two) over numbers
            // - Wild cards as fallback
            val chosenCard = if (state.accumulatedDrawStack > 0) {
                playableCards.firstOrNull { it.type == CardType.DRAW_TWO || it.type == CardType.WILD_DRAW_FOUR }
                    ?: playableCards.first()
            } else {
                playableCards.firstOrNull { it.type.isAction }
                    ?: playableCards.firstOrNull { it.type == CardType.NUMBER }
                    ?: playableCards.first()
            }

            // Pick color bot holds most of
            val colorCounts = bot.hand
                .filter { it.color.isColored }
                .groupBy { it.color }
                .mapValues { it.value.size }
            val bestColor = colorCounts.maxByOrNull { it.value }?.key ?: CardColor.RED

            executeCardPlay(bot, chosenCard, bestColor)
        } else {
            // Must draw
            drawCard(bot.id)
        }
    }

    fun sendEmojiReaction(playerId: String, emoji: String) {
        val player = _gameState.value.players.find { it.id == playerId } ?: return
        multiplayerManager.sendReaction(playerId, player.name, emoji)
    }

    fun revealPassAndPlayHand() {
        val state = _gameState.value
        val currentIdx = state.currentPlayerIndex
        _gameState.value = state.copy(
            players = state.players.mapIndexed { idx, p ->
                p.copy(isConcealed = idx != currentIdx)
            }
        )
    }

    fun updateProfile(nickname: String, avatar: String) {
        viewModelScope.launch {
            val current = playerProfile.value ?: PlayerProfileEntity()
            repository.saveProfile(current.copy(nickname = nickname.ifBlank { "Player 1" }, avatar = avatar))
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        botJob?.cancel()
        timerJob?.cancel()
    }
}
