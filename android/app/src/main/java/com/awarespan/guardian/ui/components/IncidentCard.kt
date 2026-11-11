package com.awarespan.guardian.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awarespan.guardian.domain.model.HumanLabel
import com.awarespan.guardian.domain.model.Incident
import com.awarespan.guardian.domain.model.IncidentState
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.StatusCritical
import com.awarespan.guardian.ui.theme.StatusSafe
import com.awarespan.guardian.ui.theme.StatusWarning
import com.awarespan.guardian.ui.theme.SteelMid
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Sharp rectangular incident summary card matching AWARESPAN operations aesthetic. */
@Composable
fun IncidentCard(incident: Incident, onClick: () -> Unit) {
    AwarespanCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        contentPadding = 14.dp,
    ) {
        Column {
            // Header row: Threat classification badge + Status tag + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThreatBadge(threatType = incident.threat.type)
                Spacer(Modifier.width(8.dp))

                // State indicator badge
                val (stateLabel, stateColor) = when (incident.state) {
                    IncidentState.VERIFIED -> "VERIFIED" to StatusSafe
                    IncidentState.REJECTED -> "REJECTED" to SteelMid
                    IncidentState.PENDING_VERIFICATION -> "PENDING" to StatusWarning
                    IncidentState.ESCALATED -> "ESCALATED" to StatusCritical
                    IncidentState.AUTO_HANDLED -> "AUTONOMOUS" to StatusSafe
                    else -> incident.state.name to SteelMid
                }
                AwarespanBadge(text = stateLabel, color = stateColor)

                Spacer(Modifier.weight(1f))

                Text(
                    text = formatTime(incident.timestamp),
                    fontFamily = FontFamilyMono,
                    fontSize = 11.sp,
                    color = SteelMid,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Threat description / classification headline
            val title = if (incident.threat.description.isNotBlank()) {
                incident.threat.description
            } else {
                "${incident.threat.type.name} DETECTED"
            }
            Text(
                text = title,
                fontFamily = FontFamilyDisplay,
                fontSize = 15.sp,
                color = BoneWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(8.dp))

            // Metadata footer: Location coordinates / Node identity / Verification preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val locText = incident.location?.let { loc ->
                    if (loc.humanReadable != null && loc.humanReadable.isNotBlank()) {
                        loc.humanReadable
                    } else if (loc.latitude != 0.0 || loc.longitude != 0.0) {
                        String.format("%.4f, %.4f", loc.latitude, loc.longitude)
                    } else null
                } ?: "LOC: UNRECORDED"

                Text(
                    text = locText,
                    fontFamily = FontFamilyMono,
                    fontSize = 10.sp,
                    color = FogLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(Modifier.width(8.dp))

                val nodeText = if (incident.nodeId != null) "NODE: ${incident.nodeId}" else "NODE: UNKNOWN"
                Text(
                    text = nodeText,
                    fontFamily = FontFamilyMono,
                    fontSize = 10.sp,
                    color = SteelMid,
                )
            }
        }
    }
}

private fun formatTime(instant: Instant?): String {
    if (instant == null) return "—"
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
