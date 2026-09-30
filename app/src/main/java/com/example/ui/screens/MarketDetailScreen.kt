package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContractDirection
import com.example.domain.model.Instrument
import com.example.domain.model.MarketQuote
import com.example.domain.model.MarketStatus
import com.example.domain.model.ProductType
import com.example.ui.components.MarketChart
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MarketDetailScreen(
    symbol: String,
    quote: MarketQuote?,
    instrument: Instrument?,
    onBack: () -> Unit,
    onOpenOrderSlip: (symbol: String, direction: ContractDirection, productType: ProductType, price: Double) -> Unit
) {
    BackHandler { onBack() }

    val isPositive = (quote?.change24hPercent ?: 0.0) >= 0
    val changeColor = if (isPositive) MarketGreen else MarketRed
    val timeFormat = SimpleDateFormat("HH:mm:ss dd MMM yyyy", Locale.getDefault())

    var selectedContractProduct by remember { mutableStateOf(ProductType.HIGHER_LOWER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("market_detail_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("market_detail_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = symbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = instrument?.name ?: symbol,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Market Live Status Badge
            Box(
                modifier = Modifier
                    .background(TerminalSurfaceVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (quote?.status == MarketStatus.OPEN) MarketGreen else MarketRed,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = quote?.status?.label ?: "SYNCING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (quote?.status == MarketStatus.OPEN) MarketGreen else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Price & Metrics Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (quote != null) formatPrice(symbol, quote.price) else "---",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "Change direction",
                                tint = changeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${"%.2f".format(quote?.change24h ?: 0.0)} (${"%.2f".format(quote?.change24hPercent ?: 0.0)}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = changeColor
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SPREAD",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (quote != null) formatSpread(symbol, quote.spread) else "--",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bid / Ask Split Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalDarkBg, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(
                            text = if (quote != null) formatPrice(symbol, quote.bid) else "--",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(TerminalBorder)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ASK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(
                            text = if (quote != null) formatPrice(symbol, quote.ask) else "--",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Financial Candlestick / Line Chart
        MarketChart(
            symbol = symbol,
            currentPrice = quote?.price ?: 0.0,
            isPositive = isPositive
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Market Statistics Grid
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MARKET SPECIFICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("24h High", formatPrice(symbol, quote?.high24h ?: 0.0))
                    StatItem("24h Low", formatPrice(symbol, quote?.low24h ?: 0.0))
                    StatItem("Min Stake", "₦${"%,.0f".format(instrument?.minStake ?: 500.0)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("Provider", quote?.providerName ?: "Authoritative Feed")
                    StatItem("Max Stake", "₦${"%,.0f".format(instrument?.maxStake ?: 500_000.0)}")
                    StatItem("Last Sync", if (quote != null) timeFormat.format(Date(quote.lastUpdated)).takeLast(8) else "--")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contract Product Selection Tabs
        Text(
            text = "AVAILABLE FINANCIAL PRODUCTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                ProductType.HIGHER_LOWER to "Higher/Lower",
                ProductType.ABOVE_BELOW to "Above/Below",
                ProductType.TOUCH_NO_TOUCH to "Touch Barrier",
                ProductType.RANGE to "Range"
            ).forEach { (prod, label) ->
                val isSelected = selectedContractProduct == prod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) ElectricCyan.copy(alpha = 0.18f) else TerminalSurface,
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (isSelected) ElectricCyan else TerminalBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedContractProduct = prod }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ElectricCyan else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Trading Execution Buttons (Directly launch Order Slip)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // BUY / HIGHER
            Button(
                onClick = {
                    onOpenOrderSlip(symbol, ContractDirection.HIGHER, selectedContractProduct, quote?.price ?: 0.0)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("detail_btn_buy_higher"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MarketGreen,
                    contentColor = TerminalDarkBg
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Buy", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BUY / HIGHER ↑",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // SELL / LOWER
            Button(
                onClick = {
                    onOpenOrderSlip(symbol, ContractDirection.LOWER, selectedContractProduct, quote?.price ?: 0.0)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("detail_btn_sell_lower"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MarketRed,
                    contentColor = TerminalDarkBg
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Sell", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SELL / LOWER ↓",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text(
            value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

private fun formatPrice(symbol: String, price: Double): String {
    return when {
        symbol.contains("JPY") -> "¥${"%,.2f".format(price)}"
        symbol.contains("EUR") || symbol.contains("GBP") || symbol.contains("AUD") || symbol.contains("CAD") || symbol.contains("CHF") -> "%.4f".format(price)
        price >= 1000.0 -> "$${"%,.2f".format(price)}"
        else -> "$${"%,.2f".format(price)}"
    }
}

private fun formatSpread(symbol: String, spread: Double): String {
    return if (spread < 0.01) "%.5f".format(spread) else "%.2f".format(spread)
}
