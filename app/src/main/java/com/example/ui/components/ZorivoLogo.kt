package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZorivoBlueHighlight
import com.example.ui.theme.ZorivoBorder
import com.example.ui.theme.ZorivoDarkSurface
import com.example.ui.theme.ZorivoDeepBlack
import com.example.ui.theme.ZorivoElectricBlue
import com.example.ui.theme.ZorivoNearBlack
import com.example.ui.theme.ZorivoPrimaryBlue
import com.example.ui.theme.ZorivoSignalRed
import com.example.ui.theme.ZorivoSoftRed
import com.example.ui.theme.ZorivoTextMuted
import com.example.ui.theme.ZorivoTextPrimary

@Composable
fun ZorivoSymbol(
    size: Dp = 36.dp,
    animated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zorivo_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )

    Box(
        modifier = modifier
            .size(size)
            .background(ZorivoDarkSurface, RoundedCornerShape(size * 0.26f))
            .border(1.dp, ZorivoBorder, RoundedCornerShape(size * 0.26f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.18f)) {
            val w = this.size.width
            val h = this.size.height

            // Upper Directional Path (Left to Right to Diagonal Center)
            val upperPath = Path().apply {
                moveTo(w * 0.12f, h * 0.18f)
                lineTo(w * 0.88f, h * 0.18f)
                lineTo(w * 0.52f, h * 0.50f)
            }

            // Lower Directional Path (Diagonal Center to Bottom Left to Bottom Right)
            val lowerPath = Path().apply {
                moveTo(w * 0.48f, h * 0.50f)
                lineTo(w * 0.12f, h * 0.82f)
                lineTo(w * 0.88f, h * 0.82f)
            }

            val strokeWidth = w * 0.16f

            // Draw Upper Blue Path
            drawPath(
                path = upperPath,
                brush = Brush.linearGradient(
                    colors = listOf(ZorivoBlueHighlight, ZorivoPrimaryBlue),
                    start = Offset(0f, 0f),
                    end = Offset(w, h * 0.5f)
                ),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw Lower Blue Path with Red Micro-Accent node
            drawPath(
                path = lowerPath,
                brush = Brush.linearGradient(
                    colors = listOf(ZorivoPrimaryBlue, ZorivoElectricBlue),
                    start = Offset(0f, h * 0.5f),
                    end = Offset(w, h)
                ),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Red Micro-Accent (Energy Anchor Node at the bottom turn)
            val redDotRadius = strokeWidth * 0.65f
            drawCircle(
                color = ZorivoSignalRed,
                radius = redDotRadius,
                center = Offset(w * 0.86f, h * 0.82f)
            )

            // Directional Light Sheen when animated
            if (animated) {
                val sheenX = pulseProgress * w
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f * (1f - pulseProgress)),
                    radius = redDotRadius * 1.5f,
                    center = Offset(sheenX, h * 0.18f)
                )
            }
        }
    }
}

@Composable
fun ZorivoWordmark(
    modifier: Modifier = Modifier,
    showTagline: Boolean = true
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ZORIVO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = ZorivoTextPrimary,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(ZorivoSignalRed, RoundedCornerShape(2.dp))
            )
        }
        if (showTagline) {
            Text(
                text = "SIMPLE MARKETS. ONE MOVE.",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = ZorivoTextMuted
            )
        }
    }
}

@Composable
fun ZorivoBrandHeader(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    showTagline: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ZorivoSymbol(size = size, animated = true)
        Spacer(modifier = Modifier.width(10.dp))
        ZorivoWordmark(showTagline = showTagline)
    }
}
