package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZorivoBlueHighlight
import com.example.ui.theme.ZorivoDeepBlack
import com.example.ui.theme.ZorivoElectricBlue
import com.example.ui.theme.ZorivoPrimaryBlue
import com.example.ui.theme.ZorivoSignalRed
import com.example.ui.theme.ZorivoSoftRed
import com.example.ui.theme.ZorivoTextMuted
import com.example.ui.theme.ZorivoTextPrimary

@Composable
fun CinematicLoadingBar(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isLoading) return

    val infiniteTransition = rememberInfiniteTransition(label = "cinematic_loading")
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.5.dp)
            .background(ZorivoDeepBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(2.5.dp)) {
            val width = size.width
            val startX = offsetProgress * width
            val beamWidth = width * 0.45f

            val brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    ZorivoPrimaryBlue.copy(alpha = 0.3f),
                    ZorivoElectricBlue,
                    ZorivoBlueHighlight,
                    ZorivoSignalRed,
                    Color.Transparent
                ),
                start = Offset(startX - beamWidth, 0f),
                end = Offset(startX + beamWidth, 0f)
            )

            drawRect(
                brush = brush,
                topLeft = Offset(0f, 0f),
                size = size
            )
        }
    }
}

@Composable
fun ZorivoCinematicSplash(
    isVisible: Boolean,
    onComplete: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZorivoDeepBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ZorivoSymbol(size = 72.dp, animated = true)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ZORIVO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = ZorivoTextPrimary,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SIMPLE MARKETS. ONE MOVE.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = ZorivoTextMuted
                )
            }
        }
    }
}
