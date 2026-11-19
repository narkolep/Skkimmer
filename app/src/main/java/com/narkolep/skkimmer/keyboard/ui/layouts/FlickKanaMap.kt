package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object FlickKanaMap {

    val flickLayout = listOf(
        /* 1段目 */
        listOf(
            FlickKeyConfig(
                label = "Q",
                flickPosition = Position(center = "q", left = "v", up = "/", right = "l", down = "x"),
                iconResId = R.drawable.lucide_ic_refresh_cw,
            ),
            FlickKeyConfig(
                label = "あ",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "か",
                consonant = "k",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "さ",
                consonant = "s",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "BS",
                action = KeyboardAction.Backspace,
                iconResId = R.drawable.lucide_ic_delete,
                keyRepeat = true,
            ),
        ),

        /* 2段目 */
        listOf(
            FlickKeyConfig(
                label = "Left arrow",
                action = KeyboardAction.Left,
                iconResId = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true,
            ),
            FlickKeyConfig(
                label = "た",
                consonant = "t",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "な",
                consonant = "n",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "は",
                consonant = "h",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "Right arrow",
                action = KeyboardAction.Right,
                iconResId = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true,
            ),
        ),

        // 3段目
        listOf(
            FlickKeyConfig(
                label = "Shift",
                action = KeyboardAction.Shift,
                iconResId = R.drawable.lucide_ic_arrow_big_up_dash,
            ),
            FlickKeyConfig(
                label = "ま",
                consonant = "m",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "や",
                flickPosition = Position(center = "ya", left = "「", up = "yu", right = "」", down = "yo"),
            ),
            FlickKeyConfig(
                label = "ら",
                consonant = "r",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o"),
            ),
            FlickKeyConfig(
                label = "Space",
                action = KeyboardAction.Space,
            ),
        ),

        // 4段目
        listOf(
            FlickKeyConfig(
                label = "Ctrl",
                action = KeyboardAction.Ctrl,
            ),
            FlickKeyConfig(
                label = "ﾞﾟ",
                action = KeyboardAction.Dakuten,
            ),
            FlickKeyConfig(
                label = "わ",
                flickPosition = Position(center = "wa", left = "wo", up = "nn", right = "-", down = "z-"),
            ),
            FlickKeyConfig(
                label = "､｡",
                flickPosition = Position(center = ",", left = ".", up = "？", right = "！", down = "z."),
            ),
            FlickKeyConfig(
                label = "Enter",
                action = KeyboardAction.Enter,
                iconResId = R.drawable.lucide_ic_corner_down_left,
            ),
        )
    )
}