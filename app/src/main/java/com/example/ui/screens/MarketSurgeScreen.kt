package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SurgeRoundStatus
import com.example.domain.model.WalletBalance
import com.example.ui.theme.*
import com.example.ui.viewmodel.SurgeUiState
import kotlin.math.min

data class MaskedPlayerEntry(
    val identifier: String,
    val entryUsd: Double,
    val cashOutMultiplier: Double?,
    val status: String // ACTIVE, EXITED, SETTLED
)

data class SurgeChatMessage(
    val sender: String,
    val message: String,
    val timestamp: String
)

@Composable
fun MarketSurgeScreen(
    state: SurgeUiState,
    walletBalance: WalletBalance,
    onStakeChange: (Double) -> Unit,
    onAutoCashoutChange: (Double?) -> Unit,
    onEnterRound: () -> Unit,
    onCashOut: () -> Unit
) {
    val round = state.currentRound
    val isBurst = round?.status == SurgeRoundStatus.BURST
    val currentMultiplier = round?.currentMultiplier ?: 1.00
    val userEntry = state.userEntry
    val isEntered = userEntry != null && userEntry.status == "ACTIVE"
    val isCashedOut = userEntry != null && userEntry.status == "CASHED_OUT"
    val isBusted = userEntry != null && userEntry.status == "BUSTED"

    val quickStakes = listOf(1000.0, 2000.0, 5000.0, 10000.0, 25000.0)
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: PLAYERS, 1: ROUND HISTORY, 2: MESSAGE ROOM, 3: TRANSPARENCY

    // Masked players panel (Section 146, 159)
    val livePlayers = remember {
        listOf(
            MaskedPlayerEntry("XXXXX05", 10.0, 2.00, "EXITED"),
            MaskedPlayerEntry("TRADER84", 25.0, 1.65, "EXITED"),
            MaskedPlayerEntry("USER_1092", 50.0, null, "ACTIVE"),
            MaskedPlayerEntry("ALPHA_99", 100.0, null, "ACTIVE"),
            MaskedPlayerEntry("XXXXX21", 15.0, 1.40, "EXITED")
        )
    }

    // Historical rounds (Section 149)
    val roundHistory = remember {
        listOf(
            Triple("MS-000821", 2.00, "Settled (Peak 2.00x)"),
            Triple("MS-000820", 1.42, "Settled (Peak 1.42x)"),
            Triple("MS-000819", 1.87, "Settled (Peak 1.87x)"),
            Triple("MS-000818", 3.10, "Settled (Peak 3.10x)"),
            Triple("MS-000817", 1.25, "Settled (Peak 1.25x)")
        )
    }

    // Message Room (Section 153-156)
    var inputMessage by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            SurgeChatMessage("TRADER84", "BTC volatility rising fast on 1m chart! 🚀", "10:42"),
            SurgeChatMessage("MOD_OFFICIAL", "Reminder: Never share PINs or passwords in public chat.", "10:43"),
            SurgeChatMessage("XXXXX05", "Cashed out at 2.00x clean. Good round!", "10:45")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("market_surge_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Screen Header
        var isSoundEnabled by remember { mutableStateOf(true) }
        var isVibrationEnabled by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(ZorivoPrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, ZorivoPrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = "Market Surge",
                        tint = ZorivoElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MARKET SURGE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = ZorivoTextPrimary
                    )
                    Text(
                        text = "Luminous Vector Movement • BTC/USD",
                        fontSize = 10.5.sp,
                        color = ZorivoTextMuted
                    )
                }
            }

            // Sound & Haptic Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSoundEnabled) ZorivoPrimaryBlue.copy(alpha = 0.2f) else ZorivoDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSoundEnabled) ZorivoPrimaryBlue else ZorivoBorder),
                    modifier = Modifier.clickable { isSoundEnabled = !isSoundEnabled }
                ) {
                    Text(
                        text = if (isSoundEnabled) "AUDIO ON" else "MUTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSoundEnabled) ZorivoBlueHighlight else ZorivoTextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ZorivoDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorder)
                ) {
                    Text(
                        text = "Ref: $${"%,.2f".format(round?.referencePrice ?: 84166.93)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ZorivoTextSecondary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Central Surge Canvas Graph (Section 142, 145)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .testTag("surge_canvas_container"),
            shape = RoundedCornerShape(16.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isBurst) MarketRed.copy(alpha = 0.5f) else ElectricCyan.copy(alpha = 0.4f)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 40f
                    val paddingLeft = 40f

                    // Grid reference lines (1.50x, 2.00x)
                    val y2x = h - paddingBottom - (h - paddingBottom - 40f) * 0.65f
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.25f),
                        start = Offset(paddingLeft, y2x),
                        end = Offset(w - 20f, y2x),
                        strokeWidth = 1.5f
                    )

                    // Draw curved surge path
                    val curveProgress = ((currentMultiplier - 1.0) / 3.0).coerceIn(0.05, 0.95).toFloat()
                    val targetX = paddingLeft + (w - paddingLeft - 40f) * curveProgress
                    val targetY = (h - paddingBottom) - (h - paddingBottom - 30f) * curveProgress

                    val path = Path().apply {
                        moveTo(paddingLeft, h - paddingBottom)
                        quadraticTo(
                            (paddingLeft + targetX) / 2,
                            h - paddingBottom,
                            targetX,
                            targetY
                        )
                    }

                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = if (isBurst) listOf(ZorivoSignalRed, ZorivoSoftRed) else listOf(ZorivoPrimaryBlue, ZorivoBlueHighlight)
                        ),
                        style = Stroke(width = 6f)
                    )

                    // ZORIVO Luminous Geometric Energy Node (Original Vector Object)
                    if (isBurst) {
                        drawCircle(
                            color = ZorivoSignalRed,
                            radius = 10f,
                            center = Offset(targetX, targetY)
                        )
                    } else {
                        // Outer Glow Ring
                        drawCircle(
                            color = ZorivoPrimaryBlue.copy(alpha = 0.35f),
                            radius = 16f,
                            center = Offset(targetX, targetY)
                        )
                        // Core Blue Energy Node
                        drawCircle(
                            color = ZorivoBlueHighlight,
                            radius = 8f,
                            center = Offset(targetX, targetY)
                        )
                        // Center White Nucleus
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = Offset(targetX, targetY)
                        )
                    }
                }

                // Top Status & Multiplier Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isBurst) MarketRed.copy(alpha = 0.2f) else MarketGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isBurst) "ROUND BURST" else "SURGE ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBurst) MarketRed else MarketGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Round #${round?.roundId?.take(8) ?: "SRG-984"}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }

                    // Large Multiplier Center Display
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${"%.2f".format(currentMultiplier)}x",
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (isBurst) MarketRed else BrightGold
                            )
                            if (isBurst) {
                                Text(
                                    text = "Burst at ${"%.2f".format(currentMultiplier)}x",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MarketRed
                                )
                            }
                        }
                    }

                    // 2.00x Target indicator hint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "2.00x Horizon Target",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons: Either CASH OUT or ENTER
        if (isEntered && !isBurst) {
            val livePayout = (userEntry?.stake ?: 0.0) * currentMultiplier
            Button(
                onClick = onCashOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("surge_cashout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightGold,
                    contentColor = TerminalDarkBg
                )
            ) {
                Text(
                    text = "CASH OUT ₦${"%,.0f".format(livePayout)} (${"%.2f".format(currentMultiplier)}x)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            // Stake controls & Enter button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ROUND STAKE (NGN ₦)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = if (state.stakeInput > 0) state.stakeInput.toLong().toString() else "",
                        onValueChange = { str ->
                            val n = str.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                            onStakeChange(n)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("surge_stake_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = TextPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalSurfaceVariant,
                            unfocusedContainerColor = TerminalSurfaceVariant,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickStakes.forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .background(TerminalSurfaceHighlight, RoundedCornerShape(6.dp))
                                    .clickable { onStakeChange(amt) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+₦${(amt / 1000).toLong()}k",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onEnterRound,
                        enabled = !isBurst && !state.isEntering && state.stakeInput >= 500 && !isEntered,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("surge_enter_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = TerminalDarkBg,
                            disabledContainerColor = TerminalSurfaceHighlight,
                            disabledContentColor = TextDisabled
                        )
                    ) {
                        if (state.isEntering) {
                            CircularProgressIndicator(color = TerminalDarkBg, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (isBurst) "WAITING FOR NEXT ROUND" else "ENTER SURGE (₦${"%,.0f".format(state.stakeInput)})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sub-Tabs Bar: PLAYERS | ROUND HISTORY | MESSAGE ROOM | RULES
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = TerminalSurface,
            contentColor = ElectricCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = ElectricCyan
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("PLAYERS", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("HISTORY", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("CHAT", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 3,
                onClick = { selectedSubTab = 3 },
                text = { Text("RULES", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TAB 0: PLAYERS (Sections 146, 147, 159)
        if (selectedSubTab == 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ACTIVE ROUND PARTICIPANTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    livePlayers.forEach { pl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(TerminalSurfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = "User", tint = ElectricCyan, modifier = Modifier.size(12.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pl.identifier, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$${"%,.0f".format(pl.entryUsd)}", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                if (pl.cashOutMultiplier != null) {
                                    Text(
                                        text = "${"%.2f".format(pl.cashOutMultiplier)}x ($${"%,.0f".format(pl.entryUsd * pl.cashOutMultiplier)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MarketGreen
                                    )
                                } else {
                                    Text(
                                        text = "In Flight",
                                        fontSize = 11.sp,
                                        color = ElectricCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: ROUND HISTORY (Section 149)
        if (selectedSubTab == 1) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("HISTORICAL ROUND OUTCOMES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    roundHistory.forEach { (roundId, peakMult, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("#$roundId", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(desc, fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                "${"%.2f".format(peakMult)}x",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = BrightGold
                            )
                        }
                    }
                }
            }
        }

        // TAB 2: MESSAGE ROOM (Section 153-156)
        if (selectedSubTab == 2) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Security Warning (Section 155)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TerminalSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Security Alert", tint = BrightGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Never post passwords, withdrawal PINs, or card details in chat.", fontSize = 9.sp, color = TextSecondary)
                        }
                    }

                    // Chat list
                    chatMessages.forEach { msg ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(msg.sender, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                Text(msg.timestamp, fontSize = 9.sp, color = TextMuted)
                            }
                            Text(msg.message, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = { Text("Join discussion...", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = TerminalBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    chatMessages.add(SurgeChatMessage("YOU", inputMessage, "Now"))
                                    inputMessage = ""
                                }
                            },
                            modifier = Modifier.size(40.dp).background(ElectricCyan, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = TerminalDarkBg, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // TAB 3: RULES & TRANSPARENCY (Section 167)
        if (selectedSubTab == 3) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AUDITABLE SETTLEMENT & CRYPTOGRAPHIC PROOF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Formula: Payout = Stake × Exit Multiplier.\n• Burst threshold is deterministically generated from SHA-256 hash before round opens.\n• Real market reference price: BTC/USD.\n• Zero client manipulation permitted.\n• Double-entry ledger integration.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seed Commitment: ${round?.hashProof?.take(28) ?: "c4ca4238a0b923820dcc509a7f"}...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BrightGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
