package com.awarespan.guardian.ui.screen.incidents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.domain.model.IncidentState
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanEmptyView
import com.awarespan.guardian.ui.components.AwarespanLoadingView
import com.awarespan.guardian.ui.components.AwarespanOfflineBanner
import com.awarespan.guardian.ui.components.AwarespanTextField
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.components.IncidentCard
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun IncidentFeedScreen(
    onIncidentClick: (String) -> Unit,
    viewModel: IncidentFeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Feed Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INCIDENT FEED",
                    fontFamily = FontFamilyDisplay,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    color = BoneWhite,
                )
                Text(
                    text = "REAL-TIME DETECTION & VERIFICATION STREAM",
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

        // Search Input
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            AwarespanTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = "Filter by threat type, description, node ID, or location…",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Tabs (Sharp 0dp rectangular buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterStates = listOf<Pair<String, IncidentState?>>(
                "ALL" to null,
                "PENDING" to IncidentState.PENDING_VERIFICATION,
                "VERIFIED" to IncidentState.VERIFIED,
                "REJECTED" to IncidentState.REJECTED,
                "ESCALATED" to IncidentState.ESCALATED,
            )

            filterStates.forEach { (label, incidentState) ->
                val selected = state.stateFilter == incidentState
                Box(
                    modifier = Modifier
                        .background(if (selected) EmberOrange else Carbon, RectangleShape)
                        .border(1.dp, if (selected) EmberOrange else GraphiteBorder, RectangleShape)
                        .clickable { viewModel.setStateFilter(incidentState) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp,
                        color = if (selected) VoidBlack else BoneWhite,
                    )
                }
            }
        }

        if (state.offline) {
            AwarespanOfflineBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        // Incident Stream
        when {
            state.loading && state.filteredIncidents.isEmpty() -> {
                AwarespanLoadingView(message = "STREAMING INCIDENTS…")
            }
            state.filteredIncidents.isEmpty() -> {
                AwarespanEmptyView(
                    title = "ZERO MATCHING INCIDENTS",
                    message = "No telemetry reports match the current filter parameters."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.filteredIncidents, key = { it.id }) { incident ->
                        IncidentCard(
                            incident = incident,
                            onClick = { onIncidentClick(incident.id) }
                        )
                    }
                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
