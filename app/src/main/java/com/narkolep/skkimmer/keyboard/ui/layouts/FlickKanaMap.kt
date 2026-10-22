package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object FlickKanaMap {
    data class Position(
        val center: String = "",    // 離した時：中央 (a など)
        val left: String = "",      // 離した時：左 (i など)
        val up: String = "",        // 離した時：上 (u など)
        val right: String = "",     // 離した時：右 (e など)
        val down: String = "",      // 離した時：下 (o など)
    ) {
        fun map(transform: (String) -> String): Position =
            copy(
                center = transform(center),
                left = transform(left),
                up = transform(up),
                right = transform(right),
                down = transform(down),
            )
    }

    data class FlickKeyConfig(
        val label: String,
        val consonant: String = "", // 子音 (k, s, など)
        val flickPosition: Position = Position(),

        val action: KeyboardAction? = null, // 機能キー
        val keyRepeat: Boolean = false, // 長押しした際のリピート
        val iconResId: Int? = null // アイコン
    )

    val flickLayout = listOf(
        /* 1段目 */
        listOf(
            FlickKeyConfig(
                label = "Q",
                flickPosition = Position(center = "q", left = "", up = "/", right = "l", down = "x"),
                iconResId = R.drawable.lucide_ic_refresh_cw
            ),
            FlickKeyConfig(
                label = "あ",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "か",
                consonant = "k",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "さ",
                consonant = "s",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
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
                label = "LA",
                action = KeyboardAction.Left,
                iconResId = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true
            ),
            FlickKeyConfig(
                label = "た",
                consonant = "t",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "な",
                consonant = "n",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "は",
                consonant = "h",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "RA",
                action = KeyboardAction.Right,
                iconResId = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true
            )
        ),

        // 3段目
        listOf(
            FlickKeyConfig(
                label = "Shift",
                action = KeyboardAction.Shift,
                iconResId = R.drawable.lucide_ic_arrow_big_up_dash
            ),
            FlickKeyConfig(
                label = "ま",
                consonant = "m",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "や",
                flickPosition = Position(center = "ya", left = "「", up = "yu", right = "」", down = "yo")
            ),
            FlickKeyConfig(
                label = "ら",
                consonant = "r",
                flickPosition = Position(center = "a", left = "i", up = "u", right = "e", down = "o")
            ),
            FlickKeyConfig(
                label = "Space",
                action = KeyboardAction.Space
            )
        ),

        // 4段目
        listOf(
            FlickKeyConfig(
                label = "Ctrl",
                action = KeyboardAction.Ctrl,
                iconResId = R.drawable.lucide_ic_cog
            ),
            FlickKeyConfig(
                label = "ﾞﾟ",
                action = KeyboardAction.Dakuten
            ),
            FlickKeyConfig(
                label = "わ",
                flickPosition = Position(center = "wa", left = "wo", up = "nn", right = "-", down = "")
            ),
            FlickKeyConfig(
                label = "､｡",
                flickPosition = Position(center = ",", left = ".", up = "?", right = "!", down = "")
            ),
            FlickKeyConfig(
                label = "Enter",
                action = KeyboardAction.Enter,
                iconResId = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}