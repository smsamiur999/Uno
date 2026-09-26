package com.example

import com.example.engine.UnoDeck
import com.example.model.CardColor
import com.example.model.CardType
import com.example.model.HouseRules
import com.example.model.UnoCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnoGameLogicTest {

    @Test
    fun deckGeneration_has108Cards() {
        val deck = UnoDeck.generateDeck()
        assertEquals(108, deck.size)

        // Wilds count
        val regularWilds = deck.count { it.type == CardType.WILD }
        val wildDrawFours = deck.count { it.type == CardType.WILD_DRAW_FOUR }
        assertEquals(4, regularWilds)
        assertEquals(4, wildDrawFours)
    }

    @Test
    fun isCardPlayable_colorMatch() {
        val activeCard = UnoCard(color = CardColor.RED, type = CardType.NUMBER, number = 5)
        val playCard = UnoCard(color = CardColor.RED, type = CardType.NUMBER, number = 9)

        val isPlayable = UnoDeck.isCardPlayable(
            card = playCard,
            activeCard = activeCard,
            activeColor = CardColor.RED,
            accumulatedDrawStack = 0,
            rules = HouseRules.OFFICIAL_RULES
        )
        assertTrue(isPlayable)
    }

    @Test
    fun isCardPlayable_numberMatchDifferentColor() {
        val activeCard = UnoCard(color = CardColor.RED, type = CardType.NUMBER, number = 5)
        val playCard = UnoCard(color = CardColor.BLUE, type = CardType.NUMBER, number = 5)

        val isPlayable = UnoDeck.isCardPlayable(
            card = playCard,
            activeCard = activeCard,
            activeColor = CardColor.RED,
            accumulatedDrawStack = 0,
            rules = HouseRules.OFFICIAL_RULES
        )
        assertTrue(isPlayable)
    }

    @Test
    fun isCardPlayable_stackingDrawRules() {
        val activeDrawTwo = UnoCard(color = CardColor.RED, type = CardType.DRAW_TWO)
        val responseDrawTwo = UnoCard(color = CardColor.BLUE, type = CardType.DRAW_TWO)
        val regularCard = UnoCard(color = CardColor.RED, type = CardType.NUMBER, number = 2)

        // Under Stacking Rules:
        val canStack = UnoDeck.isCardPlayable(
            card = responseDrawTwo,
            activeCard = activeDrawTwo,
            activeColor = CardColor.RED,
            accumulatedDrawStack = 2,
            rules = HouseRules.SPICY_CHAOS
        )
        assertTrue(canStack)

        // Regular number card cannot be played during active draw penalty
        val cannotPlayNumber = UnoDeck.isCardPlayable(
            card = regularCard,
            activeCard = activeDrawTwo,
            activeColor = CardColor.RED,
            accumulatedDrawStack = 2,
            rules = HouseRules.SPICY_CHAOS
        )
        assertFalse(cannotPlayNumber)
    }

    @Test
    fun canJumpIn_identicalCard() {
        val activeCard = UnoCard(color = CardColor.GREEN, type = CardType.NUMBER, number = 7)
        val identicalCard = UnoCard(color = CardColor.GREEN, type = CardType.NUMBER, number = 7)
        val differentColor = UnoCard(color = CardColor.BLUE, type = CardType.NUMBER, number = 7)

        val jumpInRules = HouseRules(jumpIn = true)

        assertTrue(UnoDeck.canJumpIn(identicalCard, activeCard, jumpInRules))
        assertFalse(UnoDeck.canJumpIn(differentColor, activeCard, jumpInRules))
    }
}
