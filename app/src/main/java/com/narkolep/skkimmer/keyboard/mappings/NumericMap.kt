package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object NumericMap {
    data class NumericConfig(
        val label: String,
        val color: Boolean = false, // 色を変える
        val action: KeyboardAction? = null,
        val iconResId: Int? = null
    )

    val numericLayout = listOf(
        listOf(
            NumericConfig(":", color = true),
            NumericConfig("1"),
            NumericConfig("2"),
            NumericConfig("3"),
            NumericConfig("BS", color = true, action = KeyboardAction.Backspace, iconResId = R.drawable.lucide_delete)
        ),

        listOf(
            NumericConfig("LA", color = true, action = KeyboardAction.Left, iconResId = R.drawable.lucide_chevron_left),
            NumericConfig("4"),
            NumericConfig("5"),
            NumericConfig("6"),
            NumericConfig("RA", color = true, action = KeyboardAction.Right, iconResId = R.drawable.lucide_chevron_right)
        ),

        listOf(
            NumericConfig("-", color = true),
            NumericConfig("7"),
            NumericConfig("8"),
            NumericConfig("9"),
            NumericConfig("Space", color = true, action = KeyboardAction.Space, iconResId = R.drawable.lucide_space)
        ),

        listOf(
            NumericConfig("ABC", color = true, action = KeyboardAction.ToggleKeyboard),
            NumericConfig(".", color = true),
            NumericConfig("0"),
            NumericConfig(",", color = true),
            NumericConfig("Enter", color = true, action = KeyboardAction.Enter, iconResId = R.drawable.bootstrap_enter)
        )
    )
}