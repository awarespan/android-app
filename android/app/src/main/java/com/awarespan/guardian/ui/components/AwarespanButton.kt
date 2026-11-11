package com.awarespan.guardian.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

enum class ButtonVariant {
    PRIMARY,   // Filled Ember Orange, Void Black text (design §110)
    GHOST,     // Transparent, 1px Bone White border, Bone White text (design §115)
    OUTLINE,   // Transparent, 1px Graphite border, Fog/Bone text
    CRITICAL,  // Transparent, 1px Ember Orange border, Ember Orange text
}

@Composable
fun AwarespanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val containerColor = when {
        !enabled -> Carbon
        variant == ButtonVariant.PRIMARY -> EmberOrange
        else -> Color.Transparent
    }

    val contentColor = when {
        !enabled -> SteelMid
        variant == ButtonVariant.PRIMARY -> VoidBlack
        variant == ButtonVariant.GHOST -> BoneWhite
        variant == ButtonVariant.OUTLINE -> BoneWhite
        variant == ButtonVariant.CRITICAL -> EmberOrange
        else -> BoneWhite
    }

    val border = when {
        !enabled -> BorderStroke(1.dp, GraphiteBorder)
        variant == ButtonVariant.PRIMARY -> null
        variant == ButtonVariant.GHOST -> BorderStroke(1.dp, BoneWhite)
        variant == ButtonVariant.OUTLINE -> BorderStroke(1.dp, GraphiteBorder)
        variant == ButtonVariant.CRITICAL -> BorderStroke(1.dp, EmberOrange)
        else -> null
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(
                if (enabled && !loading) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RectangleShape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = contentColor,
                    modifier = Modifier.defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                )
            } else {
                Text(
                    text = text.uppercase(),
                    color = contentColor,
                    fontFamily = FontFamilyMono,
                    fontSize = 13.sp,
                    letterSpacing = 0.8.sp,
                )
            }
        }
    }
}
