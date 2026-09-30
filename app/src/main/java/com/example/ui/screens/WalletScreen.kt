package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyInfo
import com.example.domain.model.LedgerRecord
import com.example.domain.model.PaymentAgent
import com.example.domain.model.VerifiedPaymentAgentsRegistry
import com.example.domain.model.WalletBalance
import com.example.domain.model.WorldwideCryptoRegistry
import com.example.domain.model.WorldwideCurrencies
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
import com.example.ui.viewmodel.WalletUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    walletBalance: WalletBalance,
    ledgerRecords: List<LedgerRecord>,
    uiState: WalletUiState,
    onOpenDeposit: () -> Unit,
    onCloseDeposit: () -> Unit,
    onOpenWithdraw: () -> Unit,
    onCloseWithdraw: () -> Unit,
    onDepositAmountChange: (String) -> Unit,
    onWithdrawAmountChange: (String) -> Unit,
    onBankAccountChange: (String) -> Unit,
    onProcessDeposit: () -> Unit,
    onProcessWithdraw: () -> Unit,
    onSelectCurrency: (String) -> Unit = {},
    onSelectDepositRail: (String) -> Unit = {},
    onSelectAgent: (PaymentAgent) -> Unit = {}
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss dd MMM", Locale.getDefault())
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var selectedWalletTab by remember { mutableIntStateOf(0) } // 0: Ledger, 1: Payment Agents, 2: Crypto Rails

    val currentCurrency = WorldwideCurrencies.getByCode(uiState.selectedCurrencyCode)
    // Convert base NGN balance to user's selected worldwide currency
    val convertedAvailable = (walletBalance.availableBalance / 1620.0) * currentCurrency.exchangeRateToUsd
    val convertedReserved = (walletBalance.reservedBalance / 1620.0) * currentCurrency.exchangeRateToUsd
    val convertedLocked = (walletBalance.lockedBalance / 1620.0) * currentCurrency.exchangeRateToUsd
    val convertedRealized = (walletBalance.realizedPnl / 1620.0) * currentCurrency.exchangeRateToUsd

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(horizontal = 16.dp)
            .testTag("wallet_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            // Title & Currency Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GLOBAL WALLET",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Multi-Currency & Payment Agents",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Currency selector chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TerminalSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightGold.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { showCurrencyPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentCurrency.flag, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${currentCurrency.code} (${currentCurrency.symbol})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CurrencyExchange,
                            contentDescription = "Switch Currency",
                            tint = BrightGold,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Balance Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = TerminalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AVAILABLE TO TRADE / WITHDRAW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${currentCurrency.symbol}${"%,.2f".format(convertedAvailable)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentCurrency.code,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (currentCurrency.code != "NGN") {
                        Text(
                            text = "Base Ledger: ₦${"%,.0f".format(walletBalance.availableBalance)} NGN",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Balances Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "Hold", tint = BrightGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reserved Stake", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                "${currentCurrency.symbol}${"%,.2f".format(convertedReserved)}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = "Escrow", tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Escrow Locked", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                "${currentCurrency.symbol}${"%,.2f".format(convertedLocked)}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Realized P&L", fontSize = 10.sp, color = TextMuted)
                            Text(
                                "${if (convertedRealized >= 0) "+" else ""}${currentCurrency.symbol}${"%,.2f".format(convertedRealized)}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (convertedRealized >= 0) MarketGreen else MarketRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Deposit & Withdraw
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenDeposit,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_open_deposit"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarketGreen,
                                contentColor = TerminalDarkBg
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Deposit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DEPOSIT", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenWithdraw,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_open_withdraw"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TerminalSurfaceVariant,
                                contentColor = ElectricCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Withdraw", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WITHDRAW", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Sub-tabs: 0 -> Ledger, 1 -> Payment Agents, 2 -> Crypto Rails
            TabRow(
                selectedTabIndex = selectedWalletTab,
                containerColor = TerminalSurface,
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedWalletTab]),
                        color = ElectricCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedWalletTab == 0,
                    onClick = { selectedWalletTab = 0 },
                    text = { Text("AUDIT LEDGER", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedWalletTab == 1,
                    onClick = { selectedWalletTab = 1 },
                    text = { Text("VERIFIED AGENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedWalletTab == 2,
                    onClick = { selectedWalletTab = 2 },
                    text = { Text("CRYPTO RAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // TAB 0: Double-Entry Immutable Ledger
        if (selectedWalletTab == 0) {
            if (ledgerRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No financial ledger entries yet", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(ledgerRecords, key = { it.id }) { record ->
                    val isCredit = record.credit > 0
                    val amount = if (isCredit) record.credit else record.debit
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = TerminalSurface,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, TerminalBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (isCredit) MarketGreen.copy(alpha = 0.15f) else MarketRed.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = if (isCredit) "CREDIT" else "DEBIT",
                                        tint = if (isCredit) MarketGreen else MarketRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = record.description,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${timeFormat.format(Date(record.timestamp))} • ${record.accountType}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isCredit) "+" else "-"}₦${"%,.0f".format(amount)}",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = if (isCredit) MarketGreen else MarketRed
                                )
                                Text(
                                    text = "Ref: ${record.referenceId.take(12)}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: Verified Payment Agents (P2P Network)
        if (selectedWalletTab == 1) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TerminalSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Agents", tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Verified local payment agents enable instantaneous cash-in and cash-out via local bank rails, mobile money, and instant transfer.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(VerifiedPaymentAgentsRegistry.agents, key = { it.agentId }) { agent ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(agent.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = BrightGold, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${agent.rating} (${agent.completedTransactions})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrightGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${agent.country} • Rails: ${agent.supportedRails.joinToString(", ")}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Limit: ${agent.currency} ${"%,.0f".format(agent.minLimit)} - ${"%,.0f".format(agent.maxLimit)}", fontSize = 10.sp, color = TextSecondary)
                                Text("Fee: ${agent.feePercent}% • Avg: ${agent.avgResponseMin} min", fontSize = 10.sp, color = TextMuted)
                            }

                            Button(
                                onClick = {
                                    onSelectAgent(agent)
                                    onSelectDepositRail("AGENTS")
                                    onOpenDeposit()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = TerminalDarkBg),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("TRADE WITH AGENT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: Crypto Rails
        if (selectedWalletTab == 2) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TerminalSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = "Crypto", tint = BrightGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Fast multi-chain settlement supported for USDT, BTC, ETH, SOL, and USDC with automated block-height verification.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(WorldwideCryptoRegistry.assets, key = { it.symbol }) { crypto ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = TerminalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${crypto.name} (${crypto.symbol})", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                Text("Min Deposit: ${crypto.minDeposit} ${crypto.symbol}", fontSize = 10.sp, color = TextMuted)
                            }

                            Button(
                                onClick = {
                                    onSelectDepositRail("CRYPTO")
                                    onOpenDeposit()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrightGold, contentColor = TerminalDarkBg),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("GET DEPOSIT VAULT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Networks: " + crypto.supportedNetworks.joinToString(", ") { it.displayName }, fontSize = 11.sp, color = ElectricCyan)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Currency Switcher Modal Dialog
    if (showCurrencyPicker) {
        AlertDialog(
            onDismissRequest = { showCurrencyPicker = false },
            containerColor = TerminalDarkBg,
            title = {
                Text("Select Worldwide Currency", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(WorldwideCurrencies.list) { curr ->
                        val isSelected = curr.code == currentCurrency.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCurrency(curr.code)
                                    showCurrencyPicker = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(curr.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${curr.code} - ${curr.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) ElectricCyan else TextPrimary)
                                    Text("Symbol: ${curr.symbol} • Rate: 1 USD = ${curr.exchangeRateToUsd} ${curr.code}", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCurrencyPicker = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceVariant, contentColor = TextPrimary)
                ) {
                    Text("CLOSE")
                }
            }
        )
    }

    // DEPOSIT DIALOG (Supports Bank, Agents, and Crypto)
    if (uiState.isDepositOpen) {
        AlertDialog(
            onDismissRequest = onCloseDeposit,
            containerColor = TerminalDarkBg,
            title = {
                Text("FUND YOUR ACCOUNT", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            },
            text = {
                Column {
                    // Rail selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("BANK" to "Bank Transfer", "AGENTS" to "P2P Agent", "CRYPTO" to "Crypto Vault").forEach { (rail, label) ->
                            val isSel = uiState.depositRail == rail
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) ElectricCyan.copy(alpha = 0.2f) else TerminalSurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) ElectricCyan else TerminalBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSelectDepositRail(rail) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) ElectricCyan else TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.depositRail == "CRYPTO") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerminalSurface,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("DEPOSIT ASSET: ${uiState.selectedCryptoSymbol} (${uiState.selectedCryptoNetwork})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrightGold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Vault Address: TY8ZqP7xR3Vw4...USDT_OFFICIAL", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { clipboardManager.setText(AnnotatedString("TY8ZqP7xR3Vw4xK981Lmn294USDT_OFFICIAL")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceVariant, contentColor = ElectricCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("COPY VAULT ADDRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (uiState.depositRail == "AGENTS") {
                        val ag = uiState.selectedAgent ?: VerifiedPaymentAgentsRegistry.agents[0]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerminalSurface,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("AGENT: ${ag.name} (Verified ★ ${ag.rating})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                Text("Accepts: ${ag.supportedRails.joinToString(", ")}", fontSize = 10.sp, color = TextSecondary)
                                Text("Response Time: ~${ag.avgResponseMin} mins", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    Text("Enter Amount (${currentCurrency.code} ${currentCurrency.symbol})", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.depositAmount,
                        onValueChange = onDepositAmountChange,
                        modifier = Modifier.fillMaxWidth().testTag("input_deposit_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(uiState.errorMessage, fontSize = 11.sp, color = MarketRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onProcessDeposit,
                    enabled = !uiState.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = MarketGreen, contentColor = TerminalDarkBg)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TerminalDarkBg)
                    } else {
                        Text("CONFIRM DEPOSIT", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = onCloseDeposit,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceVariant, contentColor = TextPrimary)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    // WITHDRAW DIALOG (Supports Bank, Agents, and Crypto)
    if (uiState.isWithdrawOpen) {
        AlertDialog(
            onDismissRequest = onCloseWithdraw,
            containerColor = TerminalDarkBg,
            title = {
                Text("WITHDRAW CAPITAL", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            },
            text = {
                Column {
                    Text("Withdraw Amount (${currentCurrency.code} ${currentCurrency.symbol})", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.withdrawAmount,
                        onValueChange = onWithdrawAmountChange,
                        modifier = Modifier.fillMaxWidth().testTag("input_withdraw_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Destination Account / IBAN / Wallet Address", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.bankAccountNumber,
                        onValueChange = onBankAccountChange,
                        modifier = Modifier.fillMaxWidth().testTag("input_withdraw_account"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("4-Digit Withdrawal Security PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrightGold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = "••••",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightGold,
                            unfocusedBorderColor = TerminalBorder,
                            focusedTextColor = BrightGold,
                            unfocusedTextColor = BrightGold
                        ),
                        singleLine = true
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(uiState.errorMessage, fontSize = 11.sp, color = MarketRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onProcessWithdraw,
                    enabled = !uiState.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = TerminalDarkBg)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TerminalDarkBg)
                    } else {
                        Text("REQUEST WITHDRAWAL", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = onCloseWithdraw,
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurfaceVariant, contentColor = TextPrimary)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}
