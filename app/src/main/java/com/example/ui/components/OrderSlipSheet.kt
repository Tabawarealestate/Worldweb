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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContractDirection
import com.example.domain.model.ProductType
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceHighlight
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.OrderSlipState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSlipSheet(
    state: OrderSlipState,
    onDismiss: () -> Unit,
    onStakeChange: (Double) -> Unit,
    onDurationChange: (Long) -> Unit,
    onDirectionChange: (ContractDirection) -> Unit,
    onProductTypeChange: (ProductType) -> Unit,
    onSubmitOrder: () -> Unit
) {
    if (!state.isOpen) return

    val durations = listOf(
        60L to "1 min",
        300L to "5 min",
        900L to "15 min",
        3600L to "1 hr",
        86400L to "24 hr"
    )

    val quickStakes = listOf(1000.0, 5000.0, 10000.0, 25000.0, 50000.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TerminalDarkBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(TerminalBorder, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .testTag("order_slip_bottom_sheet")
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ElectricCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ORDER SLIP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.symbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Order Slip", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direction Selection (Sportsbook style Odds Pill)
            Text("PREDICTED DIRECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isHigher = state.direction == ContractDirection.HIGHER
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable { onDirectionChange(ContractDirection.HIGHER) }
                        .testTag("order_slip_direction_higher"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isHigher) MarketGreen else TerminalSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isHigher) MarketGreen else TerminalBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Higher",
                            tint = if (isHigher) TerminalDarkBg else MarketGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "HIGHER ↑",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isHigher) TerminalDarkBg else TextPrimary
                        )
                    }
                }

                val isLower = state.direction == ContractDirection.LOWER
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable { onDirectionChange(ContractDirection.LOWER) }
                        .testTag("order_slip_direction_lower"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLower) MarketRed else TerminalSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isLower) MarketRed else TerminalBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Lower",
                            tint = if (isLower) TerminalDarkBg else MarketRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "LOWER ↓",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isLower) TerminalDarkBg else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expiry Duration Selection
            Text("CONTRACT DURATION / EXPIRY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                durations.forEach { (sec, label) ->
                    val isSelected = state.durationSeconds == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                if (isSelected) ElectricCyan.copy(alpha = 0.2f) else TerminalSurfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isSelected) ElectricCyan else TerminalBorder, RoundedCornerShape(8.dp))
                            .clickable { onDurationChange(sec) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ElectricCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stake Input & Quick Chips
            Text("STAKE AMOUNT (NGN ₦)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = if (state.stake > 0) state.stake.toLong().toString() else "",
                onValueChange = { str ->
                    val num = str.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                    onStakeChange(num)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_slip_stake_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("₦ ", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = TerminalBorder,
                    focusedContainerColor = TerminalSurface,
                    unfocusedContainerColor = TerminalSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Stake Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickStakes.forEach { amt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(TerminalSurfaceHighlight, RoundedCornerShape(6.dp))
                            .clickable { onStakeChange(amt) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+₦${(amt / 1000).toLong()}k",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pre-submission Financial Breakdown Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Reference Strike Price", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = if (state.currentPrice > 0) "$${"%,.2f".format(state.currentPrice)}" else "Live Tick",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Execution Fee (1%)", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "₦${"%,.0f".format(state.fee)}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Potential Payout", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(
                            text = "₦${"%,.0f".format(state.potentialPayout)}",
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = MarketGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Net Profit", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "+₦${"%,.0f".format(state.potentialProfit)} (85% ROI)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MarketGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Settlement & Risk Disclosures
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Settlement Disclosure",
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settlement Rule: Settled against authoritative licensed feed at expiry. Invariant: Settled orders cannot re-settle.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarningOrange.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .border(1.dp, WarningOrange.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Capital Risk Warning",
                    tint = WarningOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Risk Warning: Derivatives carry high risk of capital loss. Never trade money you cannot afford to lose.",
                    fontSize = 10.sp,
                    color = WarningOrange
                )
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    fontSize = 12.sp,
                    color = MarketRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Submission Button
            Button(
                onClick = onSubmitOrder,
                enabled = !state.isSubmitting && state.stake >= 500,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("order_slip_submit_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = TerminalDarkBg,
                    disabledContainerColor = TerminalSurfaceHighlight,
                    disabledContentColor = TextDisabled
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = TerminalDarkBg,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "CONFIRM & PLACE ORDER (₦${"%,.0f".format(state.stake)})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
