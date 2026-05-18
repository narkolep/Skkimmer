package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.keyboard.KeyboardAction
import com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left
import com.composables.icons.lucide.R.drawable.lucide_ic_chevron_right
import com.composables.icons.lucide.R.drawable.lucide_ic_delete
import com.composables.icons.lucide.R.drawable.lucide_ic_space
import com.composables.icons.lucide.R.drawable.lucide_ic_corner_down_left

object NumericMap {
    data class NumericConfig(
        val label: String,
        val color: Boolean = false, // 色を変える
        val action: KeyboardAction? = null,
        val keyRepeat: Boolean = false,
        val icon: Int? = null
    )

    val numericLayout = listOf(
        listOf(
            NumericConfig(":", color = true),
            NumericConfig("1"),
            NumericConfig("2"),
            NumericConfig("3"),
            NumericConfig("BS", color = true, action = KeyboardAction.Backspace, icon = lucide_ic_delete, keyRepeat = true)
        ),

        listOf(
            NumericConfig("Left", color = true, action = KeyboardAction.Left, icon = lucide_ic_chevron_left, keyRepeat = true),
            NumericConfig("4"),
            NumericConfig("5"),
            NumericConfig("6"),
            NumericConfig("Right", color = true, action = KeyboardAction.Right, icon = lucide_ic_chevron_right, keyRepeat = true)
        ),

        listOf(
            NumericConfig("-", color = true),
            NumericConfig("7"),
            NumericConfig("8"),
            NumericConfig("9"),
            NumericConfig("Space", color = true, action = KeyboardAction.Space, icon = lucide_ic_space)
        ),

        listOf(
            NumericConfig("ABC", color = true, action = KeyboardAction.ToggleKeyboard),
            NumericConfig(".", color = true),
            NumericConfig("0"),
            NumericConfig(",", color = true),
            NumericConfig("Enter", color = true, action = KeyboardAction.Enter, icon = lucide_ic_corner_down_left)
        )
    )
}