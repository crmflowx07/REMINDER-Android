package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.util.DateTimeUtils

@Composable
fun CompletedScreen(vm: MainViewModel, onBack: () -> Unit) {
    val history by vm.completedHistory.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }

    FigmaPageScaffold(
        title = "Completed",
        onBack = onBack,
        actions = { if (history.isNotEmpty()) TextButton(onClick = { confirmClear = true }) { Text("Clear") } }
    ) { padding ->
        if (history.isEmpty()) {
            Box(Modifier.padding(padding).padding(horizontal = 20.dp, vertical = 10.dp)) {
                FigmaEmptyState("Nothing completed yet", "Finished reminders will appear here.", Icons.Rounded.TaskAlt)
            }
        } else {
            LazyColumn(
                Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { h ->
                    FigmaRowCard(
                        title = h.title,
                        subtitle = "Completed ${DateTimeUtils.formatDateTime(h.completedAt)}${h.categoryName?.let { " • $it" } ?: ""}",
                        icon = Icons.Rounded.CheckCircle,
                        tint = FigmaMint
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (confirmClear) AlertDialog(
        onDismissRequest = { confirmClear = false },
        title = { Text("Clear completed history?") },
        text = { Text("This removes the history list. Active reminders are not affected.") },
        confirmButton = { TextButton(onClick = { vm.clearHistory(); confirmClear = false }) { Text("Clear") } },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } }
    )
}
