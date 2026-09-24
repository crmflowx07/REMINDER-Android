package com.muzamil.reminder.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.muzamil.reminder.ui.screens.*

data class BottomDestination(val route: String, val label: String, val icon: @Composable () -> Unit)

val bottomDestinations = listOf(
    BottomDestination("home", "Home", { Icon(Icons.Rounded.Home, null) }),
    BottomDestination("planner", "Tasks", { Icon(Icons.Rounded.CalendarMonth, null) }),
    BottomDestination("routines", "Routines", { Icon(Icons.Rounded.MenuBook, null) }),
    BottomDestination("more", "Profile", { Icon(Icons.Rounded.PersonOutline, null) })
)

fun NavHostController.goTop(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun ReminderNavHost(nav: NavHostController, vm: MainViewModel) {
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(vm, onAdd = { nav.navigate("add/0") }, onEdit = { nav.navigate("add/$it") }, onSearch = { nav.navigate("search") }) }
        composable("planner") { PlannerScreen(vm, onEdit = { nav.navigate("add/$it") }, onCalendar = { nav.navigate("calendar") }) }
        composable("routines") { RoutinesScreen(vm) }
        composable("more") { MoreScreen(onOpen = { nav.navigate(it) }) }
        composable("add/{id}") { back -> AddEditReminderScreen(vm, back.arguments?.getString("id")?.toLongOrNull() ?: 0L, onDone = { nav.popBackStack() }) }
        composable("search") { SearchScreen(vm, onBack = { nav.popBackStack() }, onEdit = { nav.navigate("add/$it") }) }
        composable("calendar") { CalendarScreen(vm, onBack = { nav.popBackStack() }, onEdit = { nav.navigate("add/$it") }) }
        composable("categories") { CategoriesScreen(vm, onBack = { nav.popBackStack() }) }
        composable("profiles") { ProfilesScreen(vm, onBack = { nav.popBackStack() }) }
        composable("completed") { CompletedScreen(vm, onBack = { nav.popBackStack() }) }
        composable("sounds") { SoundsScreen(vm, onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        composable("ideas") { IdeasScreen(vm, onBack = { nav.popBackStack() }) }
        composable("plans") { PlansScreen(vm, onBack = { nav.popBackStack() }) }
        composable("goals") { GoalsScreen(vm, onBack = { nav.popBackStack() }) }
        composable("travel") { TravelScreen(vm, onBack = { nav.popBackStack() }) }
        composable("help") { HelpScreen(onBack = { nav.popBackStack() }) }
    }
}
