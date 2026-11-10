package com.narkolep.skkimmer.ui.components.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.narkolep.skkimmer.keyboard.ui.theme.HSVColor

val defaultHsvPresets = listOf(
    HSVColor(210f, 0.4f, 0.6f),
    HSVColor(220f, 0.2f, 0.5f),
    HSVColor(280f, 0.3f, 0.6f),
    HSVColor(0f, 0f, 1f),
    HSVColor(0f, 0f, 0f),
    HSVColor(0f, 0.8f, 0.9f),
    HSVColor(30f, 0.8f, 1f),
    HSVColor(60f, 0.8f, 1f),
    HSVColor(120f, 0.8f, 0.7f),
    HSVColor(200f, 0.8f, 0.9f)
)

@Composable
fun ColorPickerDialog(
    currentHsv: HSVColor,
    defaultHsv: HSVColor = HSVColor.Default,
    presets: List<HSVColor> = defaultHsvPresets,
    onDismiss: () -> Unit,
    onSelect: (HSVColor) -> Unit
) {
    var selectedHsv by remember(currentHsv) {
        mutableStateOf(currentHsv)
    }

    var hexText by remember(currentHsv) {
        mutableStateOf(currentHsv.toColor().toHex())
    }

    fun updateHsv(
        hue: Float = selectedHsv.hue,
        saturation: Float = selectedHsv.saturation,
        value: Float = selectedHsv.value,
        alpha: Float = selectedHsv.alpha
    ) {
        selectedHsv = HSVColor(
            hue = hue,
            saturation = saturation,
            value = value,
            alpha = alpha
        )
        hexText = selectedHsv.toColor().toHex()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .heightIn(max = 800.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Text(
                    text = "Select color",
                    style = MaterialTheme.typography.headlineMedium
                )

                // Saturation / Value
                SaturationValuePicker(
                    hue = selectedHsv.hue,
                    saturation = selectedHsv.saturation,
                    value = selectedHsv.value,
                    onValueChange = { s, v ->
                        updateHsv(
                            saturation = s,
                            value = v
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Hue
                HueSlider(
                    hue = selectedHsv.hue,
                    onHueChange = { h ->
                        updateHsv(hue = h)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // HEX
                Column {
                    Text(
                        text = "Hex (#RRGGBB)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            val normalized = input
                                .uppercase()
                                .filter {
                                    it in "0123456789ABCDEF#"
                                }

                            hexText = normalized

                            normalized.toComposeColor()?.let { color ->
                                val hsvArray = color.toHsv()
                                updateHsv(
                                    hue = hsvArray[0],
                                    saturation = hsvArray[1],
                                    value = hsvArray[2],
                                    alpha = color.alpha
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Presets
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Presets",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ColorPresets(
                        presets = presets,
                        selectedColor = selectedHsv,
                        onColorSelected = { hsv ->
                            selectedHsv = hsv
                            hexText = hsv.toColor().toHex()
                        }
                    )
                }

                // Preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentHsv.toColor())
                    )

                    Text("→")

                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedHsv.toColor())
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedHsv = defaultHsv
                            hexText = defaultHsv.toColor().toHex()
                        }
                    ) {
                        Text("Reset to default")
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSelect(selectedHsv)
                        }
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}
