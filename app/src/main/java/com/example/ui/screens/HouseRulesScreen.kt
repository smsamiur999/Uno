package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HouseRules

@Composable
fun HouseRulesScreen(
    initialRules: HouseRules,
    onSaveRules: (HouseRules) -> Unit,
    onNavigateBack: () -> Unit
) {
    var rules by remember { mutableStateOf(initialRules) }
    var selectedPreset by remember { mutableStateOf("Custom") }

    BackHandler {
        onSaveRules(rules)
        onNavigateBack()
    }

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
            .testTag("house_rules_screen")
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
                IconButton(
                    onClick = {
                        onSaveRules(rules)
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("back_button_rules")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "House Rules Customizer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = {
                        rules = HouseRules.SPICY_CHAOS
                        selectedPreset = "Spicy Chaos"
                    },
                    modifier = Modifier.testTag("reset_rules_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Rules",
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Spicy Chaos" to HouseRules.SPICY_CHAOS,
                    "Tournament" to HouseRules.TOURNAMENT_STACKING,
                    "Official" to HouseRules.OFFICIAL_RULES
                ).forEach { (name, preset) ->
                    val isSelected = selectedPreset == name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPreset = name
                            rules = preset
                        },
                        label = { Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rules Toggles List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    RuleToggleCard(
                        title = "🔥 Stacking Draw (+2 & +4)",
                        description = "When a draw card is played against you, stack another draw card of equal or greater strength to pass the accumulated penalty forward!",
                        checked = rules.stackingDraw,
                        onCheckedChange = {
                            rules = rules.copy(stackingDraw = it)
                            selectedPreset = "Custom"
                        },
                        tag = "rule_stacking"
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🔄 7-0 Hand Swap & Rotation",
                        description = "Playing a 7 lets you choose any opponent to swap hands with. Playing a 0 forces ALL players to pass their entire hand in the turn direction!",
                        checked = rules.sevenZeroRule,
                        onCheckedChange = {
                            rules = rules.copy(sevenZeroRule = it)
                            selectedPreset = "Custom"
                        },
                        tag = "rule_seven_zero"
                    )
                }

                item {
                    RuleToggleCard(
                        title = "⚡ Jump-In (Identical Cards)",
                        description = "If you hold a card completely identical (same color AND same number/symbol) to the discard pile, play it immediately out of turn to steal the turn!",
                        checked = rules.jumpIn,
                        onCheckedChange = {
                            rules = rules.copy(jumpIn = it)
                            selectedPreset = "Custom"
                        },
                        tag = "rule_jump_in"
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🃏 Draw-to-Match",
                        description = "When you don't have a playable card, keep drawing cards from the pile until a playable card is found, instead of just drawing 1.",
                        checked = rules.drawToMatch,
                        onCheckedChange = {
                            rules = rules.copy(drawToMatch = it)
                            selectedPreset = "Custom"
                        },
                        tag = "rule_draw_to_match"
                    )
                }

                item {
                    RuleToggleCard(
                        title = "⚡ Force Play",
                        description = "If the card you just drew is playable, it must be played immediately without being kept in your hand.",
                        checked = rules.forcePlay,
                        onCheckedChange = {
                            rules = rules.copy(forcePlay = it)
                            selectedPreset = "Custom"
                        },
                        tag = "rule_force_play"
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🎯 Wild Draw 4 Bluff Challenge",
                        description = "If hit with a Wild +4, you can challenge! If they held matching color they draw 4; if innocent you draw 6!",
                        checked = rules.bluffChallenge,
                        onCheckedChange = {
                            rules = rules.copy(bluffChallenge = it)
                            selectedPreset = "Custom"
                        },
                        tag = "rule_bluff_challenge"
                    )
                }

                item {
                    // Turn Timer selector
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⏱️ Turn Time Limit",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Time allocated per turn before auto-drawing a card",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 15, 30, 0).forEach { seconds ->
                                    val isCurrent = rules.turnTimeLimitSeconds == seconds
                                    FilterChip(
                                        selected = isCurrent,
                                        onClick = {
                                            rules = rules.copy(turnTimeLimitSeconds = seconds)
                                            selectedPreset = "Custom"
                                        },
                                        label = {
                                            Text(
                                                text = if (seconds == 0) "No Timer" else "${seconds}s",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF38BDF8),
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color(0xFF334155),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Target Score selector
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🏆 Win Goal (Target Score)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Winner of each round accumulates points from opponents' remaining cards",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0 to "1 Round", 250 to "250 pts", 500 to "500 pts").forEach { (pts, label) ->
                                    val isCurrent = rules.targetScore == pts
                                    FilterChip(
                                        selected = isCurrent,
                                        onClick = {
                                            rules = rules.copy(targetScore = pts)
                                            selectedPreset = "Custom"
                                        },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF22C55E),
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color(0xFF334155),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Apply Button
            Button(
                onClick = {
                    onSaveRules(rules)
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_rules_btn")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("APPLY HOUSE RULES", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun RuleToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) Color(0x66FFD700) else Color(0x22FFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (checked) Color(0xFFFFD700) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF22C55E),
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF334155)
                )
            )
        }
    }
}
