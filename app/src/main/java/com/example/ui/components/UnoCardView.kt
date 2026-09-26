package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardColor
import com.example.model.CardType
import com.example.model.UnoCard

@Composable
fun UnoCardView(
    card: UnoCard?,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean = true,
    isPlayable: Boolean = false,
    isSelected: Boolean = false,
    width: Dp = 68.dp,
    height: Dp = 100.dp,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "playable_pulse")
    val pulseElevation by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_elevation"
    )

    val currentElevation = when {
        isSelected -> 14.dp
        isPlayable -> pulseElevation.dp
        else -> 3.dp
    }

    val yOffset = if (isSelected) (-14).dp else if (isPlayable) (-4).dp else 0.dp

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .offset(y = yOffset)
            .shadow(currentElevation, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isPlayable) {
                    Modifier.border(BorderStroke(2.5.dp, Color(0xFFFFD700)), RoundedCornerShape(10.dp))
                } else if (isSelected) {
                    Modifier.border(BorderStroke(2.5.dp, Color.White), RoundedCornerShape(10.dp))
                } else {
                    Modifier.border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(10.dp))
                }
            )
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White)
            ) {
                onClick?.invoke()
            }
            .testTag("uno_card_${card?.id ?: "back"}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        if (!isFaceUp || card == null) {
            UnoCardBack()
        } else {
            UnoCardFront(card = card)
        }
    }
}

@Composable
private fun UnoCardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F0C20))
                )
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Red inner border frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color(0xFFE52521), RoundedCornerShape(8.dp))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            // Dark center rotated oval
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(46.dp)
                    .rotate(-28f)
                    .background(Color.Black, CircleShape)
                    .border(2.dp, Color(0xFFFFCC00), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "UNO",
                    color = Color(0xFFFFCC00),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun UnoCardFront(card: UnoCard) {
    val bgBrush = when (card.color) {
        CardColor.RED -> Brush.verticalGradient(listOf(Color(0xFFFF473A), Color(0xFFC01B17)))
        CardColor.YELLOW -> Brush.verticalGradient(listOf(Color(0xFFFFDF00), Color(0xFFD49E00)))
        CardColor.GREEN -> Brush.verticalGradient(listOf(Color(0xFF34D399), Color(0xFF15803D)))
        CardColor.BLUE -> Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0369A1)))
        CardColor.WILD -> Brush.sweepGradient(
            listOf(
                Color(0xFFFF473A),
                Color(0xFFFFDF00),
                Color(0xFF34D399),
                Color(0xFF38BDF8),
                Color(0xFFFF473A)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(3.dp)
    ) {
        // White border trim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            // White tilted center oval
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(52.dp)
                    .rotate(-26f)
                    .background(Color.White.copy(alpha = 0.95f), CircleShape)
                    .border(1.dp, Color(0x33000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Center text or symbol
                if (card.color == CardColor.WILD && card.type == CardType.WILD) {
                    WildWheelSymbol(modifier = Modifier.size(32.dp))
                } else {
                    val centerTextColor = if (card.color == CardColor.YELLOW) Color(0xFFB45309) else card.color.composeColor
                    Text(
                        text = card.displayValue,
                        color = if (card.color == CardColor.WILD) Color(0xFF0F172A) else centerTextColor,
                        fontSize = if (card.displayValue.length > 2) 15.sp else 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Top-left index
            Text(
                text = card.displayValue,
                color = Color.White,
                fontSize = if (card.displayValue.length > 2) 9.sp else 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp, top = 2.dp)
            )

            // Bottom-right index (inverted)
            Text(
                text = card.displayValue,
                color = Color.White,
                fontSize = if (card.displayValue.length > 2) 9.sp else 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 2.dp)
                    .rotate(180f)
            )
        }
    }
}

@Composable
fun WildWheelSymbol(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.5.dp, Color.Black, CircleShape)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFE52521))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF007BC7))
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFFFCC00))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF2DB84D))
                )
            }
        }
    }
}
