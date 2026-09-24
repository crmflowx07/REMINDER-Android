package com.muzamil.reminder.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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


const val CategoryPurpleArgb: Long = 0xFFEDE8FFL
const val CategoryPinkArgb: Long = 0xFFFFE4F2L
const val CategoryBlueArgb: Long = 0xFFE8F5FFL
const val CategoryMintArgb: Long = 0xFFE5F7EFL
const val CategoryWarmArgb: Long = 0xFFFFF3DBL

fun categoryIcon(iconKey: String): ImageVector = when (iconKey.lowercase()) {
    "work", "briefcase" -> Icons.Rounded.Work
    "person", "personal" -> Icons.Rounded.Person
    "family_restroom", "family" -> Icons.Rounded.FamilyRestroom
    "groups", "meeting" -> Icons.Rounded.Groups
    "cake", "birthday" -> Icons.Rounded.Cake
    "payments", "payment" -> Icons.Rounded.Payments
    "description", "documents" -> Icons.Rounded.Description
    "flight", "travel" -> Icons.Rounded.Flight
    "fitness_center", "fitness" -> Icons.Rounded.FitnessCenter
    "restaurant", "food" -> Icons.Rounded.Restaurant
    "repeat", "routine" -> Icons.Rounded.Repeat
    "pets" -> Icons.Rounded.Pets
    "agriculture", "farm" -> Icons.Rounded.Agriculture
    "directions_car", "vehicle" -> Icons.Rounded.DirectionsCar
    "lightbulb", "ideas" -> Icons.Rounded.Lightbulb
    "flag", "goals" -> Icons.Rounded.Flag
    "event_note", "plans" -> Icons.Rounded.EventNote
    "school" -> Icons.Rounded.School
    "shopping_bag" -> Icons.Rounded.ShoppingBag
    else -> Icons.Rounded.Label
}

fun categoryTint(colorArgb: Long?, fallbackIndex: Int = 0): Color = when (colorArgb) {
    CategoryPurpleArgb -> FigmaPurpleSoft
    CategoryPinkArgb -> FigmaPink
    CategoryBlueArgb -> FigmaBlue
    CategoryMintArgb -> FigmaMint
    CategoryWarmArgb -> FigmaWarm
    else -> listOf(FigmaPurpleSoft, FigmaPink, FigmaBlue, FigmaMint, FigmaWarm)[((fallbackIndex % 5) + 5) % 5]
}

private val DarkPink = Color(0xFF482A3B)
private val DarkBlue = Color(0xFF233A4A)
private val DarkMint = Color(0xFF213D34)
private val DarkWarm = Color(0xFF493B24)

@Composable
private fun isFigmaDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.25f

@Composable
fun adaptiveFigmaTint(color: Color): Color {
    if (!isFigmaDark()) return color
    return when (color) {
        FigmaPurpleSoft -> MaterialTheme.colorScheme.primaryContainer
        FigmaPink -> DarkPink
        FigmaBlue -> DarkBlue
        FigmaMint -> DarkMint
        FigmaWarm -> DarkWarm
        else -> color
    }
}

@Composable
fun FigmaBackground(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val dark = isFigmaDark()
    Canvas(modifier.fillMaxSize()) {
        val a = if (dark) .055f else .045f
        drawCircle(primary.copy(alpha = a), radius = size.minDimension * .20f, center = Offset(size.width * .96f, size.height * .16f))
        drawCircle(primary.copy(alpha = a * .8f), radius = size.minDimension * .16f, center = Offset(size.width * .04f, size.height * .72f))
        drawCircle(primary.copy(alpha = a * .65f), radius = size.minDimension * .10f, center = Offset(size.width * .72f, size.height * .48f))
    }
}

@Composable
fun FigmaCard(
    modifier: Modifier = Modifier,
    radius: Dp = 15.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isFigmaDark()
    val shape = RoundedCornerShape(radius)
    Surface(
        modifier = modifier.then(
            if (dark) Modifier else Modifier.shadow(
                12.dp,
                shape,
                ambientColor = Color.Black.copy(alpha = .035f),
                spotColor = Color.Black.copy(alpha = .045f)
            )
        ),
        color = MaterialTheme.colorScheme.surface,
        shape = shape,
        tonalElevation = 0.dp,
        border = if (dark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .42f)) else null
    ) {
        Column(content = content)
    }
}

@Composable
fun FigmaActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (accent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f),
        border = if (isFigmaDark()) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .35f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription,
                tint = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FigmaTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                        }
                    }
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FigmaPageScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FigmaBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { FigmaTopBar(title = title, onBack = onBack, actions = actions) },
            floatingActionButton = floatingActionButton,
            content = content
        )
    }
}

@Composable
fun FigmaRowCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    tint: Color = FigmaPurpleSoft,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    FigmaCard(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorIconTile(tint, Modifier.size(40.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            trailing()
        }
    }
}

@Composable
fun FigmaEmptyState(title: String, subtitle: String, icon: ImageVector, action: (@Composable () -> Unit)? = null) {
    FigmaCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ColorIconTile(FigmaPurpleSoft, Modifier.size(56.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            if (action != null) {
                Spacer(Modifier.height(16.dp))
                action()
            }
        }
    }
}

@Composable
fun FigmaSectionHeader(title: String, count: Int? = null, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FigmaFormSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FigmaCard(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorIconTile(FigmaPurpleSoft, Modifier.size(36.dp)) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            content()
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
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(adaptiveFigmaTint(color)),
        contentAlignment = Alignment.Center,
        content = content
    )
}
