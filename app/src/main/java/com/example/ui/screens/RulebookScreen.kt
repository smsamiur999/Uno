package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RulebookScreen(onNavigateBack: () -> Unit) {
    BackHandler { onNavigateBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1B4B), Color(0xFF0F0C20), Color(0xFF030206))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("rulebook_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("rulebook_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "UNO Arena Rulebook",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    RuleSectionCard(
                        title = "🎯 Objective",
                        content = "Be the first player to get rid of all the cards in your hand in each round. Score points from the cards remaining in your opponents' hands!"
                    )
                }

                item {
                    RuleSectionCard(
                        title = "💥 The UNO! Call",
                        content = "When you have only 2 cards remaining and play one, you MUST immediately call UNO! by pressing the UNO button. If you forget and another player taps CATCH before the next turn, you will be penalized with 2 draw cards!"
                    )
                }

                item {
                    RuleSectionCard(
                        title = "🔥 House Rule: Stacking Draw (+2 & +4)",
                        content = "When someone plays a Draw Two (+2) or Wild Draw Four (+4), you don't have to accept the draw if you hold a valid draw card! Stack another Draw Two or Wild Draw Four on top. The accumulated penalty transfers to the next player until someone cannot stack and must draw the entire accumulated amount!"
                    )
                }

                item {
                    RuleSectionCard(
                        title = "🔄 House Rule: 7-0 Hand Swap & Rotation",
                        content = "• Playing a 7: You can immediately choose ANY opponent to trade hands with! Highly strategic when an opponent has only 1 or 2 cards.\n• Playing a 0: ALL players must pass their entire hand to the next player in the current direction of play!"
                    )
                }

                item {
                    RuleSectionCard(
                        title = "⚡ House Rule: Jump-In",
                        content = "If you hold a card that is completely identical to the top card of the discard pile (same color AND same value/symbol), you can play it immediately out of turn! Play then continues from your turn position."
                    )
                }

                item {
                    RuleSectionCard(
                        title = "🎯 Wild Draw 4 Bluff Challenge",
                        content = "A Wild Draw 4 can legally only be played if you do NOT hold any card in your hand that matches the current active color. The victim has the right to challenge: If guilty, the bluffer draws 4 cards. If innocent, the challenger draws 6 cards (4 + 2 penalty)!"
                    )
                }

                item {
                    RuleSectionCard(
                        title = "🏆 Scoring & Points",
                        content = "• Number cards (0-9): Face value\n• Draw Two (+2): 20 points\n• Reverse (⇄): 20 points\n• Skip (⊘): 20 points\n• Wild (★): 50 points\n• Wild Draw Four (+4): 50 points\nFirst player to reach the Target Score (e.g. 250 or 500 points) wins the match!"
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleSectionCard(title: String, content: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
