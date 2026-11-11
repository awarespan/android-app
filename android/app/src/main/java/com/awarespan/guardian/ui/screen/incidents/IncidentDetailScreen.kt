package com.awarespan.guardian.ui.screen.incidents

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.domain.model.HumanLabel
import com.awarespan.guardian.domain.model.IncidentState
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.AwarespanErrorView
import com.awarespan.guardian.ui.components.AwarespanLoadingView
import com.awarespan.guardian.ui.components.AwarespanSectionHeader
import com.awarespan.guardian.ui.components.AwarespanTextField
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.components.ConfidenceMeter
import com.awarespan.guardian.ui.components.ThreatBadge
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
fun IncidentDetailScreen(
    onBack: () -> Unit,
    viewModel: IncidentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var notes by remember { mutableStateOf("") }
    val incident = state.incident

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
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AwarespanButton(
                    text = "← Back",
                    onClick = onBack,
                    variant = ButtonVariant.OUTLINE
                )
                Text(
                    text = "INCIDENT // TELEMETRY",
                    fontFamily = FontFamilyMono,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = SteelMid,
                )
            }

            when {
                state.loading && incident == null -> {
                    AwarespanLoadingView(message = "RESOLVING INCIDENT DETAILS…")
                }
                incident == null -> {
                    AwarespanErrorView(
                        error = state.error ?: "Incident record unavailable",
                        onRetry = { viewModel.refresh() }
                    )
                }
                else -> {
                    // Header Overview Card
                    AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThreatBadge(threatType = incident.threat.type)
                                Spacer(Modifier.width(8.dp))
                                val (stateLabel, stateColor) = when (incident.state) {
                                    IncidentState.VERIFIED -> "VERIFIED" to StatusSafe
                                    IncidentState.REJECTED -> "REJECTED" to SteelMid
                                    IncidentState.PENDING_VERIFICATION -> "PENDING VERIFICATION" to StatusWarning
                                    IncidentState.ESCALATED -> "ESCALATED" to StatusCritical
                                    else -> incident.state.name to SteelMid
                                }
                                AwarespanBadge(text = stateLabel, color = stateColor)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = formatTimestamp(incident.timestamp),
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    color = SteelMid,
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = incident.threat.description.ifEmpty { "${incident.threat.type.name} DETECTION" },
                                fontFamily = FontFamilyDisplay,
                                fontSize = 20.sp,
                                color = BoneWhite,
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "INCIDENT ID: ${incident.id}",
                                fontFamily = FontFamilyMono,
                                fontSize = 10.sp,
                                color = SteelMid,
                            )
                            incident.nodeId?.let {
                                Text(
                                    text = "REPORTING NODE: $it",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 10.sp,
                                    color = SteelMid,
                                )
                            }
                        }
                    }

                    // AI Prediction & Detection Metadata (IMMUTABLE)
                    incident.detectionResult?.let { det ->
                        AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI MODEL DETECTION",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.8.sp,
                                        color = SteelMid,
                                    )
                                    AwarespanBadge(text = "IMMUTABLE AI INFERENCE", color = SteelMid)
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = "CLASSIFICATION: ${det.predictedClass.uppercase()}",
                                    fontFamily = FontFamilyDisplay,
                                    fontSize = 15.sp,
                                    color = BoneWhite,
                                )

                                Spacer(Modifier.height(8.dp))
                                ConfidenceMeter(det.confidence.toFloat())

                                if (det.modelVersion != null && det.modelVersion.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "MODEL VERSION: ${det.modelVersion}",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 10.sp,
                                        color = SteelMid,
                                    )
                                }

                                if (det.sensorModalities != null && det.sensorModalities.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "MODALITIES: ${det.sensorModalities.joinToString(", ")}",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 10.sp,
                                        color = SteelMid,
                                    )
                                }
                            }
                        }
                    }

                    // Location Card
                    incident.location?.let { loc ->
                        AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = "GEOGRAPHIC TELEMETRY",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    color = SteelMid,
                                )
                                Spacer(Modifier.height(8.dp))

                                if (loc.latitude != 0.0 || loc.longitude != 0.0) {
                                    Text(
                                        text = "COORDINATES: ${loc.latitude}, ${loc.longitude}",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 13.sp,
                                        color = BoneWhite,
                                    )
                                    if (loc.accuracyMeters > 0.0) {
                                        Text(
                                            text = "ACCURACY RADIUS: ±${loc.accuracyMeters}m",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 10.sp,
                                            color = SteelMid,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "GPS LOCK PENDING // OFF-GRID",
                                        fontFamily = FontFamilyMono,
                                        fontSize = 12.sp,
                                        color = StatusWarning,
                                    )
                                }

                                if (!loc.humanReadable.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "SECTOR: ${loc.humanReadable}",
                                        fontFamily = FontFamilyDisplay,
                                        fontSize = 13.sp,
                                        color = FogLight,
                                    )
                                }
                            }
                        }
                    }

                    // Existing Human Verification Annotations
                    if (incident.humanAnnotations != null && incident.humanAnnotations.isNotEmpty()) {
                        AwarespanCard(modifier = Modifier.fillMaxWidth(), borderColor = StatusSafe) {
                            Column {
                                Text(
                                    text = "OPERATOR VERIFICATION LOG",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    color = StatusSafe,
                                )
                                Spacer(Modifier.height(8.dp))

                                for (ann in incident.humanAnnotations) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "VERDICT: ${ann.label.name}",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 12.sp,
                                            color = BoneWhite,
                                        )
                                        Text(
                                            text = "BY: ${ann.annotatorId ?: "OPERATOR"}",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 10.sp,
                                            color = SteelMid,
                                        )
                                    }
                                    if (!ann.notes.isNullOrBlank()) {
                                        Text(
                                            text = "NOTES: ${ann.notes}",
                                            fontFamily = FontFamilyDisplay,
                                            fontSize = 12.sp,
                                            color = FogLight,
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }

                    // Operator Verification Actions (if pending)
                    if (incident.requiresVerification() && !state.completed) {
                        AwarespanCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = EmberOrange
                        ) {
                            Column {
                                Text(
                                    text = "OPERATOR VERIFICATION REQUIRED",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    color = EmberOrange,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Submit a ground-truth confirmation or rejection. AI model predictions remain preserved separately for training dataset annotation.",
                                    fontFamily = FontFamilyDisplay,
                                    fontSize = 12.sp,
                                    color = FogLight,
                                )

                                Spacer(Modifier.height(12.dp))

                                AwarespanTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    label = "OPERATOR LOG NOTES (OPTIONAL)",
                                    placeholder = "Enter observation details or confirmation reasons…",
                                    singleLine = false,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Spacer(Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AwarespanButton(
                                        text = "VERIFY (TRUE POSITIVE)",
                                        onClick = { viewModel.verify(HumanLabel.TRUE_POSITIVE, notes) },
                                        variant = ButtonVariant.PRIMARY,
                                        enabled = !state.submitting,
                                        loading = state.submitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AwarespanButton(
                                            text = "REJECT",
                                            onClick = { viewModel.verify(HumanLabel.FALSE_POSITIVE, notes) },
                                            variant = ButtonVariant.CRITICAL,
                                            enabled = !state.submitting,
                                            modifier = Modifier.weight(1f),
                                        )
                                        AwarespanButton(
                                            text = "UNCERTAIN",
                                            onClick = { viewModel.verify(HumanLabel.UNCERTAIN, notes) },
                                            variant = ButtonVariant.OUTLINE,
                                            enabled = !state.submitting,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    } else if (state.completed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Carbon, RectangleShape)
                                .border(1.dp, StatusSafe, RectangleShape)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "VERIFICATION DISPATCHED // SYNCHRONIZED WITH BACKEND",
                                fontFamily = FontFamilyMono,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp,
                                color = StatusSafe,
                            )
                        }
                    }

                    // Autonomous Response Events Log
                    if (incident.responseEvents != null && incident.responseEvents.isNotEmpty()) {
                        AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = "AUTOPILOT & RESPONSE ACTIONS",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    color = SteelMid,
                                )
                                Spacer(Modifier.height(8.dp))

                                for (re in incident.responseEvents) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${re.actionType.name} (${if (re.isAutonomous) "AUTONOMOUS" else "MANUAL"})",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 11.sp,
                                            color = BoneWhite,
                                        )
                                        Text(
                                            text = re.result ?: "EXECUTED",
                                            fontFamily = FontFamilyMono,
                                            fontSize = 10.sp,
                                            color = if ("SUCCESS".equals(re.result, true)) StatusSafe else FogLight,
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }

                    state.error?.let {
                        AwarespanErrorView(error = it)
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

private fun formatTimestamp(instant: Instant?): String {
    if (instant == null) return "—"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
