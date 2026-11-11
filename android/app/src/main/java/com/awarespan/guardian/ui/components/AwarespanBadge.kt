package com.awarespan.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awarespan.guardian.ui.theme.FontFamilyMono

@Composable
fun AwarespanBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fillAlpha: Float = 0.12f,
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = fillAlpha), RectangleShape)
            .border(1.dp, color, RectangleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontFamily = FontFamilyMono,
            fontSize = 10.sp,
            letterSpacing = 0.6.sp,
        )
    }
}
