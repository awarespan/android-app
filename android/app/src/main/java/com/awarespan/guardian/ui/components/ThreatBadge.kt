package com.awarespan.guardian.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.awarespan.guardian.domain.model.ThreatType
import com.awarespan.guardian.ui.theme.StatusCritical
import com.awarespan.guardian.ui.theme.StatusInfo
import com.awarespan.guardian.ui.theme.StatusMuted
import com.awarespan.guardian.ui.theme.StatusWarning

/** Color-coded threat type badge with sharp rectangular AWARESPAN styling. */
@Composable
fun ThreatBadge(threatType: ThreatType, modifier: Modifier = Modifier) {
    val (label, color) = when (threatType) {
        ThreatType.WEAPON -> "WEAPON" to StatusCritical
        ThreatType.ASSAULT -> "ASSAULT" to StatusCritical
        ThreatType.TRESPASS -> "TRESPASS" to StatusWarning
        ThreatType.LOITERING -> "LOITERING" to StatusWarning
        ThreatType.WILDLIFE -> "WILDLIFE" to StatusInfo
        ThreatType.SUSPICIOUS_OBJECT -> "SUSPICIOUS" to StatusWarning
        ThreatType.UNKNOWN -> "UNKNOWN" to StatusMuted
    }
    AwarespanBadge(text = label, color = color, modifier = modifier)
}
