package com.awarespan.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.StatusWarning
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun AwarespanSectionHeader(
    title: String,
    counter: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = FontFamilyDisplay,
            fontSize = 18.sp,
            color = BoneWhite,
        )
        if (counter != null) {
            Text(
                text = counter,
                fontFamily = FontFamilyMono,
                fontSize = 13.sp,
                color = SteelMid,
            )
        }
    }
}

@Composable
fun AwarespanLoadingView(
    message: String = "ACQUIRING TELEMETRY…",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = EmberOrange,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            fontFamily = FontFamilyMono,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = SteelMid,
        )
    }
}

@Composable
fun AwarespanEmptyView(
    title: String = "NO RECORDS FOUND",
    message: String = "No telemetry or event records available at this time.",
    modifier: Modifier = Modifier,
) {
    AwarespanCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentPadding = 24.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamilyMono,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                color = FogLight,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                fontFamily = FontFamilyDisplay,
                fontSize = 13.sp,
                color = SteelMid,
            )
        }
    }
}

@Composable
fun AwarespanErrorView(
    error: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    AwarespanCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        borderColor = EmberOrange,
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = "OPERATIONAL ALERT",
                fontFamily = FontFamilyMono,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = EmberOrange,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = error,
                fontFamily = FontFamilyDisplay,
                fontSize = 13.sp,
                color = BoneWhite,
            )
            if (onRetry != null) {
                Spacer(Modifier.height(12.dp))
                AwarespanButton(
                    text = "Retry Request",
                    onClick = onRetry,
                    variant = ButtonVariant.OUTLINE,
                )
            }
        }
    }
}

@Composable
fun AwarespanOfflineBanner(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Carbon, RectangleShape)
            .border(1.dp, StatusWarning, RectangleShape)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(6.dp)
                    .background(StatusWarning, RectangleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "OFFLINE MODE — SERVING CACHED LOCAL TELEMETRY",
                fontFamily = FontFamilyMono,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = StatusWarning,
            )
        }
    }
}
