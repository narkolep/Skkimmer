package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object NumericMap {
    val numericLayout = listOf(
        /* 1段目 */
        listOf(
            FlickKeyConfig(
                label = "Toggle width",
                action = KeyboardAction.ToggleWidth,
                iconResId = R.drawable.lucide_ic_refresh_cw
            ),
            FlickKeyConfig(
                label = "1",
                flickPosition = Position(center = "1", left = "☆", up = "♪", right = "→", down = "")
            ),
            FlickKeyConfig(
                label = "2",
                flickPosition = Position(center = "2", left = "¥", up = "€", right = "$", down = "")
            ),
            FlickKeyConfig(
                label = "3",
                flickPosition = Position(center = "3", left = "%", up = "°", right = "#", down = "")
            ),
            FlickKeyConfig(
                label = "BS",
                action = KeyboardAction.Backspace,
                iconResId = R.drawable.lucide_ic_delete,
                keyRepeat = true
            )
        ),

        /* 2段目 */
        listOf(
            FlickKeyConfig(
                label = "Left arrow",
                action = KeyboardAction.Left,
                iconResId = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true
            ),
            FlickKeyConfig(
                label = "4",
                flickPosition = Position(center = "4", left = "○", up = "*", right = "・", down = "")
            ),
            FlickKeyConfig(
                label = "5",
                flickPosition = Position(center = "5", left = "+", up = "×", right = "÷", down = "")
            ),
            FlickKeyConfig(
                label = "6",
                flickPosition = Position(center = "6", left = "<", up = "=", right = ">", down = "")
            ),
            FlickKeyConfig(
                label = "Right arrow",
                action = KeyboardAction.Right,
                iconResId = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true
            )
        ),

        /* 3段目 */
        listOf(
            FlickKeyConfig(
                label = "Emoji",
                action = KeyboardAction.Emoji,
                iconResId = R.drawable.lucide_ic_apple
            ),
            FlickKeyConfig(
                label = "7",
                flickPosition = Position(center = "7", left = "「", up = ":", right = "」", down = "")
            ),
            FlickKeyConfig(
                label = "8",
                flickPosition = Position(center = "8", left = "〒", up = "々", right = "〆", down = "")
            ),
            FlickKeyConfig(
                label = "9",
                flickPosition = Position(center = "9", left = "^", up = "|", right = """\""", down = "")
            ),
            FlickKeyConfig(
                label = "Space",
                action = KeyboardAction.Space,
                iconResId = R.drawable.lucide_ic_space
            )
        ),

        /* 4段目 */
        listOf(
            FlickKeyConfig(
                label = "Back",
                action = KeyboardAction.ToggleKeyboard,
                iconResId = R.drawable.lucide_ic_earth
            ),
            FlickKeyConfig(
                label = "()[]",
                flickPosition = Position(center = "(", left = ")", up = "[", right = "]", down = "")
            ),
            FlickKeyConfig(
                label = "0",
                flickPosition = Position(center = "0", left = "~", up = "…", right = "@", down = "")
            ),
            FlickKeyConfig(
                label = ".,-/",
                flickPosition = Position(center = ".", left = ",", up = "-", right = "/", down = "")
            ),
            FlickKeyConfig(
                label = "Enter",
                action = KeyboardAction.Enter,
                iconResId = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}