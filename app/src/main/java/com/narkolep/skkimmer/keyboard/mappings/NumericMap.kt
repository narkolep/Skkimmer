package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.keyboard.KeyboardAction
import com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left
import com.composables.icons.lucide.R.drawable.lucide_ic_chevron_right
import com.composables.icons.lucide.R.drawable.lucide_ic_delete
import com.composables.icons.lucide.R.drawable.lucide_ic_space
import com.composables.icons.lucide.R.drawable.lucide_ic_corner_down_left

object NumericMap {
    enum class KeyColor {
        Background,
        Button,
        Action
    }

    data class NumericConfig(
        val label: String,
        val color: KeyColor = KeyColor.Button,
        val action: KeyboardAction? = null,
        val keyRepeat: Boolean = false,
        val icon: Int? = null
    )

    val numericLayout = listOf(
        listOf(
            NumericConfig(":", color = KeyColor.Background),
            NumericConfig("1"),
            NumericConfig("2"),
            NumericConfig("3"),
            NumericConfig("BS", color = KeyColor.Background, action = KeyboardAction.Backspace, icon = lucide_ic_delete, keyRepeat = true)
        ),

        listOf(
            NumericConfig("Left", color = KeyColor.Background, action = KeyboardAction.Left, icon = lucide_ic_chevron_left, keyRepeat = true),
            NumericConfig("4"),
            NumericConfig("5"),
            NumericConfig("6"),
            NumericConfig("Right", color = KeyColor.Background, action = KeyboardAction.Right, icon = lucide_ic_chevron_right, keyRepeat = true)
        ),

        listOf(
            NumericConfig("-", color = KeyColor.Background),
            NumericConfig("7"),
            NumericConfig("8"),
            NumericConfig("9"),
            NumericConfig("Space", color = KeyColor.Background, action = KeyboardAction.Space, icon = lucide_ic_space)
        ),

        listOf(
            NumericConfig("ABC", color = KeyColor.Background, action = KeyboardAction.ToggleKeyboard),
            NumericConfig(".", color = KeyColor.Background),
            NumericConfig("0"),
            NumericConfig(",", color = KeyColor.Background),
            NumericConfig("Enter", color = KeyColor.Action, action = KeyboardAction.Enter, icon = lucide_ic_corner_down_left)
        )
    )
}