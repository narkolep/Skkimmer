package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object NumericMap {
    enum class KeyColor {
        Background,
        Button,
        Action
    }

    data class NumericConfig(
        val label: String,

        val center: String = "",    // 中央
        val left: String = "",      // 左
        val up: String = "",        // 上
        val right: String = "",     // 右
        val down: String = "",      // 下

        val color: KeyColor = KeyColor.Button,
        val action: KeyboardAction? = null,
        val keyRepeat: Boolean = false,
        val icon: Int? = null
    )

    val numericLayout = listOf(
        listOf(
            NumericConfig(
                label = "Toggle",
                color = KeyColor.Background,
                icon = R.drawable.lucide_ic_refresh_cw
            ),
            NumericConfig(
                label = "1",
                center = "1", left = "", up = "", right = "", down = ""
            ),
            NumericConfig(
                label = "2",
                center = "2", left = "$", up = "", right = "￥", down = ""
            ),
            NumericConfig(
                label = "3",
                center = "3", left = "%", up = "&", right = "#", down = ""
            ),
            NumericConfig(
                label = "BS",
                color = KeyColor.Background,
                action = KeyboardAction.Backspace,
                icon = R.drawable.lucide_ic_delete,
                keyRepeat = true
            )
        ),

        listOf(
            NumericConfig(
                label = "Left",
                color = KeyColor.Background,
                action = KeyboardAction.Left,
                icon = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true
            ),
            NumericConfig(
                label = "4",
                center = "4", left = "*", up = "", right = "・", down = ""
            ),
            NumericConfig(
                label = "5",
                center = "5", left = "+", up = "×", right = "÷", down = ""
            ),
            NumericConfig(
                label = "6",
                center = "6", left = "<", up = "=", right = ">", down = ""
            ),
            NumericConfig(
                label = "Right",
                color = KeyColor.Background,
                action = KeyboardAction.Right,
                icon = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true
            )
        ),

        listOf(
            NumericConfig(
                label = "Shift",
                color = KeyColor.Background,
                action = KeyboardAction.Shift,
                icon = R.drawable.lucide_ic_arrow_big_up_dash
            ),
            NumericConfig(
                label = "7",
                center = "7", left = "「", up = ":", right = "」", down = ";"
            ),
            NumericConfig(
                label = "8",
                center = "8", left = "(", up = "", right = ")", down = ""
            ),
            NumericConfig(
                label = "9",
                center = "9", left = "|", up = "^", right = "", down = ""
            ),
            NumericConfig(
                label = "Space",
                color = KeyColor.Background,
                action = KeyboardAction.Space,
                icon = R.drawable.lucide_ic_space
            )
        ),

        listOf(
            NumericConfig(
                label = "Back",
                color = KeyColor.Background,
                action = KeyboardAction.ToggleKeyboard
            ),
            NumericConfig(
                label = "-~",
                center = "-", left = "~", up = """\""", right = "/", down = "",
                color = KeyColor.Background
            ),
            NumericConfig(
                label = "0",
                center = "0", left = "@", up = "", right = "", down = ""
            ),
            NumericConfig(
                label = ",.",
                center = ",", left = "!", up = "?", right = ".", down = "",
                color = KeyColor.Background
            ),
            NumericConfig(
                label = "Enter",
                color = KeyColor.Action,
                action = KeyboardAction.Enter,
                icon = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}