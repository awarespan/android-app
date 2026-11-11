package com.awarespan.guardian.ui.screen.events

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
import com.awarespan.guardian.data.remote.websocket.WebSocketEvent
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanEmptyView
import com.awarespan.guardian.ui.components.ButtonVariant
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EventHistoryScreen(
    onBack: () -> Unit,
    viewModel: EventHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val (connLabel, connColor) = when (state.connectionState) {
        WebSocketEvent.ConnectionState.CONNECTED -> "CONNECTED // REALTIME" to StatusSafe
        WebSocketEvent.ConnectionState.CONNECTING -> "ESTABLISHING HANDSHAKE…" to StatusWarning
        WebSocketEvent.ConnectionState.DISCONNECTED -> "OFFLINE // DISCONNECTED" to SteelMid
        WebSocketEvent.ConnectionState.FAILED -> "CHANNEL FAULT" to StatusCritical
    }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AwarespanButton(text = "← Back", onClick = onBack, variant = ButtonVariant.OUTLINE)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EVENT LOG",
                            fontFamily = FontFamilyDisplay,
                            fontSize = 18.sp,
                            color = BoneWhite,
                        )
                        Text(
                            text = "REAL-TIME AUDIT STREAM",
                            fontFamily = FontFamilyMono,
                            fontSize = 10.sp,
                            color = SteelMid,
                        )
                    }
                }

                AwarespanBadge(text = connLabel, color = connColor)
            }

            if (state.events.isEmpty()) {
                AwarespanEmptyView(
                    title = "NO EVENT LOGS",
                    message = "Awaiting real-time telemetry events from edge nodes."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.events) { event ->
                        AwarespanCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (event.isAlert) EmberOrange else SteelMid.copy(alpha = 0.5f),
                            contentPadding = 12.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AwarespanBadge(
                                        text = event.type,
                                        color = if (event.isAlert) EmberOrange else SteelMid
                                    )
                                    Text(
                                        text = formatTime(event.timestamp),
                                        fontFamily = FontFamilyMono,
                                        fontSize = 10.sp,
                                        color = SteelMid,
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = event.title,
                                    fontFamily = FontFamilyDisplay,
                                    fontSize = 14.sp,
                                    color = BoneWhite,
                                    )

                                if (event.details.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = event.details,
                                        fontFamily = FontFamilyMono,
                                        fontSize = 11.sp,
                                        color = FogLight,
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

private fun formatTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
