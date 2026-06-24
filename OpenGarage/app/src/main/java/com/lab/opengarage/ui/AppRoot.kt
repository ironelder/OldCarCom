package com.lab.opengarage.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lab.opengarage.ui.feed.FeedScreen
import com.lab.opengarage.ui.garage.CarDetailScreen
import com.lab.opengarage.ui.garage.CarEditScreen
import com.lab.opengarage.ui.garage.GarageScreen
import com.lab.opengarage.ui.login.LoginScreen
import com.lab.opengarage.ui.profile.ProfileScreen
import com.lab.opengarage.ui.record.RecordDetailScreen
import com.lab.opengarage.ui.record.RecordEditScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.FEED, "차종피드", Icons.Filled.Home),
    Tab(Routes.GARAGE, "내 차고", Icons.Filled.DirectionsCar),
    Tab(Routes.PROFILE, "프로필", Icons.Filled.Person),
)

@Composable
fun AppRoot(rootVm: RootViewModel = hiltViewModel()) {
    val user by rootVm.currentUser.collectAsStateWithLifecycle()
    if (user == null) {
        LoginScreen()
    } else {
        MainApp()
    }
}

@Composable
private fun MainApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar {
                    val dest = backStack?.destination
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = dest?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.FEED,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.FEED) {
                FeedScreen(onRecordClick = { nav.navigate(Routes.recordDetail(it)) })
            }
            composable(Routes.GARAGE) {
                GarageScreen(
                    onAddCar = { nav.navigate(Routes.carEdit()) },
                    onCarClick = { nav.navigate(Routes.carDetail(it)) },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onRecordClick = { nav.navigate(Routes.recordDetail(it)) })
            }
            composable(
                Routes.CAR_EDIT,
                arguments = listOf(navArgument("carId") { type = NavType.StringType; defaultValue = "" }),
            ) {
                CarEditScreen(onDone = { nav.popBackStack() })
            }
            composable(Routes.CAR_DETAIL) {
                CarDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEditCar = { carId -> nav.navigate(Routes.carEdit(carId)) },
                    onAddRecord = { carId -> nav.navigate(Routes.recordEdit(carId)) },
                    onRecordClick = { nav.navigate(Routes.recordDetail(it)) },
                )
            }
            composable(
                Routes.RECORD_EDIT,
                arguments = listOf(
                    navArgument("carId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("recordId") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { entry ->
                RecordEditScreen(
                    carId = entry.arguments?.getString("carId").orEmpty(),
                    onDone = { nav.popBackStack() },
                )
            }
            composable(Routes.RECORD_DETAIL) {
                RecordDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { recordId, carId -> nav.navigate(Routes.recordEdit(carId, recordId)) },
                )
            }
        }
    }
}
