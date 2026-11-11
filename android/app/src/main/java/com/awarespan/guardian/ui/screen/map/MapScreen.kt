package com.awarespan.guardian.ui.screen.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.model.Node
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanLoadingView
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.NodeOnline
import com.awarespan.guardian.ui.theme.StatusWarning
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun MapScreen(
    onIncidentClick: (String) -> Unit,
    onNodeClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
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
                        text = "TACTICAL MAP",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = BoneWhite,
                    )
                    Text(
                        text = "SECTOR GEOGRAPHIC POSITIONING & NODES",
                        fontFamily = FontFamilyMono,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = SteelMid,
                    )
                }

                AwarespanButton(
                    text = "Sync",
                    onClick = { viewModel.refresh() },
                    variant = ButtonVariant.OUTLINE,
                    loading = state.loading,
                )
            }

            if (state.loading) {
                AwarespanLoadingView(message = "ACQUIRING SECTOR COORDINATES…")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tactical Radar Canvas
                    item {
                        TacticalRadarCanvas(
                            nodes = state.nodesWithLocation,
                            incidents = state.incidentsWithLocation
                        )
                    }

                    // Stats row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AwarespanCard(
                                modifier = Modifier.weight(1f),
                                contentPadding = 12.dp
                            ) {
                                Column {
                                    Text(
                                        text = "COORDINATE NODES",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 9.sp,
                                        color = SteelMid
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${state.nodesWithLocation.size} ACTIVE",
                                        fontFamily = FontFamilyDisplay,
                                        fontSize = 15.sp,
                                        color = BoneWhite
                                    )
                                }
                            }

                            AwarespanCard(
                                modifier = Modifier.weight(1f),
                                contentPadding = 12.dp
                            ) {
                                Column {
                                    Text(
                                        text = "GEOTAGGED THREATS",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 9.sp,
                                        color = SteelMid
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${state.incidentsWithLocation.size} DETECTIONS",
                                        fontFamily = FontFamilyDisplay,
                                        fontSize = 15.sp,
                                        color = if (state.incidentsWithLocation.isNotEmpty()) EmberOrange else BoneWhite
                                    )
                                }
                            }
                        }
                    }

                    // Located Incidents List
                    if (state.incidentsWithLocation.isNotEmpty()) {
                        item {
                            Text(
                                text = "ACTIVE GEOTAGGED THREATS",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = EmberOrange,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(state.incidentsWithLocation, key = { it.id }) { inc ->
                            AwarespanCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onIncidentClick(inc.id) },
                                borderColor = EmberOrange,
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = inc.threat.type.name,
                                            fontFamily = FontFamilyDisplay,
                                            fontSize = 14.sp,
                                            color = BoneWhite
                                        )
                                        Text(
                                            text = "LAT: ${inc.location?.latitude}, LNG: ${inc.location?.longitude}",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 10.sp,
                                            color = SteelMid
                                        )
                                    }
                                    AwarespanBadge(text = inc.state.name, color = EmberOrange)
                                }
                            }
                        }
                    }

                    // Located Nodes List
                    if (state.nodesWithLocation.isNotEmpty()) {
                        item {
                            Text(
                                text = "SECTOR FIXED NODES",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = BoneWhite,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(state.nodesWithLocation, key = { it.nodeId }) { node ->
                            AwarespanCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onNodeClick(node.nodeId) },
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = node.name.ifEmpty { node.nodeId },
                                            fontFamily = FontFamilyDisplay,
                                            fontSize = 14.sp,
                                            color = BoneWhite
                                        )
                                        Text(
                                            text = "LAT: ${node.location?.latitude}, LNG: ${node.location?.longitude}",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 10.sp,
                                            color = SteelMid
                                        )
                                    }
                                    AwarespanBadge(text = "ONLINE", color = NodeOnline)
                                }
                            }
                        }
                    }

                    // Off-grid / Pending GPS Items (Explicit Graceful Handling)
                    val unlocatedCount = state.nodesWithoutLocation.size + state.incidentsWithoutLocation.size
                    if (unlocatedCount > 0) {
                        item {
                            AwarespanCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                borderColor = GraphiteBorder,
                                contentPadding = 14.dp
                            ) {
                                Column {
                                    Text(
                                        text = "OFF-GRID / PENDING GPS TELEMETRY ($unlocatedCount)",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.8.sp,
                                        color = StatusWarning
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${state.nodesWithoutLocation.size} nodes and ${state.incidentsWithoutLocation.size} incidents have no confirmed GPS coordinate telemetry recorded. Unverified coordinates are not fabricated.",
                                        fontFamily = FontFamilyDisplay,
                                        fontSize = 12.sp,
                                        color = FogLight
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalRadarCanvas(
    nodes: List<Node>,
    incidents: List<Incident>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Carbon, RectangleShape)
            .border(1.dp, GraphiteBorder, RectangleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Hairline coordinate grid lines
            drawLine(GraphiteBorder, Offset(0f, height / 2f), Offset(width, height / 2f), strokeWidth = 1f)
            drawLine(GraphiteBorder, Offset(width / 2f, 0f), Offset(width / 2f, height), strokeWidth = 1f)
            drawLine(GraphiteBorder.copy(alpha = 0.5f), Offset(0f, height / 4f), Offset(width, height / 4f), strokeWidth = 1f)
            drawLine(GraphiteBorder.copy(alpha = 0.5f), Offset(0f, 3f * height / 4f), Offset(width, 3f * height / 4f), strokeWidth = 1f)
            drawLine(GraphiteBorder.copy(alpha = 0.5f), Offset(width / 4f, 0f), Offset(width / 4f, height), strokeWidth = 1f)
            drawLine(GraphiteBorder.copy(alpha = 0.5f), Offset(3f * width / 4f, 0f), Offset(3f * width / 4f, height), strokeWidth = 1f)

            // Normalize coordinates if points exist
            val allPoints = mutableListOf<Pair<Double, Double>>()
            nodes.forEach { n -> n.location?.let { allPoints.add(it.latitude to it.longitude) } }
            incidents.forEach { i -> i.location?.let { allPoints.add(it.latitude to it.longitude) } }

            val minLat = allPoints.minOfOrNull { it.first } ?: 0.0
            val maxLat = allPoints.maxOfOrNull { it.first } ?: 1.0
            val minLng = allPoints.minOfOrNull { it.second } ?: 0.0
            val maxLng = allPoints.maxOfOrNull { it.second } ?: 1.0

            val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
            val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

            // Render Nodes as green-bordered square markers
            nodes.forEach { n ->
                n.location?.let { loc ->
                    val x = (((loc.longitude - minLng) / lngSpan) * (width - 40) + 20).toFloat()
                    val y = ((1.0 - (loc.latitude - minLat) / latSpan) * (height - 40) + 20).toFloat()
                    drawRect(NodeOnline, topLeft = Offset(x - 5, y - 5), size = Size(10f, 10f))
                }
            }

            // Render Incidents as ignited Ember Orange circles
            incidents.forEach { inc ->
                inc.location?.let { loc ->
                    val x = (((loc.longitude - minLng) / lngSpan) * (width - 40) + 20).toFloat()
                    val y = ((1.0 - (loc.latitude - minLat) / latSpan) * (height - 40) + 20).toFloat()
                    drawCircle(EmberOrange, radius = 6f, center = Offset(x, y))
                }
            }
        }

        // Overlay corner radar indicators
        Text(
            text = "GRID // RELATIVE POSITIONING",
            fontFamily = FontFamilyMono,
            fontSize = 9.sp,
            color = SteelMid,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        Text(
            text = "SCALE: NOMINAL",
            fontFamily = FontFamilyMono,
            fontSize = 9.sp,
            color = SteelMid,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}
