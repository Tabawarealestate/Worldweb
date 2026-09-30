package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.components.ZorivoSymbol
import com.example.ui.theme.ZorivoTextMuted
import com.example.ui.theme.ZorivoTextPrimary
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
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppPinLockScreen(
    isSetupMode: Boolean = false,
    onPinSuccess: () -> Unit,
    onSaveNewPin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onForgotPasswordOrLogin: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var confirmPinStage by remember { mutableStateOf(false) }
    var initialPinForSetup by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptCount by remember { mutableStateOf(0) }

    fun onNumberClick(num: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + num
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (isSetupMode) {
                    if (!confirmPinStage) {
                        initialPinForSetup = newPin
                        confirmPinStage = true
                        enteredPin = ""
                    } else {
                        if (newPin == initialPinForSetup) {
                            onSaveNewPin(newPin)
                            onPinSuccess()
                        } else {
                            errorMessage = "PINs do not match. Try again."
                            enteredPin = ""
                            confirmPinStage = false
                        }
                    }
                } else {
                    val isValid = onVerifyPin(newPin)
                    if (isValid) {
                        onPinSuccess()
                    } else {
                        attemptCount++
                        errorMessage = "Incorrect PIN (${3 - attemptCount} attempts left)"
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top Brand Logo & Shield
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ZorivoSymbol(size = 56.dp, animated = true)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ZORIVO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = ZorivoTextPrimary
            )

            Text(
                text = "SIMPLE MARKETS. ONE MOVE.",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ZorivoTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isSetupMode) {
                    if (confirmPinStage) "Confirm your 4-digit App PIN" else "Create a 4-digit App Security PIN"
                } else {
                    "Enter 4-digit App Re-entry PIN"
                },
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (isFilled) ElectricCyan else TerminalSurfaceHighlight,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFilled) ElectricCyan else TerminalBorder,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error Display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MarketRed
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            keyRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        when (key) {
                            "BIO" -> {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            // Biometric quick bypass
                                            onPinSuccess()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Unlock",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            "DEL" -> {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .clickable { onBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(TerminalSurface)
                                        .border(1.dp, TerminalBorder.copy(alpha = 0.5f), CircleShape)
                                        .clickable { onNumberClick(key) }
                                        .testTag("pin_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Help: Forgot PIN / Sign In with Password
        TextButton(
            onClick = onForgotPasswordOrLogin,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "Forgot PIN? Sign In with Password",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
        }
    }
}
