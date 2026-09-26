package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameDirection
import com.example.model.Player
import com.example.model.QuickReaction

@Composable
fun OpponentAvatarView(
    player: Player,
    isCurrentTurn: Boolean,
    onCatchUno: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Column(
        modifier = modifier
            .width(96.dp)
            .testTag("opponent_${player.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                // Card count badge
                Badge(
                    containerColor = if (player.cardCount == 1) Color(0xFFEF4444) else Color(0xFF3B82F6),
                    contentColor = Color.White,
                    modifier = Modifier.offset(x = 6.dp, y = (-2).dp)
                ) {
                    Text(
                        text = "${player.cardCount} 🂠",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .then(
                        if (isCurrentTurn) {
                            Modifier.border(
                                width = 3.dp,
                                color = Color(0xFFFFD700).copy(alpha = haloPulse),
                                shape = CircleShape
                            )
                        } else {
                            Modifier.border(1.5.dp, Color(0x33FFFFFF), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.avatar,
                    fontSize = 26.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Player Name & Score
        Text(
            text = player.name,
            color = if (isCurrentTurn) Color(0xFFFFD700) else Color.White,
            fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // UNO called status or Catch UNO button
        if (player.hasCalledUno && player.cardCount == 1) {
            Surface(
                color = Color(0xFFDC2626),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "UNO!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        } else if (player.isVulnerableToUnoCall && onCatchUno != null) {
            Surface(
                color = Color(0xFFEAB308),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable { onCatchUno() }
                    .testTag("catch_uno_btn_${player.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Catch UNO",
                        tint = Color.Black,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "CATCH!",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DirectionIndicatorView(
    direction: GameDirection,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "direction_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = if (direction == GameDirection.CLOCKWISE) 0f else 360f,
        targetValue = if (direction == GameDirection.CLOCKWISE) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "direction_rotation"
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .rotate(rotation)
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0x0038BDF8),
                        Color(0xFF38BDF8),
                        Color(0xFFFBBF24),
                        Color(0x0038BDF8)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (direction == GameDirection.CLOCKWISE) "↻" else "↺",
            color = Color(0xFF38BDF8),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StackingBannerView(
    accumulatedDrawCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stack_glow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stack_alpha"
    )

    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .testTag("stacking_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDC2626).copy(alpha = alphaAnim)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🔥 DRAW STACK: +$accumulatedDrawCount CARDS!",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun TurnTimerBar(
    remainingSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 1f
    val barColor = when {
        progress > 0.5f -> Color(0xFF22C55E)
        progress > 0.25f -> Color(0xFFEAB308)
        else -> Color(0xFFEF4444)
    }

    Column(modifier = modifier.fillMaxWidth(0.6f)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0x33FFFFFF),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun QuickReactionOverlay(
    reaction: QuickReaction?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = reaction != null,
        enter = scaleIn(spring()) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        reaction?.let {
            Surface(
                color = Color(0xDD0F172A),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFFFFF)),
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = it.emoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = it.playerName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun <T> spring() = androidx.compose.animation.core.spring<T>()
