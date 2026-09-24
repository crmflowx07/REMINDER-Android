package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.ui.*

@Composable
fun MoreScreen(onOpen: (String) -> Unit) {
    val items = listOf(
        MoreItem("Categories", "Create and organize reminder categories", Icons.Rounded.Category, "categories", FigmaPink),
        MoreItem("Family Profiles", "Me, spouse, children, parents, pets and others", Icons.Rounded.People, "profiles", FigmaBlue),
        MoreItem("Ideas", "Capture ideas before they disappear", Icons.Rounded.Lightbulb, "ideas", FigmaWarm),
        MoreItem("Plans", "Simple future plans with target dates and progress", Icons.Rounded.EventNote, "plans", FigmaPurpleSoft),
        MoreItem("Goals", "Track simple goals without project-management clutter", Icons.Rounded.Flag, "goals", FigmaMint),
        MoreItem("Travel", "Trips, places and travel preparation", Icons.Rounded.Flight, "travel", FigmaBlue),
        MoreItem("Completed", "Completed reminder history", Icons.Rounded.TaskAlt, "completed", FigmaMint),
        MoreItem("Sounds", "Built-in tones and My Sounds", Icons.Rounded.MusicNote, "sounds", FigmaPink),
        MoreItem("Settings", "Theme, snooze, vibration, export and reliability", Icons.Rounded.Settings, "settings", FigmaPurpleSoft),
        MoreItem("Help", "Notification and battery reliability guidance", Icons.Rounded.HelpOutline, "help", FigmaWarm)
    )

    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        FigmaBackground()
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FigmaSectionHeader("More")
                Spacer(Modifier.height(4.dp))
                Text("Everything else, without cluttering your day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
            }
            items(items.size) { i ->
                val item = items[i]
                FigmaRowCard(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    tint = item.tint,
                    onClick = { onOpen(item.route) }
                ) {
                    IconButton(onClick = { onOpen(item.route) }) {
                        Icon(Icons.Rounded.ChevronRight, "Open ${item.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private data class MoreItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val tint: Color
)
