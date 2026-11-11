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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.domain.model.Node
import com.awarespan.guardian.domain.model.NodeStatus
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanEmptyView
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
fun NodeListScreen(
    onNodeClick: (String) -> Unit,
    viewModel: NodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EDGE NODES",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = BoneWhite,
                    )
                    Text(
                        text = "RASPBERRY PI HARDWARE & INFERENCE MESH",
                        fontFamily = FontFamilyMono,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = SteelMid,
                    )
                }

                AwarespanButton(
                    text = "Scan",
                    onClick = { viewModel.refresh() },
                    variant = ButtonVariant.OUTLINE,
                    loading = state.loading,
                )
            }

            when {
                state.loading && state.nodes.isEmpty() -> {
                    AwarespanLoadingView(message = "SCANNING REGISTERED NODES…")
                }
                state.error != null && state.nodes.isEmpty() -> {
                    AwarespanErrorView(
                        error = state.error ?: "Failed to query nodes",
                        onRetry = { viewModel.refresh() }
                    )
                }
                state.nodes.isEmpty() -> {
                    AwarespanEmptyView(
                        title = "ZERO REGISTERED NODES",
                        message = "No edge camera/inference nodes reported in this sector."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.nodes, key = { it.nodeId }) { node ->
                            NodeCard(node = node, onClick = { onNodeClick(node.nodeId) })
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeCard(node: Node, onClick: () -> Unit) {
    val statusColor = when (node.status) {
        NodeStatus.ONLINE -> NodeOnline
        NodeStatus.DEGRADED -> NodeDegraded
        NodeStatus.OFFLINE -> NodeOffline
        else -> SteelMid
    }

    AwarespanCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.name.ifEmpty { node.nodeId },
                    fontFamily = FontFamilyDisplay,
                    fontSize = 15.sp,
                    color = BoneWhite,
                )
                AwarespanBadge(text = node.status.name, color = statusColor)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "ID: ${node.nodeId}",
                fontFamily = FontFamilyMono,
                fontSize = 10.sp,
                color = SteelMid,
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HEARTBEAT: ${formatTime(node.lastHeartbeat)}",
                    fontFamily = FontFamilyMono,
                    fontSize = 10.sp,
                    color = FogLight,
                )

                if (node.batteryLevel != null) {
                    Text(
                        text = "BATTERY: ${node.batteryLevel}%",
                        fontFamily = FontFamilyMono,
                        fontSize = 10.sp,
                        color = if (node.batteryLevel < 20) NodeOffline else NodeOnline,
                    )
                }
            }

            if (node.firmwareVersion != null && node.firmwareVersion.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "FIRMWARE: ${node.firmwareVersion}",
                    fontFamily = FontFamilyMono,
                    fontSize = 10.sp,
                    color = SteelMid,
                )
            }
        }
    }
}

private fun formatTime(instant: Instant?): String {
    if (instant == null) return "NEVER"
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
