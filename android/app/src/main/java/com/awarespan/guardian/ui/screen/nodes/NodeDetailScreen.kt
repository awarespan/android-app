package com.awarespan.guardian.ui.screen.nodes

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.domain.model.NodeStatus
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanErrorView
import com.awarespan.guardian.ui.components.AwarespanLoadingView
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.NodeDegraded
import com.awarespan.guardian.ui.theme.NodeOffline
import com.awarespan.guardian.ui.theme.NodeOnline
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NodeDetailScreen(
    nodeId: String,
    onBack: () -> Unit,
    onConfigurePolicy: (String) -> Unit,
    viewModel: NodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(nodeId) {
        viewModel.selectNode(nodeId)
    }

    val node = state.selectedNode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AwarespanButton(text = "← Back", onClick = onBack, variant = ButtonVariant.OUTLINE)
                Text(
                    text = "NODE TELEMETRY",
                    fontFamily = FontFamilyMono,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = SteelMid,
                )
            }

            if (node == null) {
                AwarespanLoadingView(message = "READING NODE REGISTRY…")
            } else {
                val statusColor = when (node.status) {
                    NodeStatus.ONLINE -> NodeOnline
                    NodeStatus.DEGRADED -> NodeDegraded
                    NodeStatus.OFFLINE -> NodeOffline
                    else -> SteelMid
                }

                // Node Identity Card
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = node.name.ifEmpty { node.nodeId },
                                fontFamily = FontFamilyDisplay,
                                fontSize = 20.sp,
                                color = BoneWhite,
                            )
                            AwarespanBadge(text = node.status.name, color = statusColor)
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "NODE ID: ${node.nodeId}",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            color = SteelMid,
                        )
                    }
                }

                // Operational Status & Heartbeat
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "TELEMETRY HEALTH",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = SteelMid,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LAST HEARTBEAT",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                color = FogLight,
                            )
                            Text(
                                text = formatFullTimestamp(node.lastHeartbeat),
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                color = BoneWhite,
                            )
                        }

                        if (node.batteryLevel != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BATTERY LEVEL",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    color = FogLight,
                                )
                                Text(
                                    text = "${node.batteryLevel}%",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    color = if (node.batteryLevel < 20) NodeOffline else NodeOnline,
                                )
                            }
                        }

                        if (node.firmwareVersion != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "FIRMWARE RUNTIME",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    color = FogLight,
                                )
                                Text(
                                    text = node.firmwareVersion,
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    color = BoneWhite,
                                )
                            }
                        }
                    }
                }

                // Coordinates & Location
                node.location?.let { loc ->
                    AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "NODE DEPLOYMENT COORDINATES",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = SteelMid,
                            )
                            Spacer(Modifier.height(8.dp))
                            if (loc.latitude != 0.0 || loc.longitude != 0.0) {
                                Text(
                                    text = "LAT / LNG: ${loc.latitude}, ${loc.longitude}",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 13.sp,
                                    color = BoneWhite,
                                )
                            } else {
                                Text(
                                    text = "COORDINATES: UNRECORDED / STATIC NODE",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    color = SteelMid,
                                )
                            }
                        }
                    }
                }

                // Autopilot Policy Quick Action
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "AUTONOMOUS INTERVENTION POLICY",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = SteelMid,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Configure edge-node autonomous deterrence rules, confidence thresholds, and automatic escalation pathways.",
                            fontFamily = FontFamilyDisplay,
                            fontSize = 12.sp,
                            color = FogLight,
                        )
                        Spacer(Modifier.height(14.dp))
                        AwarespanButton(
                            text = "Configure Node Autopilot",
                            onClick = { onConfigurePolicy(node.nodeId) },
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun formatFullTimestamp(instant: Instant?): String {
    if (instant == null) return "NEVER"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
