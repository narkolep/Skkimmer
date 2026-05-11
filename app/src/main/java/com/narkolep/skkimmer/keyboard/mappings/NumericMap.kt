package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.R

object NumericMap {
    data class NumericConfig(
        val label: String,
        val color: Boolean = false, // 色を変える
        val action: String? = null,
        val iconResId: Int? = null
    )

    val numericLayout = listOf(
        listOf(
            NumericConfig("%", color = true),
            NumericConfig("1"),
            NumericConfig("2"),
            NumericConfig("3"),
            NumericConfig("BS", color = true, action = "BACKSPACE", iconResId = R.drawable.lucide_delete)
        ),

        listOf(
            NumericConfig("LA", color = true, action = "LEFT", iconResId = R.drawable.lucide_chevron_left),
            NumericConfig("4"),
            NumericConfig("5"),
            NumericConfig("6"),
            NumericConfig("RA", color = true, action = "RIGHT", iconResId = R.drawable.lucide_chevron_right)
        ),

        listOf(
            NumericConfig("-", color = true),
            NumericConfig("7"),
            NumericConfig("8"),
            NumericConfig("9"),
            NumericConfig("Space", color = true, action = "SPACE", iconResId = R.drawable.lucide_space)
        ),

        listOf(
            NumericConfig("ABC", color = true, action = "TOGGLE_NUMERIC"),
            NumericConfig(".", color = true),
            NumericConfig("0"),
            NumericConfig(",", color = true),
            NumericConfig("Enter", color = true, action = "ENTER", iconResId = R.drawable.bootstrap_enter)
        )
    )
}