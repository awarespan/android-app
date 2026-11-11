package com.awarespan.guardian.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanLoadingView
import com.awarespan.guardian.ui.components.AwarespanOfflineBanner
import com.awarespan.guardian.ui.components.AwarespanSectionHeader
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.components.IncidentCard
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.StatusCritical
import com.awarespan.guardian.ui.theme.StatusSafe
import com.awarespan.guardian.ui.theme.StatusWarning
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun DashboardScreen(
    onIncidentClick: (String) -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToNodes: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(16.dp)
                                        .background(EmberOrange, RectangleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "AWARESPAN",
                                    fontFamily = FontFamilyDisplay,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.sp,
                                    color = BoneWhite,
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "OPERATIONS CONSOLE // SECTOR TELEMETRY",
                                fontFamily = FontFamilyMono,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = SteelMid,
                            )
                        }

                        AwarespanButton(
                            text = "Refresh",
                            onClick = { viewModel.refresh() },
                            variant = ButtonVariant.OUTLINE,
                            loading = state.loading,
                        )
                    }
                }
            }

            // Offline warning banner
            if (state.isOffline) {
                item {
                    AwarespanOfflineBanner(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // Health Status Surface
            item {
                val healthColor = when (state.systemHealth) {
                    "GREEN" -> StatusSafe
                    "YELLOW" -> StatusWarning
                    "RED" -> StatusCritical
                    else -> SteelMid
                }

                AwarespanCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    borderColor = healthColor,
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NETWORK HEALTH STATE",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = SteelMid,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (state.systemHealth == "GREEN") "ALL SYSTEMS NOMINAL" else "ATTENTION REQUIRED",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = BoneWhite,
                            )
                        }

                        AwarespanBadge(
                            text = state.systemHealth,
                            color = healthColor,
                            fillAlpha = 0.2f
                        )
                    }
                }
            }

            // Metrics readout cards (Nodes Online & Active Threats)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Online Nodes Card
                    AwarespanCard(
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNodes,
                        contentPadding = 14.dp
                    ) {
                        Column {
                            Text(
                                text = "EDGE NODES",
                                fontFamily = FontFamilyMono,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp,
                                color = SteelMid,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${state.onlineNodes} / ${state.totalNodes}",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 24.sp,
                                color = BoneWhite,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "ONLINE // ACTIVE",
                                fontFamily = FontFamilyMono,
                                fontSize = 9.sp,
                                color = StatusSafe,
                            )
                        }
                    }

                    // Active Threats Card
                    AwarespanCard(
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFeed,
                        contentPadding = 14.dp
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE THREATS",
                                fontFamily = FontFamilyMono,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp,
                                color = SteelMid,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${state.activeThreats}",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 24.sp,
                                color = if (state.activeThreats > 0) EmberOrange else BoneWhite,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (state.activeThreats > 0) "REQUIRES ACTION" else "ZERO ACTIVE",
                                fontFamily = FontFamilyMono,
                                fontSize = 9.sp,
                                color = if (state.activeThreats > 0) EmberOrange else SteelMid,
                            )
                        }
                    }
                }
            }

            // Section Header: Recent Incidents
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    AwarespanSectionHeader(
                        title = "Recent Incidents",
                        counter = "01 // LOG"
                    )
                }
            }

            // Incidents List
            if (state.loading && state.recentIncidents.isEmpty()) {
                item {
                    AwarespanLoadingView()
                }
            } else if (state.recentIncidents.isEmpty()) {
                item {
                    AwarespanCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ZERO INCIDENTS RECORDED",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = SteelMid,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Sector sensors report no active safety alerts.",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 13.sp,
                                color = FogLight,
                            )
                        }
                    }
                }
            } else {
                items(state.recentIncidents, key = { it.id }) { incident ->
                    IncidentCard(
                        incident = incident,
                        onClick = { onIncidentClick(incident.id) }
                    )
                }
            }

            // Bottom Spacing
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
