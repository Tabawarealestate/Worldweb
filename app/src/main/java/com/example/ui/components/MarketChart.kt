package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.sin

@Composable
fun MarketChart(
    symbol: String,
    currentPrice: Double,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val timeframes = listOf("1m", "5m", "15m", "1H", "4H", "1D", "1W")
    var selectedTimeframe by remember { mutableStateOf("5m") }

    val lineColor = if (isPositive) MarketGreen else MarketRed
    val gradientColor = if (isPositive) MarketGreen.copy(alpha = 0.25f) else MarketRed.copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalDarkBg, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Timeframe selector bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeframes.forEach { tf ->
                val isSelected = tf == selectedTimeframe
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) ElectricCyan.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeframe = tf },
                        label = { Text(tf, fontSize = 11.sp, color = if (isSelected) ElectricCyan else TextMuted) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            selectedContainerColor = Color.Transparent
                        ),
                        border = null
                    )
                }
            }
        }

        // Price Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(top = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val width = size.width
                val height = size.height

                // Grid lines
                val gridColor = Color(0x1AFFFFFF)
                for (i in 1..3) {
                    val y = height * (i / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }

                // Deterministic smooth price line points based on symbol and timeframe
                val pointsCount = 30
                val points = mutableListOf<Offset>()
                val seed = symbol.hashCode() + selectedTimeframe.hashCode()

                for (i in 0 until pointsCount) {
                    val x = (width / (pointsCount - 1)) * i
                    val wave1 = sin(i * 0.4 + (seed % 10))
                    val wave2 = sin(i * 0.15 + (seed % 7)) * 0.5
                    val trend = if (isPositive) (i / pointsCount.toFloat()) * 0.3f else -(i / pointsCount.toFloat()) * 0.3f
                    val normalizedY = 0.5f - ((wave1 + wave2 + trend) * 0.22f).toFloat()
                    val y = (normalizedY.coerceIn(0.1f, 0.9f)) * height
                    points.add(Offset(x, y))
                }

                // Draw gradient area
                val fillPath = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientColor, Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw continuous price line
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cx = (prev.x + curr.x) / 2
                        cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Current live price pulse dot
                val lastPoint = points.last()
                drawCircle(
                    color = lineColor.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = lastPoint
                )
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = lastPoint
                )
            }
        }
    }
}
