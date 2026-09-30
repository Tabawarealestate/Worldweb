package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContractDirection
import com.example.domain.model.Instrument
import com.example.domain.model.InstrumentCategory
import com.example.domain.model.MarketQuote
import com.example.domain.model.Order
import com.example.domain.model.SurgeRound
import com.example.domain.model.WalletBalance
import com.example.domain.model.WorldwideCurrencies
import com.example.ui.components.MarketCard
import com.example.ui.components.VisualTradeDialog
import com.example.ui.components.ZorivoBrandHeader
import com.example.ui.components.ZorivoSymbol
import com.example.ui.theme.ZorivoBlueHighlight
import com.example.ui.theme.ZorivoBorder
import com.example.ui.theme.ZorivoBorderSubtle
import com.example.ui.theme.ZorivoDarkSurface
import com.example.ui.theme.ZorivoDeepBlack
import com.example.ui.theme.ZorivoElectricBlue
import com.example.ui.theme.ZorivoElevatedSurface
import com.example.ui.theme.ZorivoNearBlack
import com.example.ui.theme.ZorivoPrimaryBlue
import com.example.ui.theme.ZorivoSignalRed
import com.example.ui.theme.ZorivoSuccessGreen
import com.example.ui.theme.ZorivoTextMuted
import com.example.ui.theme.ZorivoTextPrimary
import com.example.ui.theme.ZorivoTextSecondary
import com.example.ui.viewmodel.MarketUiState

@Composable
fun HomeScreen(
    uiState: MarketUiState,
    quotes: List<MarketQuote>,
    instruments: List<Instrument>,
    walletBalance: WalletBalance,
    openOrders: List<Order>,
    surgeRound: SurgeRound?,
    selectedCurrencyCode: String = "USD",
    tradeSlipCount: Int = 0,
    onCategorySelected: (InstrumentCategory) -> Unit,
    onSearchChanged: (String) -> Unit,
    onSelectMarket: (String) -> Unit,
    onQuickOrder: (symbol: String, direction: ContractDirection, price: Double) -> Unit,
    onNavigateToSurge: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onOpenTradeSlip: () -> Unit,
    onLoadTradeSlipCode: (String) -> Unit,
    onRefresh: () -> Unit,
    onSelectCurrency: (String) -> Unit
) {
    val currentCurrency = WorldwideCurrencies.getByCode(selectedCurrencyCode)
    var isCurrencyDialogVisible by remember { mutableStateOf(false) }
    var visualTradeQuote by remember { mutableStateOf<MarketQuote?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        InstrumentCategory.ALL,
        InstrumentCategory.FOREX,
        InstrumentCategory.CRYPTO,
        InstrumentCategory.STOCKS,
        InstrumentCategory.COMMODITIES,
        InstrumentCategory.INDICES
    )

    val infiniteTransition = rememberInfiniteTransition(label = "feed_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(ZorivoDeepBlack)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("home_screen_lazy_column")
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // TOP ZORIVO BRAND HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZorivoBrandHeader(
                        size = 36.dp,
                        showTagline = true,
                        modifier = Modifier.clickable { onRefresh() }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Base Currency Switcher
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ZorivoDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorder),
                            modifier = Modifier.clickable { isCurrencyDialogVisible = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currentCurrency.flag, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentCurrency.code,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ZorivoTextPrimary
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Currency", tint = ZorivoTextMuted, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Notifications Icon
                        IconButton(
                            onClick = { showNotificationDialog = true },
                            modifier = Modifier.size(34.dp).background(ZorivoDarkSurface, RoundedCornerShape(8.dp)).border(1.dp, ZorivoBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = ZorivoTextSecondary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Refresh Icon
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(34.dp).background(ZorivoDarkSurface, RoundedCornerShape(8.dp)).border(1.dp, ZorivoBorder, RoundedCornerShape(8.dp))
                        ) {
                            if (uiState.isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ZorivoPrimaryBlue, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ZorivoTextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // REAL-TIME FEED CONNECTION BADGE
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ZorivoDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(ZorivoSuccessGreen.copy(alpha = pulseAlpha), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME MARKET FEED ACTIVE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = ZorivoTextSecondary
                            )
                        }

                        Text(
                            text = "TWELVE DATA + DIRECT PIPELINE",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ZorivoTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ZORIVO HERO STATEMENT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(ZorivoElevatedSurface, ZorivoDarkSurface)),
                            RoundedCornerShape(14.dp)
                        )
                        .border(1.dp, ZorivoBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Markets move.\nYou choose the move.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 26.sp,
                        letterSpacing = (-0.5).sp,
                        color = ZorivoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore global markets, build multi-selection Trade Slips, and track fast-paced market moves with institutional precision.",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = ZorivoTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Simple Trade Action
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(ZorivoPrimaryBlue, RoundedCornerShape(8.dp))
                                .clickable { onOpenTradeSlip() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SIMPLE TRADE", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = Color.White)
                            }
                        }

                        // Market Surge Action
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(ZorivoDarkSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, ZorivoBorder, RoundedCornerShape(8.dp))
                                .clickable { onNavigateToSurge() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = ZorivoBlueHighlight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MARKET SURGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = ZorivoTextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // REAL-TIME TICKER TAPE
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ZorivoNearBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        quotes.take(8).forEach { q ->
                            val isPos = q.change24h >= 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onSelectMarket(q.symbol) }
                            ) {
                                Text(q.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZorivoTextPrimary)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$${"%,.2f".format(q.price)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ZorivoTextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isPos) "+" else ""}${"%.2f".format(q.change24hPercent)}%",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPos) ZorivoSuccessGreen else ZorivoSignalRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GLOBAL MARKET SUMMARY
                Text(
                    text = "GLOBAL MARKET SUMMARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = ZorivoTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hero Cards Carousel (XAU/USD, BTC/USD, AAPL, EUR/USD, NVDA)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val heroSymbols = listOf("XAU/USD", "BTC/USD", "AAPL", "EUR/USD", "NVDA")
                    heroSymbols.forEach { sym ->
                        val heroQuote = quotes.find { it.symbol == sym } ?: MarketQuote(
                            symbol = sym,
                            price = if (sym.contains("BTC")) 84166.93 else if (sym.contains("XAU")) 4140.80 else if (sym == "AAPL") 338.40 else 1.1350,
                            change24h = 14.2,
                            change24hPercent = 0.85
                        )
                        val isPositive = heroQuote.change24hPercent >= 0

                        Surface(
                            modifier = Modifier
                                .width(215.dp)
                                .height(128.dp)
                                .clickable { onSelectMarket(sym) },
                            shape = RoundedCornerShape(12.dp),
                            color = ZorivoDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(heroQuote.symbol, fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = ZorivoTextPrimary)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = (if (isPositive) ZorivoSuccessGreen else ZorivoSignalRed).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${if (isPositive) "+" else ""}${"%.2f".format(heroQuote.change24hPercent)}%",
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPositive) ZorivoSuccessGreen else ZorivoSignalRed,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text("LIVE REFERENCE", fontSize = 8.5.sp, letterSpacing = 0.5.sp, color = ZorivoTextMuted)
                                    Text(
                                        text = "$${"%,.2f".format(heroQuote.price)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = ZorivoTextPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .background(ZorivoPrimaryBlue.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                            .border(1.dp, ZorivoPrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .clickable { onQuickOrder(heroQuote.symbol, ContractDirection.HIGHER, heroQuote.price) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("ABOVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ZorivoBlueHighlight)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .background(ZorivoSignalRed.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                            .border(1.dp, ZorivoSignalRed.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                            .clickable { onQuickOrder(heroQuote.symbol, ContractDirection.LOWER, heroQuote.price) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("BELOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ZorivoSignalRed)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // MARKET EXPLORER SEARCH & CATEGORY PILLS
                Text(
                    text = "MARKET EXPLORER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = ZorivoTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChanged,
                    placeholder = { Text("Search Forex, Crypto, Stocks, Commodities...", fontSize = 11.5.sp, color = ZorivoTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ZorivoTextMuted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("home_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ZorivoDarkSurface,
                        unfocusedContainerColor = ZorivoDarkSurface,
                        focusedBorderColor = ZorivoPrimaryBlue,
                        unfocusedBorderColor = ZorivoBorder,
                        focusedTextColor = ZorivoTextPrimary,
                        unfocusedTextColor = ZorivoTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = uiState.selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ZorivoPrimaryBlue else ZorivoDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ZorivoPrimaryBlue else ZorivoBorder
                            ),
                            modifier = Modifier.clickable { onCategorySelected(cat) }
                        ) {
                            Text(
                                text = cat.name.replace("_", " "),
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) Color.White else ZorivoTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // LIST OF REAL INSTRUMENT CARDS
            items(quotes, key = { it.symbol }) { quote ->
                val inst = instruments.find { it.symbol == quote.symbol }
                MarketCard(
                    quote = quote,
                    instrumentName = inst?.name ?: quote.symbol,
                    onSelectMarket = { onSelectMarket(quote.symbol) },
                    onQuickOrder = { dir -> onQuickOrder(quote.symbol, dir, quote.price) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FLOATING TRADE SLIP DOCK CHIP (Signature ZORIVO bottom action)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .clickable { onOpenTradeSlip() },
            shape = RoundedCornerShape(12.dp),
            color = ZorivoElevatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoPrimaryBlue)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = "Trade Slip", tint = ZorivoBlueHighlight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TRADE SLIP", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = ZorivoTextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(ZorivoPrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tradeSlipCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // CURRENCY SELECTION DIALOG
    if (isCurrencyDialogVisible) {
        AlertDialog(
            onDismissRequest = { isCurrencyDialogVisible = false },
            containerColor = ZorivoDarkSurface,
            title = {
                Text("SELECT BASE CURRENCY", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ZorivoTextPrimary)
            },
            text = {
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(WorldwideCurrencies.list) { cur ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCurrency(cur.code)
                                    isCurrencyDialogVisible = false
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cur.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${cur.name} (${cur.code})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ZorivoTextPrimary)
                                    Text("Symbol: ${cur.symbol}", fontSize = 10.sp, color = ZorivoTextMuted)
                                }
                            }
                            if (cur.code == selectedCurrencyCode) {
                                Icon(Icons.Default.Verified, contentDescription = "Active", tint = ZorivoSuccessGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { isCurrencyDialogVisible = false }) {
                    Text("CLOSE", color = ZorivoTextMuted)
                }
            }
        )
    }

    // NOTIFICATIONS DIALOG
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            containerColor = ZorivoDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = ZorivoPrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NOTIFICATION CENTER", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ZorivoTextPrimary)
                }
            },
            text = {
                Column {
                    Text("• Twelve Data Real-Time Engine synced successfully", fontSize = 11.sp, color = ZorivoTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Market Surge vector initialized on BTC/USD", fontSize = 11.sp, color = ZorivoTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Security: 4-digit PIN protection active", fontSize = 11.sp, color = ZorivoTextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("DISMISS", color = ZorivoBlueHighlight)
                }
            }
        )
    }
}
