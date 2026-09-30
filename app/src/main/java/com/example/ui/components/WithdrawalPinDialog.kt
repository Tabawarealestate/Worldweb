package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WithdrawalPinDialog(
    isCreateMode: Boolean,
    onDismiss: () -> Unit,
    onPinConfirmed: (String) -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var confirmStage by remember { mutableStateOf(false) }
    var firstPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onNumber(n: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + n
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (isCreateMode) {
                    if (!confirmStage) {
                        firstPin = newPin
                        confirmStage = true
                        enteredPin = ""
                    } else {
                        if (newPin == firstPin) {
                            onPinConfirmed(newPin)
                        } else {
                            errorMessage = "PINs do not match. Try again."
                            confirmStage = false
                            enteredPin = ""
                        }
                    }
                } else {
                    onPinConfirmed(newPin)
                }
            }
        }
    }

    fun onBack() {
        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TerminalDarkBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Security", tint = BrightGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isCreateMode) {
                        if (confirmStage) "CONFIRM WITHDRAWAL PIN" else "CREATE 4-DIGIT WITHDRAWAL PIN"
                    } else "ENTER WITHDRAWAL PIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCreateMode) {
                        if (confirmStage) "Re-enter your 4-digit PIN to confirm" else "Choose a 4-digit security PIN to authorize all funds releases."
                    } else "Authorized withdrawal protection is active.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    if (isFilled) BrightGold else TerminalSurfaceHighlight,
                                    CircleShape
                                )
                                .border(1.dp, if (isFilled) BrightGold else TerminalBorder, CircleShape)
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 11.sp, color = MarketRed)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { k ->
                            if (k == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable { onBack() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            } else if (k.isEmpty()) {
                                Spacer(modifier = Modifier.size(48.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(TerminalSurface)
                                        .border(1.dp, TerminalBorder, CircleShape)
                                        .clickable { onNumber(k) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(k, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        }
    )
}
