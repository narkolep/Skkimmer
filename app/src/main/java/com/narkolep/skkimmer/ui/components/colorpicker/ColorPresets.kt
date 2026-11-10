package com.narkolep.skkimmer.ui.components.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.narkolep.skkimmer.keyboard.ui.theme.HSVColor

@Composable
fun ColorPresets(
    presets: List<HSVColor>,
    selectedColor: HSVColor,
    onColorSelected: (HSVColor) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(presets) { hsv ->
            val color = hsv.toColor()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (hsv == selectedColor) 3.dp else 1.dp,
                        color = if (hsv == selectedColor)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable {
                        onColorSelected(hsv)
                    }
            )
        }
    }
}
