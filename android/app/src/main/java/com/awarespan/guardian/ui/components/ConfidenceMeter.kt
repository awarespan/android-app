package com.awarespan.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.StatusCritical
import com.awarespan.guardian.ui.theme.StatusSafe
import com.awarespan.guardian.ui.theme.StatusWarning
import com.awarespan.guardian.ui.theme.SteelMid

/** Sharp rectangular architectural confidence gauge with monospaced readout. */
@Composable
fun ConfidenceMeter(confidence: Float, modifier: Modifier = Modifier) {
    val clamped = confidence.coerceIn(0f, 1f)
    val color: Color = when {
        clamped < 0.5f -> StatusCritical
        clamped < 0.8f -> StatusWarning
        else -> StatusSafe
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODEL CONFIDENCE",
                fontFamily = FontFamilyMono,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = SteelMid,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${(clamped * 100).toInt()}%",
                fontFamily = FontFamilyMono,
                fontSize = 11.sp,
                color = color,
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Carbon, RectangleShape)
                .border(1.dp, GraphiteBorder, RectangleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .background(color, RectangleShape)
            )
        }
    }
}
