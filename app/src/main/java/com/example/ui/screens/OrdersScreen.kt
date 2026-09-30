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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContractDirection
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.TradeOutcome
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
import com.example.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    openOrders: List<Order>,
    settledOrders: List<Order>,
    onSettleOrder: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedOutcomeFilter by remember { mutableStateOf<TradeOutcome?>(null) }
    val timeFormat = SimpleDateFormat("HH:mm:ss dd MMM", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(horizontal = 16.dp)
            .testTag("orders_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FINANCIAL CONTRACTS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TerminalSurfaceVariant
            ) {
                Text(
                    text = "${openOrders.size} Active Positions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: Active Contracts vs Settled History
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TerminalSurface,
            contentColor = ElectricCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ElectricCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "OPEN CONTRACTS (${openOrders.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "SETTLED HISTORY (${settledOrders.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Outcome Filters Horizontal Scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedOutcomeFilter == null,
                onClick = { selectedOutcomeFilter = null },
                label = { Text("All", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricCyan,
                    selectedLabelColor = TerminalDarkBg,
                    containerColor = TerminalSurfaceVariant,
                    labelColor = TextMuted
                )
            )

            listOf(
                TradeOutcome.WON to MarketGreen,
                TradeOutcome.LOSS to MarketRed,
                TradeOutcome.CASHED_OUT to BrightGold,
                TradeOutcome.ACTIVE to ElectricCyan,
                TradeOutcome.VOID to TextMuted,
                TradeOutcome.EXPIRED to WarningOrange
            ).forEach { (outcome, _) ->
                FilterChip(
                    selected = selectedOutcomeFilter == outcome,
                    onClick = {
                        selectedOutcomeFilter = if (selectedOutcomeFilter == outcome) null else outcome
                    },
                    label = { Text(outcome.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = TerminalDarkBg,
                        containerColor = TerminalSurfaceVariant,
                        labelColor = TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val baseList = if (selectedTab == 0) openOrders else settledOrders
        val filteredList = baseList.filter { order ->
            if (selectedOutcomeFilter == null) true
            else {
                val outcome = deriveTradeOutcome(order)
                outcome == selectedOutcomeFilter
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.HourglassBottom,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedTab == 0) "No active financial contracts" else "No matching settled orders",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { order ->
                    ComprehensiveOrderCard(
                        order = order,
                        timeFormat = timeFormat,
                        onSettle = { onSettleOrder(order.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

private fun deriveTradeOutcome(order: Order): TradeOutcome {
    return when (order.status) {
        OrderStatus.OPEN -> TradeOutcome.ACTIVE
        OrderStatus.PENDING -> TradeOutcome.PENDING_SETTLEMENT
        OrderStatus.PARTIALLY_FILLED -> TradeOutcome.PARTIALLY_CASHED_OUT
        OrderStatus.FILLED -> TradeOutcome.ACTIVE
        OrderStatus.CANCELLED -> TradeOutcome.CANCELLED
        OrderStatus.EXPIRED -> TradeOutcome.EXPIRED
        OrderStatus.REJECTED -> TradeOutcome.VOID
        OrderStatus.SETTLED -> if (order.pnl > 0) TradeOutcome.WON else TradeOutcome.LOSS
    }
}

@Composable
private fun ComprehensiveOrderCard(
    order: Order,
    timeFormat: SimpleDateFormat,
    onSettle: () -> Unit
) {
    val isHigher = order.direction == ContractDirection.HIGHER
    val isSettled = order.status == OrderStatus.SETTLED
    val outcome = deriveTradeOutcome(order)

    val (badgeText, badgeColor, badgeBg) = when (outcome) {
        TradeOutcome.WON -> Triple("WON", MarketGreen, MarketGreen.copy(alpha = 0.15f))
        TradeOutcome.LOSS -> Triple("LOSS", MarketRed, MarketRed.copy(alpha = 0.15f))
        TradeOutcome.ACTIVE -> Triple("ACTIVE", ElectricCyan, ElectricCyan.copy(alpha = 0.15f))
        TradeOutcome.CASHED_OUT -> Triple("CASHED OUT", BrightGold, BrightGold.copy(alpha = 0.15f))
        TradeOutcome.PARTIALLY_CASHED_OUT -> Triple("PARTIAL EXIT", BrightGold, BrightGold.copy(alpha = 0.15f))
        TradeOutcome.VOID -> Triple("VOID", TextMuted, TextMuted.copy(alpha = 0.15f))
        TradeOutcome.EXPIRED -> Triple("EXPIRED", WarningOrange, WarningOrange.copy(alpha = 0.15f))
        TradeOutcome.CANCELLED -> Triple("CANCELLED", TextMuted, TextMuted.copy(alpha = 0.15f))
        TradeOutcome.PENDING_SETTLEMENT -> Triple("PENDING", BrightGold, BrightGold.copy(alpha = 0.15f))
        TradeOutcome.SETTLED -> Triple("SETTLED", ElectricCyan, ElectricCyan.copy(alpha = 0.15f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_item_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        color = TerminalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Symbol, Direction Pill & Outcome Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isHigher) MarketGreen.copy(alpha = 0.15f) else MarketRed.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.direction.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHigher) MarketGreen else MarketRed
                        )
                    }
                }

                // Outcome Badge
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order Financial Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Stake / Hold", fontSize = 10.sp, color = TextMuted)
                    Text(
                        "₦${"%,.0f".format(order.stake)}",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text("Strike Price", fontSize = 10.sp, color = TextMuted)
                    Text(
                        "$${"%,.2f".format(order.strikePrice)}",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isSettled) "Realized P&L" else "Potential Payout", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = if (isSettled) "${if (order.pnl > 0) "+" else ""}₦${"%,.0f".format(order.pnl)}" else "₦${"%,.0f".format(order.potentialPayout)}",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSettled) (if (order.pnl > 0) MarketGreen else MarketRed) else MarketGreen
                    )
                }
            }

            if (isSettled && order.settlementPrice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalDarkBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Settlement: $${"%,.2f".format(order.settlementPrice)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Text(order.auditLog.ifEmpty { "Standard Expiry" }, fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timestamps and Settle Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opened: ${timeFormat.format(Date(order.createdAt))}",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                if (order.status == OrderStatus.OPEN) {
                    Button(
                        onClick = onSettle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalSurfaceVariant,
                            contentColor = ElectricCyan
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp).testTag("btn_settle_${order.id}")
                    ) {
                        Text("SETTLE CONTRACT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
