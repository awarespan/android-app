package com.awarespan.guardian.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.GraphiteBorder

@Composable
fun AwarespanCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = Carbon,
    borderColor: Color = GraphiteBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
