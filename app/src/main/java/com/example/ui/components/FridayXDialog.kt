package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FridayXEvent
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceHighlight
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FridayXDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onEnterEvent: (entryUsd: Double) -> Unit
) {
    if (!isOpen) return

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var entryAmountText by remember { mutableStateOf("50") }
    var entryConfirmed by remember { mutableStateOf(false) }
    val event = remember { FridayXEvent("EVT-FRI-X-994") }

    val entryUsd = entryAmountText.toDoubleOrNull() ?: 50.0
    val maxPotentialReturn = (entryUsd * 900.0).coerceAtMost(9000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalDarkBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(BrightGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = "Surge", tint = BrightGold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.title, fontSize = 15.sp, fontWeight = FontWeight.Black, color = BrightGold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("friday_x_dialog")) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TerminalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MAX POTENTIAL OUTCOME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("STATUS: ${event.status}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MarketGreen)
                        }
                        Text(
                            text = "$9,000 USD (900x Multiplier)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = BrightGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Weekly financial liquidity surge indexed to global volatility. Strict non-custodial settlement rules applied.",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Entry Range: $10 - $500
                Text("ENTER PARTICIPATION AMOUNT (MIN $10 - MAX $500 USD)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = entryAmountText,
                    onValueChange = { entryAmountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightGold,
                        unfocusedBorderColor = TerminalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Potential Peak Return:", fontSize = 11.sp, color = TextMuted)
                    Text(
                        "$${"%,.0f".format(maxPotentialReturn)} USD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MarketGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Provably Fair Cryptographic Hash Proof
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TerminalSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = "Hash", tint = ElectricCyan, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PROVABLE HASH COMMITMENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(event.hashProof)) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hash", tint = TextMuted, modifier = Modifier.size(12.dp))
                            }
                        }
                        Text(
                            text = event.hashProof,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }
                }

                if (entryConfirmed) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("✓ Entry of $${entryAmountText} USD Confirmed for Friday X!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    entryConfirmed = true
                    onEnterEvent(entryUsd)
                },
                enabled = entryUsd in 10.0..500.0 && !entryConfirmed,
                colors = ButtonDefaults.buttonColors(containerColor = BrightGold, contentColor = TerminalDarkBg)
            ) {
                Text("ENTER FRIDAY X ($${"%,.0f".format(entryUsd)} USD)", fontWeight = FontWeight.Black)
            }
        }
    )
}
