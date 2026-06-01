package com.kalenyator.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kalenyator.app.R
import com.kalenyator.app.ui.calculator.CalculatorScreen
import com.kalenyator.app.ui.calendar.CalendarScreen
import com.kalenyator.app.ui.family.FamilyScreen
import com.kalenyator.app.ui.settings.SettingsScreen
import com.kalenyator.app.ui.weather.WeatherScreen
import com.kalenyator.app.ui.theme.HolidayIndependence
import com.kalenyator.app.ui.theme.HolidayNewYear
import com.kalenyator.app.ui.theme.TentakoGold
import com.kalenyator.app.ui.theme.TentakoPink

private data class NavItem(
    val route: NavRoute,
    val icon: ImageVector,
    val labelRes: Int,
    val tint: Color
)

sealed class NavRoute(val route: String) {
    data object Calculator : NavRoute("calculator")
    data object Calendar : NavRoute("calendar")
    data object Weather : NavRoute("weather")
    data object Family : NavRoute("family")
    data object Settings : NavRoute("settings")
}

@Composable
fun KalenyatorNav(
    onExport: () -> Unit,
    onExportNew: () -> Unit,
    onImport: () -> Unit,
    onScanQr: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        NavItem(NavRoute.Calculator, Icons.Default.Calculate, R.string.nav_calculator, TentakoGold),
        NavItem(NavRoute.Calendar, Icons.Default.CalendarMonth, R.string.nav_calendar, HolidayNewYear),
        NavItem(NavRoute.Weather, Icons.Default.Cloud, R.string.nav_weather, HolidayIndependence),
        NavItem(NavRoute.Family, Icons.Default.Favorite, R.string.nav_family, TentakoPink),
        NavItem(NavRoute.Settings, Icons.Default.Settings, R.string.nav_settings, Color(0xFF9B59B6))
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val current = backStack?.destination?.route
                items.forEach { item ->
                    val selected = current == item.route.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = stringResource(item.labelRes),
                                tint = if (selected) item.tint else item.tint.copy(alpha = 0.65f)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.labelRes),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = item.tint,
                            selectedTextColor = item.tint,
                            indicatorColor = item.tint.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Calculator.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoute.Calculator.route) { CalculatorScreen() }
            composable(NavRoute.Calendar.route) { CalendarScreen() }
            composable(NavRoute.Weather.route) { WeatherScreen() }
            composable(NavRoute.Family.route) { FamilyScreen() }
            composable(NavRoute.Settings.route) {
                SettingsScreen(
                    onExport = onExport,
                    onExportNew = onExportNew,
                    onImport = onImport,
                    onScanQr = onScanQr,
                    onCheckUpdate = onCheckUpdate
                )
            }
        }
    }
}
