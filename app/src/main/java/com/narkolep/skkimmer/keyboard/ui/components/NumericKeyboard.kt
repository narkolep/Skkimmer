package com.narkolep.skkimmer.keyboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.narkolep.skkimmer.keyboard.mappings.NumericMap

@Composable
fun NumericKeyboard(
    height: Float,
    backgroundColor: Color,
    buttonColor: Color,
    textColor: Color,
    onInput: (String) -> Unit,
    onActionInput: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        NumericMap.numericLayout.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth().height(height.dp)
            ) {
                rowKeys.forEach { config ->
                    SkkKey(
                        mainText = config.label,
                        modifier = Modifier.weight(1f),
                        keyColor = if (config.color) backgroundColor else buttonColor,
                        textColor = textColor,
                        iconResId = config.iconResId,
                        keyboardHeight = height,
                        onClick = {
                            if (config.action != null) onActionInput(config.action)
                            else onInput(config.label)
                        }
                    )
                }
            }
        }
    }
}