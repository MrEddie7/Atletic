package com.eddie.atletic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eddie.atletic.ui.AthletesViewModel
import com.eddie.atletic.ui.screens.AthleteDetailScreen
import com.eddie.atletic.ui.screens.AthleteFormScreen
import com.eddie.atletic.ui.screens.HomeScreen
import com.eddie.atletic.ui.screens.RenewalsScreen
import com.eddie.atletic.ui.theme.AtleticTheme
import com.eddie.atletic.ui.theme.BluePrimary
import com.eddie.atletic.ui.theme.GoldAccent
import com.eddie.atletic.ui.theme.NavyCard
import com.eddie.atletic.ui.theme.RedExpired

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtleticTheme {
                AtleticApp()
            }
        }
    }
}

@Composable
fun AtleticApp(
    viewModel: AthletesViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsState()

    val pendingRenewalsCount = uiState.expiringRenewalsCount + uiState.expiredRenewalsCount

    val showBottomBar = currentRoute in listOf("home", "renewals")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NavyCard,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.SportsMartialArts,
                                contentDescription = "Atletas",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Atletas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldAccent,
                            indicatorColor = GoldAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == "renewals",
                        onClick = {
                            navController.navigate("renewals") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingRenewalsCount > 0) {
                                        Badge(containerColor = RedExpired) {
                                            Text(
                                                text = pendingRenewalsCount.toString(),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentLate,
                                    contentDescription = "Anuidades",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = { Text("Anuidades") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldAccent,
                            indicatorColor = GoldAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { navController.navigate("create") },
                    onNavigateToEdit = { athleteId -> navController.navigate("edit/$athleteId") },
                    onNavigateToDetail = { athleteId -> navController.navigate("detail/$athleteId") }
                )
            }

            composable("create") {
                AthleteFormScreen(
                    athleteId = null,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "edit/{athleteId}",
                arguments = listOf(navArgument("athleteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val athleteId = backStackEntry.arguments?.getString("athleteId")
                AthleteFormScreen(
                    athleteId = athleteId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "detail/{athleteId}",
                arguments = listOf(navArgument("athleteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val athleteId = backStackEntry.arguments?.getString("athleteId") ?: ""
                AthleteDetailScreen(
                    athleteId = athleteId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate("edit/$id") }
                )
            }

            composable("renewals") {
                RenewalsScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { athleteId -> navController.navigate("detail/$athleteId") }
                )
            }
        }
    }
}
