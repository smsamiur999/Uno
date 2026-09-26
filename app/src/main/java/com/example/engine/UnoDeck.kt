package com.example.engine

import com.example.model.CardColor
import com.example.model.CardType
import com.example.model.HouseRules
import com.example.model.UnoCard
import java.util.Collections
import java.util.UUID

object UnoDeck {

    fun generateDeck(): List<UnoCard> {
        val cards = mutableListOf<UnoCard>()
        val coloredSuits = listOf(CardColor.RED, CardColor.YELLOW, CardColor.GREEN, CardColor.BLUE)

        for (color in coloredSuits) {
            // One '0' card per color
            cards.add(UnoCard(id = UUID.randomUUID().toString(), color = color, type = CardType.NUMBER, number = 0))

            // Two of each 1-9 per color
            for (num in 1..9) {
                cards.add(UnoCard(id = UUID.randomUUID().toString(), color = color, type = CardType.NUMBER, number = num))
                cards.add(UnoCard(id = UUID.randomUUID().toString(), color = color, type = CardType.NUMBER, number = num))
            }

            // Two of each action card per color
            repeat(2) {
                cards.add(UnoCard(id = UUID.randomUUID().toString(), color = color, type = CardType.SKIP))
                cards.add(UnoCard(id = UUID.randomUUID().toString(), color = color, type = CardType.REVERSE))
                cards.add(UnoCard(id = UUID.randomUUID().toString(), color = color, type = CardType.DRAW_TWO))
            }
        }

        // 4 Wild cards
        repeat(4) {
            cards.add(UnoCard(id = UUID.randomUUID().toString(), color = CardColor.WILD, type = CardType.WILD))
        }

        // 4 Wild Draw Four cards
        repeat(4) {
            cards.add(UnoCard(id = UUID.randomUUID().toString(), color = CardColor.WILD, type = CardType.WILD_DRAW_FOUR))
        }

        return cards.shuffled()
    }

    fun isCardPlayable(
        card: UnoCard,
        activeCard: UnoCard,
        activeColor: CardColor,
        accumulatedDrawStack: Int,
        rules: HouseRules
    ): Boolean {
        // If there is an active draw penalty stack
        if (accumulatedDrawStack > 0) {
            if (!rules.stackingDraw) return false
            // Under stacking rules:
            return when (activeCard.type) {
                CardType.DRAW_TWO -> card.type == CardType.DRAW_TWO || card.type == CardType.WILD_DRAW_FOUR
                CardType.WILD_DRAW_FOUR -> card.type == CardType.WILD_DRAW_FOUR || card.type == CardType.DRAW_TWO
                else -> false
            }
        }

        // Wild cards are always playable on normal turns
        if (card.color == CardColor.WILD || card.type == CardType.WILD || card.type == CardType.WILD_DRAW_FOUR) {
            return true
        }

        // Color matches active color chosen
        if (card.color == activeColor) {
            return true
        }

        // Matching number for number cards
        if (card.type == CardType.NUMBER && activeCard.type == CardType.NUMBER) {
            if (card.number != null && card.number == activeCard.number) {
                return true
            }
        }

        // Matching action type (e.g. Reverse on Reverse, Skip on Skip, Draw Two on Draw Two)
        if (card.type != CardType.NUMBER && card.type == activeCard.type) {
            return true
        }

        return false
    }

    fun canJumpIn(card: UnoCard, activeCard: UnoCard, rules: HouseRules): Boolean {
        if (!rules.jumpIn) return false
        // Wild cards cannot jump-in to avoid ambiguity, identical colored cards can jump-in
        if (card.color == CardColor.WILD || activeCard.color == CardColor.WILD) return false
        return card.color == activeCard.color && card.type == activeCard.type && card.number == activeCard.number
    }

    fun calculateHandScore(cards: List<UnoCard>): Int {
        return cards.sumOf { it.scoreValue }
    }
}
