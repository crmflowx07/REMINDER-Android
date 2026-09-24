package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.data.ProfileEntity
import com.muzamil.reminder.ui.*

@Composable
fun ProfilesScreen(vm: MainViewModel, onBack: () -> Unit) {
    val profiles by vm.profiles.collectAsState()
    var add by remember { mutableStateOf(false) }

    FigmaPageScaffold(
        title = "Family Profiles",
        onBack = onBack,
        actions = {
            IconButton(onClick = { add = true }) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.PersonAdd, "Add profile", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FigmaCard(Modifier.fillMaxWidth()) {
                    Text(
                        "Optional profiles make family reminders easier. Core reminders still work without them.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (profiles.isEmpty()) item { FigmaEmptyState("No profiles", "Add family members or pets for shared-life reminders.", Icons.Rounded.People) }
            items(profiles, key = { it.id }) { p ->
                val icon = if (p.type == "PET") Icons.Rounded.Pets else Icons.Rounded.Person
                val tint = if (p.type == "ME") FigmaPurpleSoft else FigmaBlue
                FigmaRowCard(
                    title = p.name,
                    subtitle = p.type.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                    icon = icon,
                    tint = tint
                ) {
                    if (p.type != "ME") IconButton(onClick = { vm.deleteProfile(p) }) { Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (add) AddProfileDialog(onDismiss = { add = false }) { name, type -> vm.addProfile(ProfileEntity(name = name, type = type)); add = false }
}

@Composable
private fun AddProfileDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("WIFE") }
    var menu by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth()) { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                    DropdownMenu(menu, { menu = false }) {
                        listOf("WIFE", "HUSBAND", "CHILD", "PARENT", "PET", "OTHER").forEach { v ->
                            DropdownMenuItem(text = { Text(v.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { type = v; menu = false })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onSave(name.trim(), type) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
