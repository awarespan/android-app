package com.awarespan.guardian.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.awarespan.guardian.ui.screen.auth.LoginScreen
import com.awarespan.guardian.ui.screen.autopilot.AutopilotScreen
import com.awarespan.guardian.ui.screen.dashboard.DashboardScreen
import com.awarespan.guardian.ui.screen.events.EventHistoryScreen
import com.awarespan.guardian.ui.screen.incidents.IncidentDetailScreen
import com.awarespan.guardian.ui.screen.incidents.IncidentFeedScreen
import com.awarespan.guardian.ui.screen.map.MapScreen
import com.awarespan.guardian.ui.screen.nodes.NodeDetailScreen
import com.awarespan.guardian.ui.screen.nodes.NodeListScreen
import com.awarespan.guardian.ui.screen.settings.SettingsScreen
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

private data class NavItem(
    val destination: AwarespanNavDestination,
    val label: String,
    val symbol: String,
)

private val topLevelNavItems = listOf(
    NavItem(AwarespanNavDestination.Dashboard, "DASH", "⌂"),
    NavItem(AwarespanNavDestination.IncidentFeed, "FEED", "!"),
    NavItem(AwarespanNavDestination.NodeList, "NODES", "⬚"),
    NavItem(AwarespanNavDestination.Map, "MAP", "⊕"),
    NavItem(AwarespanNavDestination.EventHistory, "LOG", "≡"),
    NavItem(AwarespanNavDestination.Settings, "CONFIG", "⚙"),
)

@Composable
fun SipNavGraph(
    navController: NavHostController,
    startDestination: String,
    onAuthenticated: () -> Unit = {},
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isTopLevel = topLevelNavItems.any { it.destination.route == currentRoute }

    Scaffold(
        containerColor = VoidBlack,
        bottomBar = {
            if (isTopLevel) {
                // Architectural sharp 0dp navigation bar extending under gesture handle
                Surface(
                    color = Carbon,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GraphiteBorder, RectangleShape)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        topLevelNavItems.forEach { item ->
                            val selected = currentRoute == item.destination.route
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (!selected) {
                                            navController.navigate(item.destination.route) {
                                                launchSingleTop = true
                                                restoreState = true
                                                popUpTo(AwarespanNavDestination.Dashboard.route) {
                                                    saveState = true
                                                }
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                // 2px Ember Orange accent mark when active
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(2.dp)
                                        .background(
                                            if (selected) EmberOrange else Carbon,
                                            RectangleShape
                                        )
                                )
                                Text(
                                    text = item.symbol,
                                    fontSize = 14.sp,
                                    color = if (selected) EmberOrange else SteelMid
                                )
                                Text(
                                    text = item.label,
                                    fontFamily = FontFamilyMono,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (selected) BoneWhite else SteelMid
                                )
                            }
                        }
                    }
                }
            }
        }
    }
) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Authentication
            composable(AwarespanNavDestination.Login.route) {
                LoginScreen(onLoggedIn = {
                    navController.navigate(AwarespanNavDestination.Dashboard.route) {
                        popUpTo(AwarespanNavDestination.Login.route) { inclusive = true }
                    }
                    onAuthenticated()
                })
            }

            // Dashboard
            composable(AwarespanNavDestination.Dashboard.route) {
                DashboardScreen(
                    onIncidentClick = { id ->
                        navController.navigate(AwarespanNavDestination.IncidentDetail.createRoute(id))
                    },
                    onNavigateToFeed = {
                        navController.navigate(AwarespanNavDestination.IncidentFeed.route)
                    },
                    onNavigateToNodes = {
                        navController.navigate(AwarespanNavDestination.NodeList.route)
                    }
                )
            }

            // Incident Feed
            composable(AwarespanNavDestination.IncidentFeed.route) {
                IncidentFeedScreen(onIncidentClick = { id ->
                    navController.navigate(AwarespanNavDestination.IncidentDetail.createRoute(id))
                })
            }

            // Incident Details
            composable(
                route = AwarespanNavDestination.IncidentDetail.route,
                arguments = listOf(navArgument(AwarespanNavDestination.IncidentDetail.ARG_ID) {
                    type = NavType.StringType
                })
            ) {
                IncidentDetailScreen(onBack = { navController.popBackStack() })
            }

            // Node Management
            composable(AwarespanNavDestination.NodeList.route) {
                NodeListScreen(onNodeClick = { nodeId ->
                    navController.navigate(AwarespanNavDestination.NodeDetail.createRoute(nodeId))
                })
            }

            // Node Detail
            composable(
                route = AwarespanNavDestination.NodeDetail.route,
                arguments = listOf(navArgument(AwarespanNavDestination.NodeDetail.ARG_NODE_ID) {
                    type = NavType.StringType
                })
            ) { backStack ->
                val nodeId = backStack.arguments?.getString(AwarespanNavDestination.NodeDetail.ARG_NODE_ID) ?: ""
                NodeDetailScreen(
                    nodeId = nodeId,
                    onBack = { navController.popBackStack() },
                    onConfigurePolicy = { id ->
                        navController.navigate(AwarespanNavDestination.Autopilot.createRoute(id))
                    }
                )
            }

            // Map and Coordinates
            composable(AwarespanNavDestination.Map.route) {
                MapScreen(
                    onIncidentClick = { id ->
                        navController.navigate(AwarespanNavDestination.IncidentDetail.createRoute(id))
                    },
                    onNodeClick = { nodeId ->
                        navController.navigate(AwarespanNavDestination.NodeDetail.createRoute(nodeId))
                    }
                )
            }

            // Autopilot Policy Controls
            composable(
                route = AwarespanNavDestination.Autopilot.route,
                arguments = listOf(navArgument("nodeId") {
                    type = NavType.StringType
                    defaultValue = "global"
                })
            ) {
                AutopilotScreen(onBack = { navController.popBackStack() })
            }

            // Event History & Audit Log
            composable(AwarespanNavDestination.EventHistory.route) {
                EventHistoryScreen(onBack = { navController.popBackStack() })
            }

            // Settings & Operator Management
            composable(AwarespanNavDestination.Settings.route) {
                SettingsScreen(onLoggedOut = {
                    navController.navigate(AwarespanNavDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}
