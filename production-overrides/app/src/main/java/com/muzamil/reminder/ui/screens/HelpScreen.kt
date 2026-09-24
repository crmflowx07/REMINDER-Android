package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.ui.*

@Composable
fun HelpScreen(onBack: () -> Unit) {
    FigmaPageScaffold(title = "Help & reliability", onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Keep reminders reliable", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Text("These Android settings matter most for on-time alerts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HelpCard(Icons.Rounded.Notifications, "1. Notifications", "Android 13+ requires notification permission. You can manage it from Settings → Notifications.", FigmaPurpleSoft)
            HelpCard(Icons.Rounded.Alarm, "2. Exact alarms", "Some Android versions require special exact-alarm access for precise times. REMINDER falls back to an allowed alarm method when exact access is unavailable.", FigmaBlue)
            HelpCard(Icons.Rounded.BatterySaver, "3. Battery saver", "Aggressive device battery settings can delay alarms. If that happens, review your phone's battery optimization settings for REMINDER.", FigmaWarm)
            HelpCard(Icons.Rounded.RestartAlt, "4. Phone restart", "REMINDER listens for reboot, timezone and clock changes and reschedules pending local alarms.", FigmaPink)
            HelpCard(Icons.Rounded.CloudOff, "5. Offline-first", "Creating, editing, completing, snoozing and scheduling reminders works without an internet connection.", FigmaMint)
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun HelpCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String, tint: androidx.compose.ui.graphics.Color) {
    FigmaRowCard(title = title, subtitle = text, icon = icon, tint = tint)
}
