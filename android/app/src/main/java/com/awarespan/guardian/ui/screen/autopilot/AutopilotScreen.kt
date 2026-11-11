package com.awarespan.guardian.ui.screen.autopilot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.domain.model.ThreatType
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanErrorView
import com.awarespan.guardian.ui.components.AwarespanLoadingView
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.StatusCritical
import com.awarespan.guardian.ui.theme.StatusSafe
import com.awarespan.guardian.ui.theme.StatusWarning
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AutopilotScreen(
    onBack: () -> Unit,
    viewModel: AutopilotViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AwarespanButton(text = "← Back", onClick = onBack, variant = ButtonVariant.OUTLINE)
                Text(
                    text = "AUTOPILOT POLICY",
                    fontFamily = FontFamilyMono,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = SteelMid,
                )
            }

            if (state.loading) {
                AwarespanLoadingView(message = "READING ACTIVE POLICY RULES…")
            } else {
                // Sync State Card
                val (syncLabel, syncColor) = when (state.syncStatus) {
                    PolicySyncStatus.SYNCHRONIZED -> "POLICY CONFIRMED SYNCED" to StatusSafe
                    PolicySyncStatus.PENDING_LOCAL_CHANGES -> "UNSAVED LOCAL CHANGES" to StatusWarning
                    PolicySyncStatus.APPLYING_CHANGES -> "SYNCHRONIZING WITH BACKEND…" to EmberOrange
                    PolicySyncStatus.SYNC_FAILED -> "SYNC REJECTED / FAILED" to StatusCritical
                }

                AwarespanCard(modifier = Modifier.fillMaxWidth(), borderColor = syncColor) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NODE: ${state.nodeId.uppercase()}",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                color = BoneWhite
                            )
                            AwarespanBadge(text = syncLabel, color = syncColor)
                        }

                        state.lastSyncTimestamp?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "CONFIRMED TIMESTAMP: ${formatTimestamp(it)}",
                                fontFamily = FontFamilyMono,
                                fontSize = 10.sp,
                                color = SteelMid
                            )
                        }

                        state.statusMessage?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = it,
                                fontFamily = FontFamilyDisplay,
                                fontSize = 12.sp,
                                color = FogLight
                            )
                        }
                    }
                }

                // Autopilot Enabled Toggle
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AUTONOMOUS INTERVENTION",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 15.sp,
                                color = BoneWhite
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Permit edge hardware to autonomously trigger strobes, alarms, and escalation without manual operator intervention.",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 12.sp,
                                color = FogLight
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .background(if (state.enabled) EmberOrange else Carbon, RectangleShape)
                                .border(1.dp, if (state.enabled) EmberOrange else GraphiteBorder, RectangleShape)
                                .clickable { viewModel.toggleEnabled(!state.enabled) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (state.enabled) "ACTIVE" else "DISABLED",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                color = if (state.enabled) VoidBlack else SteelMid
                            )
                        }
                    }
                }

                // Confidence Threshold Control
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MINIMUM CONFIDENCE THRESHOLD",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp,
                                color = SteelMid
                            )
                            Text(
                                text = "${(state.confidenceThreshold * 100).toInt()}%",
                                fontFamily = FontFamilyMono,
                                fontSize = 12.sp,
                                color = EmberOrange
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Slider(
                            value = state.confidenceThreshold.toFloat(),
                            onValueChange = { viewModel.setConfidenceThreshold(it.toDouble()) },
                            valueRange = 0.5f..0.99f,
                            colors = SliderDefaults.colors(
                                thumbColor = EmberOrange,
                                activeTrackColor = EmberOrange,
                                inactiveTrackColor = GraphiteBorder
                            )
                        )

                        Text(
                            text = "Edge nodes will only intervene when AI inference confidence strictly equals or exceeds this threshold.",
                            fontFamily = FontFamilyDisplay,
                            fontSize = 11.sp,
                            color = SteelMid
                        )
                    }
                }

                // Deterrence Timeout Seconds
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DETERRENCE TIMEOUT",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                color = SteelMid
                            )
                            Text(
                                text = "${state.deterrenceTimeoutSeconds} SECONDS",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 15.sp,
                                color = BoneWhite
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Carbon, RectangleShape)
                                    .border(1.dp, GraphiteBorder, RectangleShape)
                                    .clickable { viewModel.setDeterrenceTimeout(state.deterrenceTimeoutSeconds - 1) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("-", fontFamily = FontFamilyMono, color = BoneWhite, fontSize = 16.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .background(Carbon, RectangleShape)
                                    .border(1.dp, GraphiteBorder, RectangleShape)
                                    .clickable { viewModel.setDeterrenceTimeout(state.deterrenceTimeoutSeconds + 1) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("+", fontFamily = FontFamilyMono, color = BoneWhite, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Allowed Threat Types Matrix
                AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "AUTHORIZED AUTONOMOUS THREAT DOMAINS",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            letterSpacing = 0.6.sp,
                            color = SteelMid
                        )
                        Spacer(Modifier.height(10.dp))

                        val availableTypes = listOf(
                            ThreatType.WEAPON,
                            ThreatType.ASSAULT,
                            ThreatType.TRESPASS,
                            ThreatType.LOITERING,
                            ThreatType.SUSPICIOUS_OBJECT
                        )

                        availableTypes.forEach { type ->
                            val allowed = state.allowedThreatTypes.contains(type)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.toggleThreatType(type) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type.name,
                                    fontFamily = FontFamilyMono,
                                    fontSize = 12.sp,
                                    color = if (allowed) BoneWhite else SteelMid
                                )

                                AwarespanBadge(
                                    text = if (allowed) "PERMITTED" else "PROHIBITED",
                                    color = if (allowed) StatusSafe else SteelMid
                                )
                            }
                        }
                    }
                }

                state.error?.let {
                    AwarespanErrorView(error = it)
                }

                // Submit Action
                Spacer(Modifier.height(8.dp))
                AwarespanButton(
                    text = if (state.syncStatus == PolicySyncStatus.APPLYING_CHANGES) "SYNCHRONIZING RULES…" else "CONFIRM & APPLY POLICY",
                    onClick = { viewModel.applyPolicyChanges() },
                    variant = ButtonVariant.PRIMARY,
                    loading = state.syncStatus == PolicySyncStatus.APPLYING_CHANGES,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private fun formatTimestamp(instant: Instant?): String {
    if (instant == null) return "—"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
