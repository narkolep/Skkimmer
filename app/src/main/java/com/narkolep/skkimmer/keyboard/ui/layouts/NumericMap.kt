package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object NumericMap {
    val numericLayout = listOf(
        /* 1段目 */
        listOf(
            FlickKanaMap.FlickKeyConfig(
                label = "L",
                action = KeyboardAction.ToggleWidth,
                iconResId = R.drawable.lucide_ic_refresh_cw
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "1",
                flickPosition = FlickKanaMap.Position(center = "1", left = "☆", up = "♪", right = "→", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "2",
                flickPosition = FlickKanaMap.Position(center = "2", left = "¥", up = "€", right = "$", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "3",
                flickPosition = FlickKanaMap.Position(center = "3", left = "%", up = "°", right = "#", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "BS",
                action = KeyboardAction.Backspace,
                iconResId = R.drawable.lucide_ic_delete,
                keyRepeat = true
            )
        ),

        /* 2段目 */
        listOf(
            FlickKanaMap.FlickKeyConfig(
                label = "Left",
                action = KeyboardAction.Left,
                iconResId = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "4",
                flickPosition = FlickKanaMap.Position(center = "4", left = "○", up = "*", right = "・", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "5",
                flickPosition = FlickKanaMap.Position(center = "5", left = "+", up = "×", right = "÷", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "6",
                flickPosition = FlickKanaMap.Position(center = "6", left = "<", up = "=", right = ">", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "Right",
                action = KeyboardAction.Right,
                iconResId = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true
            )
        ),

        /* 3段目 */
        listOf(
            FlickKanaMap.FlickKeyConfig(
                label = "Emoji",
                action = KeyboardAction.Emoji,
                iconResId = R.drawable.lucide_ic_apple
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "7",
                flickPosition = FlickKanaMap.Position(center = "7", left = "「", up = ":", right = "」", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "8",
                flickPosition = FlickKanaMap.Position(center = "8", left = "〒", up = "々", right = "〆", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "9",
                flickPosition = FlickKanaMap.Position(center = "9", left = "^", up = "|", right = """\""", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "Space",
                action = KeyboardAction.Space,
                iconResId = R.drawable.lucide_ic_space
            )
        ),

        /* 4段目 */
        listOf(
            FlickKanaMap.FlickKeyConfig(
                label = "Back",
                action = KeyboardAction.ToggleKeyboard,
                iconResId = R.drawable.lucide_ic_earth
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "()[]",
                flickPosition = FlickKanaMap.Position(center = "(", left = ")", up = "[", right = "]", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "0",
                flickPosition = FlickKanaMap.Position(center = "0", left = "~", up = "…", right = "@", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = ".,-/",
                flickPosition = FlickKanaMap.Position(center = ".", left = ",", up = "-", right = "/", down = "")
            ),
            FlickKanaMap.FlickKeyConfig(
                label = "Enter",
                action = KeyboardAction.Enter,
                iconResId = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}