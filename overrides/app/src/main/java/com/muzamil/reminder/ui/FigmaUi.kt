package com.muzamil.reminder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val FigmaPurple = Color(0xFF5F33E1)
val FigmaPurpleSoft = Color(0xFFEDE8FF)
val FigmaInk = Color(0xFF24252C)
val FigmaSecondary = Color(0xFF6E6A7C)
val FigmaCanvas = Color(0xFFFBFAFF)
val FigmaPink = Color(0xFFFFE4F2)
val FigmaBlue = Color(0xFFE8F5FF)
val FigmaMint = Color(0xFFE5F7EF)
val FigmaWarm = Color(0xFFFFF3DB)

@Composable
fun FigmaBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawCircle(FigmaPurple.copy(alpha = .055f), radius = size.minDimension * .20f, center = Offset(size.width * .96f, size.height * .16f))
        drawCircle(FigmaPurple.copy(alpha = .045f), radius = size.minDimension * .16f, center = Offset(size.width * .04f, size.height * .72f))
        drawCircle(FigmaPurple.copy(alpha = .035f), radius = size.minDimension * .10f, center = Offset(size.width * .72f, size.height * .48f))
    }
}

@Composable
fun FigmaCard(
    modifier: Modifier = Modifier,
    radius: Dp = 15.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.shadow(12.dp, RoundedCornerShape(radius), ambientColor = Color.Black.copy(alpha = .04f), spotColor = Color.Black.copy(alpha = .04f)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(radius),
        tonalElevation = 0.dp
    ) {
        Column(content = content)
    }
}

@Composable
fun FigmaSectionHeader(title: String, count: Int? = null, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = FigmaInk, fontWeight = FontWeight.SemiBold)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Surface(color = FigmaPurpleSoft, shape = CircleShape) {
                Text(count.toString(), modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = FigmaPurple, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FigmaProgressRing(value: Float, centerText: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            drawArc(
                color = Color.White.copy(alpha = .25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * value.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text(centerText, color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ColorIconTile(color: Color, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(9.dp)).background(color),
        contentAlignment = Alignment.Center,
        content = content
    )
}
