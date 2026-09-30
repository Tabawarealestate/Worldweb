package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AuditLogEntity
import com.example.domain.model.FinancialProductConfig
import com.example.domain.model.ProductType
import com.example.domain.model.ServiceStatus
import com.example.domain.model.SystemHealth
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
import com.example.ui.viewmodel.AdminUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    state: AdminUiState,
    systemStatus: List<SystemHealth>,
    activeProducts: List<FinancialProductConfig>,
    auditLogs: List<AuditLogEntity>,
    onBack: () -> Unit,
    onFormChange: (name: String, symbol: String, type: ProductType, dur: Long, min: Double, max: Double, fee: Double, payout: Double, rule: String) -> Unit,
    onSaveProduct: () -> Unit
) {
    BackHandler { onBack() }
    val timeFormat = SimpleDateFormat("HH:mm:ss dd MMM", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "ADMINISTRATION CENTER",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "System Status • Product Builder • Immutable Audit Trail",
                    fontSize = 11.sp,
                    color = BrightGold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Public System Status Grid
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = "Status", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PUBLIC SYSTEM HEALTH STATUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(10.dp))

                systemStatus.forEach { health ->
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
                                    .size(7.dp)
                                    .background(
                                        when (health.status) {
                                            ServiceStatus.OPERATIONAL -> MarketGreen
                                            ServiceStatus.DEGRADED -> BrightGold
                                            ServiceStatus.OUTAGE -> MarketRed
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(health.componentName, fontSize = 12.sp, color = TextPrimary)
                        }

                        Text(
                            text = "${health.status.name} (${health.latencyMs}ms)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when (health.status) {
                                ServiceStatus.OPERATIONAL -> MarketGreen
                                ServiceStatus.DEGRADED -> BrightGold
                                ServiceStatus.OUTAGE -> MarketRed
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Financial Product Builder Form
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = "Product Builder", tint = BrightGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CREATE FINANCIAL PRODUCT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.productName,
                    onValueChange = { onFormChange(it, state.productSymbol, state.productType, state.durationSeconds, state.minStake, state.maxStake, state.feeRate, state.payoutRate, state.settlementRule) },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TerminalSurfaceVariant,
                        unfocusedContainerColor = TerminalSurfaceVariant,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = TerminalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.productSymbol,
                        onValueChange = { onFormChange(state.productName, it, state.productType, state.durationSeconds, state.minStake, state.maxStake, state.feeRate, state.payoutRate, state.settlementRule) },
                        label = { Text("Instrument") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalSurfaceVariant,
                            unfocusedContainerColor = TerminalSurfaceVariant,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = state.durationSeconds.toString(),
                        onValueChange = {
                            val dur = it.toLongOrNull() ?: 60L
                            onFormChange(state.productName, state.productSymbol, state.productType, dur, state.minStake, state.maxStake, state.feeRate, state.payoutRate, state.settlementRule)
                        },
                        label = { Text("Duration (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalSurfaceVariant,
                            unfocusedContainerColor = TerminalSurfaceVariant,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.minStake.toLong().toString(),
                        onValueChange = {
                            val m = it.toDoubleOrNull() ?: 500.0
                            onFormChange(state.productName, state.productSymbol, state.productType, state.durationSeconds, m, state.maxStake, state.feeRate, state.payoutRate, state.settlementRule)
                        },
                        label = { Text("Min Stake (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalSurfaceVariant,
                            unfocusedContainerColor = TerminalSurfaceVariant,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = state.payoutRate.toString(),
                        onValueChange = {
                            val p = it.toDoubleOrNull() ?: 1.85
                            onFormChange(state.productName, state.productSymbol, state.productType, state.durationSeconds, state.minStake, state.maxStake, state.feeRate, p, state.settlementRule)
                        },
                        label = { Text("Payout Mult (e.g. 1.85)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TerminalSurfaceVariant,
                            unfocusedContainerColor = TerminalSurfaceVariant,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.settlementRule,
                    onValueChange = { onFormChange(state.productName, state.productSymbol, state.productType, state.durationSeconds, state.minStake, state.maxStake, state.feeRate, state.payoutRate, it) },
                    label = { Text("Authoritative Settlement Ruleset") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TerminalSurfaceVariant,
                        unfocusedContainerColor = TerminalSurfaceVariant,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = TerminalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (state.successMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(state.successMessage, color = MarketGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onSaveProduct,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("admin_save_product_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = TerminalDarkBg
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TerminalDarkBg)
                    } else {
                        Text("PUBLISH FINANCIAL PRODUCT", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Immutable Audit Log Inspector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ListAlt, contentDescription = "Audit Logs", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("IMMUTABLE AUDIT LOG STREAM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(10.dp))

                auditLogs.take(15).forEach { log ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.action} • ${log.actor}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Text(
                                text = timeFormat.format(Date(log.timestamp)),
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = log.details,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(TerminalBorder)
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
