package com.muzamil.reminder

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.muzamil.reminder.ui.*
import com.muzamil.reminder.ui.screens.OnboardingScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val openedReminderId = intent.getLongExtra("reminderId", -1L)
        setContent {
            val settings by vm.settings.collectAsState()
            val scope = rememberCoroutineScope()
            ReminderTheme(settings.theme) {
                val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
                if (!settings.onboardingDone) {
                    OnboardingScreen(onFinish = {
                        scope.launch { vm.settingsStore.setOnboardingDone(true) }
                        if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    })
                } else {
                    val nav = rememberNavController()
                    LaunchedEffect(openedReminderId) {
                        if (openedReminderId > 0) nav.navigate("add/$openedReminderId")
                    }
                    val current by nav.currentBackStackEntryAsState()
                    val route = current?.destination?.route.orEmpty()
                    val showBottom = route in bottomDestinations.map { it.route }

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            if (showBottom) {
                                FigmaBottomBar(
                                    currentRoute = route,
                                    onNavigate = { nav.goTop(it) },
                                    onAdd = { nav.navigate("add/0") }
                                )
                            }
                        }
                    ) { padding ->
                        Box(Modifier.padding(padding)) { ReminderNavHost(nav, vm) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FigmaBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 18.dp,
        tonalElevation = 0.dp
    ) {
        Box(Modifier.fillMaxWidth()) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                bottomDestinations.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        icon = item.icon,
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    if (index == 1) Spacer(Modifier.width(58.dp))
                }
            }

            FloatingActionButton(
                onClick = onAdd,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 10.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .size(58.dp)
            ) {
                Icon(Icons.Rounded.Add, "Add reminder", modifier = Modifier.size(27.dp))
            }
        }
    }
}
