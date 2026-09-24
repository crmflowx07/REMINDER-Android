package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.ui.*

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        Pair("Task Management &\nTo-Do List", "This productive tool is designed to help you better manage your reminders and plans conveniently!"),
        Pair("Plan every part\nof your day", "Keep work, family, payments, routines and important dates together in one calm place."),
        Pair("Never miss what\nmatters", "Reliable offline reminders, repeat schedules, snooze and completion actions — ready when you are.")
    )

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FigmaBackground()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(54.dp))
            HeroIllustration(page)
            Spacer(Modifier.weight(1f))
            Text(
                pages[page].first,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                pages[page].second,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp)
            )
            Spacer(Modifier.height(34.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                pages.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == page) 24.dp else 7.dp, 7.dp)
                            .clip(CircleShape)
                            .background(if (i == page) FigmaPurple else FigmaPurple.copy(alpha = .18f))
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (page < pages.lastIndex) page++ else onFinish() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaPurple)
            ) {
                Text(if (page == pages.lastIndex) "Let’s Start" else "Continue", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
private fun HeroIllustration(page: Int) {
    Box(Modifier.fillMaxWidth().height(370.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.offset(x = 128.dp, y = (-94).dp).size(64.dp).clip(CircleShape).background(FigmaPurple.copy(alpha = .08f)))
        Box(Modifier.offset(x = (-135).dp, y = 30.dp).size(72.dp).clip(CircleShape).background(FigmaPurple.copy(alpha = .07f)))
        Box(Modifier.offset(x = 92.dp, y = 112.dp).size(54.dp).clip(CircleShape).background(FigmaPurple.copy(alpha = .06f)))

        Surface(
            modifier = Modifier.size(214.dp),
            shape = RoundedCornerShape(54.dp),
            color = FigmaPurpleSoft,
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (page) {
                        0 -> Icons.Outlined.TaskAlt
                        1 -> Icons.Outlined.CalendarMonth
                        else -> Icons.Outlined.NotificationsActive
                    },
                    contentDescription = null,
                    tint = FigmaPurple,
                    modifier = Modifier.size(92.dp)
                )
            }
        }
        ColorIconTile(FigmaPink, Modifier.offset(x = (-115).dp, y = (-90).dp).size(48.dp)) {
            Icon(Icons.Outlined.Timer, null, tint = Color(0xFFE66398), modifier = Modifier.size(26.dp))
        }
        ColorIconTile(FigmaBlue, Modifier.offset(x = 122.dp, y = 2.dp).size(50.dp)) {
            Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF4C8EDB), modifier = Modifier.size(27.dp))
        }
        ColorIconTile(FigmaWarm, Modifier.offset(x = (-120).dp, y = 100.dp).size(44.dp)) {
            Icon(Icons.Outlined.EventNote, null, tint = Color(0xFFE69A27), modifier = Modifier.size(23.dp))
        }
    }
}
