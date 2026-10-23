package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HabitGreenContainer
import com.example.ui.theme.HabitGreenPrimary
import com.example.ui.theme.HabitTextPrimary
import com.example.ui.theme.HabitTextSecondary

@Composable
fun CircularProgressGauge(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 9.dp,
    trackColor: Color = HabitGreenContainer,
    progressColor: Color = HabitGreenPrimary
) {
    val progress = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "gauge_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokePx
            val topLeftOffset = strokePx / 2

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$completed/$total",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HabitTextPrimary
            )
            Text(
                text = "habits done",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = HabitTextSecondary
            )
        }
    }
}

@Composable
fun DonutRateChart(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    strokeWidth: Dp = 12.dp,
    trackColor: Color = Color(0xFFE2EBE4),
    progressColor: Color = HabitGreenPrimary
) {
    val progress = (percentage.toFloat() / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "donut_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokePx
            val topLeftOffset = strokePx / 2

            // Track (representing missed)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )

            // Completed arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HabitTextPrimary
            )
            Text(
                text = "Overall",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = HabitTextSecondary
            )
        }
    }
}
