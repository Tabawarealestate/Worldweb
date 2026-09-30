package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContractDirection
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun VisualTradeDialog(
    isOpen: Boolean,
    symbol: String = "BTC/USD",
    initialPrice: Double = 105240.0,
    onDismiss: () -> Unit,
    onTradeComplete: (direction: ContractDirection, outcome: String, payout: Double) -> Unit
) {
    if (!isOpen) return

    var selectedDuration by remember { mutableIntStateOf(15) } // 10s, 15s, 20s
    var isRunning by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(15) }
    var currentTickingPrice by remember { mutableDoubleStateOf(initialPrice) }
    var selectedDirection by remember { mutableStateOf(ContractDirection.HIGHER) }
    var strikePrice by remember { mutableDoubleStateOf(initialPrice) }
    var tradeCompletedOutcome by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
            // Real market tick variation
            val tick = (Math.random() - 0.49) * 8.5
            currentTickingPrice += tick
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            val isWin = if (selectedDirection == ContractDirection.HIGHER) {
                currentTickingPrice > strikePrice
            } else {
                currentTickingPrice < strikePrice
            }
            val outcome = if (isWin) "WON" else "LOST"
            tradeCompletedOutcome = outcome
            val payout = if (isWin) 19.0 else 0.0
            onTradeComplete(selectedDirection, outcome, payout)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalDarkBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ULTRA-SHORT VISUAL TRADE", fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("$symbol • Fast Expiry Execution", fontSize = 11.sp, color = ElectricCyan)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("visual_trade_dialog")) {
                // Duration selection (10s, 15s, 20s)
                Text("SELECT EXPIRY HORIZON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 15, 20).forEach { dur ->
                        val isSel = selectedDuration == dur
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) ElectricCyan.copy(alpha = 0.2f) else TerminalSurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSel) ElectricCyan else TerminalBorder, RoundedCornerShape(8.dp))
                                .clickable(enabled = !isRunning) {
                                    selectedDuration = dur
                                    remainingSeconds = dur
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${dur}s Turbo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) ElectricCyan else TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Visual Price Track Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                            // Strike line in the middle
                            val midY = size.height / 2
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(0f, midY),
                                end = Offset(size.width, midY),
                                strokeWidth = 2f
                            )

                            // Ticking position dot
                            val priceDelta = currentTickingPrice - strikePrice
                            val dotY = (midY - (priceDelta * 4f).toFloat()).coerceIn(10f, size.height - 10f)
                            val dotColor = if (priceDelta >= 0) MarketGreen else MarketRed

                            drawCircle(
                                color = dotColor,
                                radius = 10f,
                                center = Offset(size.width * 0.7f, dotY)
                            )
                        }

                        // Overlay metrics
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("STRIKE: $${"%,.2f".format(strikePrice)}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = "Time", tint = ElectricCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${remainingSeconds}s", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ElectricCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                text = "LIVE: $${"%,.2f".format(currentTickingPrice)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (currentTickingPrice >= strikePrice) MarketGreen else MarketRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direction selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            selectedDirection = ContractDirection.HIGHER
                            strikePrice = currentTickingPrice
                            remainingSeconds = selectedDuration
                            isRunning = true
                            tradeCompletedOutcome = null
                        },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MarketGreen, contentColor = TerminalDarkBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Higher", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("HIGHER (${selectedDuration}s)", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = {
                            selectedDirection = ContractDirection.LOWER
                            strikePrice = currentTickingPrice
                            remainingSeconds = selectedDuration
                            isRunning = true
                            tradeCompletedOutcome = null
                        },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MarketRed, contentColor = TerminalDarkBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Lower", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOWER (${selectedDuration}s)", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                if (tradeCompletedOutcome != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Outcome: $tradeCompletedOutcome • Settled to Ledger",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tradeCompletedOutcome == "WON") MarketGreen else MarketRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceVariant, contentColor = TextPrimary)
            ) {
                Text("CLOSE")
            }
        }
    )
}
