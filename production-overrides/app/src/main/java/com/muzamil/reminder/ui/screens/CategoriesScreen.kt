package com.muzamil.reminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.muzamil.reminder.ui.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(vm: MainViewModel, onBack: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var add by remember { mutableStateOf(false) }

    FigmaPageScaffold(
        title = "Categories",
        onBack = onBack,
        actions = {
            FigmaActionIconButton(Icons.Rounded.Add, "Add category", { add = true }, accent = true)
            Spacer(Modifier.width(8.dp))
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (categories.isEmpty()) {
                item { FigmaEmptyState("No categories", "Create a category to keep reminders organized.", Icons.Rounded.Category) }
            }
            items(categories, key = { it.id }) { c ->
                FigmaRowCard(
                    title = c.name,
                    subtitle = if (c.isDefault) "Default category" else "Custom category",
                    icon = categoryIcon(c.icon),
                    tint = categoryTint(c.colorArgb, c.id.toInt())
                ) {
                    if (!c.isDefault) {
                        IconButton(onClick = { vm.deleteCategory(c) }) {
                            Icon(Icons.Rounded.DeleteOutline, "Delete ${c.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (add) CategoryDialog(onDismiss = { add = false }) { name, icon, color ->
        vm.addCategory(name, icon, color)
        add = false
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryDialog(onDismiss: () -> Unit, onSave: (String, String, Long) -> Unit) {
    var text by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("label") }
    var color by remember { mutableLongStateOf(CategoryPurpleArgb) }
    val icons = listOf(
        "label" to Icons.Rounded.Label,
        "work" to Icons.Rounded.Work,
        "person" to Icons.Rounded.Person,
        "family_restroom" to Icons.Rounded.FamilyRestroom,
        "groups" to Icons.Rounded.Groups,
        "cake" to Icons.Rounded.Cake,
        "payments" to Icons.Rounded.Payments,
        "description" to Icons.Rounded.Description,
        "flight" to Icons.Rounded.Flight,
        "fitness_center" to Icons.Rounded.FitnessCenter,
        "restaurant" to Icons.Rounded.Restaurant,
        "repeat" to Icons.Rounded.Repeat,
        "pets" to Icons.Rounded.Pets,
        "directions_car" to Icons.Rounded.DirectionsCar,
        "lightbulb" to Icons.Rounded.Lightbulb,
        "flag" to Icons.Rounded.Flag,
        "event_note" to Icons.Rounded.EventNote,
        "shopping_bag" to Icons.Rounded.ShoppingBag
    )
    val colors = listOf(
        CategoryPurpleArgb to FigmaPurpleSoft,
        CategoryPinkArgb to FigmaPink,
        CategoryBlueArgb to FigmaBlue,
        CategoryMintArgb to FigmaMint,
        CategoryWarmArgb to FigmaWarm
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    text,
                    { text = it.take(50) },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(categoryIcon(icon), null) }
                )
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.forEach { (key, image) ->
                        Surface(
                            onClick = { icon = key },
                            shape = MaterialTheme.shapes.small,
                            color = if (icon == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (icon == key) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .55f)) else null
                        ) {
                            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                Icon(image, key, tint = if (icon == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEach { (value, tint) ->
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(adaptiveFigmaTint(tint))
                                .clickable { color = value },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == value) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSave(text.trim(), icon, color) }) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
