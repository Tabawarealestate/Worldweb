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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContractDirection
import com.example.domain.model.MarketQuote
import com.example.domain.model.MarketStatus
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MarketCard(
    quote: MarketQuote,
    instrumentName: String = quote.symbol,
    onSelectMarket: () -> Unit,
    onQuickOrder: (direction: ContractDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = quote.change24hPercent >= 0
    val changeColor = if (isPositive) MarketGreen else MarketRed
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_card_${quote.symbol}")
            .clickable(onClick = onSelectMarket),
        shape = RoundedCornerShape(14.dp),
        color = TerminalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Symbol, Category & Market Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (quote.status == MarketStatus.OPEN) MarketGreen else MarketRed,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = quote.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = instrumentName,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }

                // Status & Provider badge
                Box(
                    modifier = Modifier
                        .background(TerminalSurfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (quote.status == MarketStatus.OPEN) "LIVE" else quote.status.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (quote.status == MarketStatus.OPEN) MarketGreen else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price and 24h Change Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatPrice(quote.symbol, quote.price),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Bid / Ask & Spread row
                    Text(
                        text = "Bid: ${formatPrice(quote.symbol, quote.bid)}  Ask: ${formatPrice(quote.symbol, quote.ask)}  Spr: ${formatSpread(quote.symbol, quote.spread)}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                // 24h % Pill
                Box(
                    modifier = Modifier
                        .background(changeColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, changeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (isPositive) "Price up" else "Price down",
                            tint = changeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}${"%.2f".format(quote.change24hPercent)}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // High-Speed Sportsbook-style Action Slip Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // HIGHER / BUY Button
                Button(
                    onClick = { onQuickOrder(ContractDirection.HIGHER) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_higher_${quote.symbol}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MarketGreen.copy(alpha = 0.15f),
                        contentColor = MarketGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = "Higher",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "HIGHER ↑",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // LOWER / SELL Button
                Button(
                    onClick = { onQuickOrder(ContractDirection.LOWER) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_lower_${quote.symbol}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MarketRed.copy(alpha = 0.15f),
                        contentColor = MarketRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketRed.copy(alpha = 0.4f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Lower",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOWER ↓",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer metadata: Provider & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Src: ${quote.providerName}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = "Updated: ${timeFormat.format(Date(quote.lastUpdated))}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

private fun formatPrice(symbol: String, price: Double): String {
    return when {
        symbol.contains("JPY") -> "¥${"%,.2f".format(price)}"
        symbol.contains("EUR") || symbol.contains("GBP") || symbol.contains("AUD") || symbol.contains("NZD") || symbol.contains("CAD") || symbol.contains("CHF") -> "%.4f".format(price)
        price >= 1000.0 -> "$${"%,.2f".format(price)}"
        else -> "$${"%,.2f".format(price)}"
    }
}

private fun formatSpread(symbol: String, spread: Double): String {
    return if (spread < 0.01) "%.5f".format(spread) else "%.2f".format(spread)
}
