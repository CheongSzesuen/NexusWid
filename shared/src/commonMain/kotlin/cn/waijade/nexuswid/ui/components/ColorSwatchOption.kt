package cn.waijade.nexuswid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun ColorSwatchOption(
    selected: Boolean,
    swatch: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        swatch.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    }
    val checkColor = if (swatch.luminance() > 0.55f) Color.Black else Color.White

    Surface(
        onClick = onClick,
        modifier = modifier.size(50.dp),
        shape = CircleShape,
        color = swatch,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = checkColor
                )
            }
        }
    }
}
