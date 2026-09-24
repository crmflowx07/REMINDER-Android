package com.muzamil.reminder

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
                    LaunchedEffect(openedReminderId) { if (openedReminderId > 0) nav.navigate("add/$openedReminderId") }
                    val current by nav.currentBackStackEntryAsState()
                    val route = current?.destination?.route.orEmpty()
                    val showBottom = route in bottomDestinations.map { it.route }
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            if (showBottom) {
                                Surface(color = Color.White, shadowElevation = 18.dp) {
                                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                                        bottomDestinations.forEachIndexed { index, item ->
                                            NavigationBarItem(
                                                selected = route == item.route,
                                                onClick = { nav.goTop(item.route) },
                                                icon = item.icon,
                                                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = FigmaPurple,
                                                    selectedTextColor = FigmaPurple,
                                                    indicatorColor = FigmaPurpleSoft,
                                                    unselectedIconColor = FigmaSecondary,
                                                    unselectedTextColor = FigmaSecondary
                                                )
                                            )
                                            if (index == 1) Spacer(Modifier.width(54.dp))
                                        }
                                    }
                                }
                            }
                        },
                        floatingActionButtonPosition = FabPosition.Center,
                        floatingActionButton = {
                            if (showBottom) {
                                FloatingActionButton(
                                    onClick = { nav.navigate("add/0") },
                                    shape = CircleShape,
                                    containerColor = FigmaPurple,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Icon(Icons.Outlined.Add, "Add reminder", modifier = Modifier.size(28.dp))
                                }
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
