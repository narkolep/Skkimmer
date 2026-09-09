package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object NumericMap {
    val numericLayout = listOf(
        listOf(
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "L",
                action = KeyboardAction.ToggleWidth,
                iconResId = R.drawable.lucide_ic_refresh_cw
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "1",
                center = "1", left = "", up = "", right = "", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "2",
                center = "2", left = "$", up = "", right = "￥", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "3",
                center = "3", left = "%", up = "&", right = "#", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "BS",
                action = KeyboardAction.Backspace,
                iconResId = R.drawable.lucide_ic_delete,
                keyRepeat = true
            )
        ),

        listOf(
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "Left",
                action = KeyboardAction.Left,
                iconResId = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "4",
                center = "4", left = "*", up = "", right = "・", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "5",
                center = "5", left = "+", up = "×", right = "÷", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "6",
                center = "6", left = "<", up = "=", right = ">", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "Right",
                action = KeyboardAction.Right,
                iconResId = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true
            )
        ),

        listOf(
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "Shift",
                action = KeyboardAction.Shift,
                iconResId = R.drawable.lucide_ic_arrow_big_up_dash
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "7",
                center = "7", left = "「", up = ":", right = "」", down = ";"
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "8",
                center = "8", left = "(", up = "", right = ")", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "9",
                center = "9", left = "|", up = "^", right = "", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "Space",
                action = KeyboardAction.Space,
                iconResId = R.drawable.lucide_ic_space
            )
        ),

        listOf(
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "Back",
                action = KeyboardAction.ToggleKeyboard
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "-",
                center = "-", left = "~", up = """\""", right = "/", down = "",
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "0",
                center = "0", left = "@", up = "", right = "", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = ",.",
                center = ",", left = "!", up = "?", right = ".", down = ""
            ),
            FlickKanaMap.FlickKeyConfig(
                hiraLabel = "Enter",
                action = KeyboardAction.Enter,
                iconResId = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}