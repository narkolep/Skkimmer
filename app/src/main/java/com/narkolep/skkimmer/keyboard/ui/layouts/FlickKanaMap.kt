package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object FlickKanaMap {
    data class FlickKeyConfig(
        val hiraLabel: String,
        val kataLabel: String = hiraLabel,
        val halfLabel: String = hiraLabel,

        val consonant: String = "", // 子音 (k, s, など)
        val center: String = "",    // 離した時：中央 (a など)
        val left: String = "",      // 離した時：左 (i など)
        val up: String = "",        // 離した時：上 (u など)
        val right: String = "",     // 離した時：右 (e など)
        val down: String = "",      // 離した時：下 (o など)

        val action: KeyboardAction? = null, // 機能キー
        val keyRepeat: Boolean = false,     // 長押しした際のリピート
        val iconResId: Int? = null          // アイコン
    )

    val flickLayout = listOf(
        /* 1段目 */
        listOf(
            FlickKeyConfig(
                hiraLabel = "Q",
                center = "q", left = "", up = "/", right = "l", down = "x",
                iconResId = R.drawable.lucide_ic_refresh_cw
            ),
            FlickKeyConfig(
                hiraLabel = "あ",
                kataLabel = "ア",
                halfLabel = "ｱ",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "か",
                kataLabel = "カ",
                halfLabel = "ｶ",
                consonant = "k",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "さ",
                kataLabel = "サ",
                halfLabel = "ｻ",
                consonant = "s",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "BS",
                action = KeyboardAction.Backspace,
                iconResId = R.drawable.lucide_ic_delete,
                keyRepeat = true
            )
        ),
        /* 2段目 */
        listOf(
            FlickKeyConfig(
                hiraLabel = "LA",
                action = KeyboardAction.Left,
                iconResId = R.drawable.lucide_ic_chevron_left,
                keyRepeat = true
            ),
            FlickKeyConfig(
                hiraLabel = "た",
                kataLabel = "タ",
                halfLabel = "ﾀ",
                consonant = "t",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "な",
                kataLabel = "ナ",
                halfLabel = "ﾅ",
                consonant = "n",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "は",
                kataLabel = "ハ",
                halfLabel = "ﾊ",
                consonant = "h",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "RA",
                action = KeyboardAction.Right,
                iconResId = R.drawable.lucide_ic_chevron_right,
                keyRepeat = true
            )
        ),
        // 3段目
        listOf(
            FlickKeyConfig(
                hiraLabel = "Shift",
                action = KeyboardAction.Shift,
                iconResId = R.drawable.lucide_ic_arrow_big_up_dash
            ),
            FlickKeyConfig(
                hiraLabel = "ま",
                kataLabel = "マ",
                halfLabel = "ﾏ",
                consonant = "m",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "や",
                kataLabel = "ヤ",
                halfLabel = "ﾔ",
                consonant = "y",
                center = "a", left = "「", up = "u", right = "」", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "ら",
                kataLabel = "ラ",
                halfLabel = "ﾗ",
                consonant = "r",
                center = "a", left = "i", up = "u", right = "e", down = "o"
            ),
            FlickKeyConfig(
                hiraLabel = "Space",
                action = KeyboardAction.Space
            )
        ),
        // 4段目
        listOf(
            FlickKeyConfig(
                hiraLabel = "Ctrl",
                action = KeyboardAction.Ctrl,
                iconResId = R.drawable.lucide_ic_command
            ),
            FlickKeyConfig(
                hiraLabel = "ﾞﾟ",
                action = KeyboardAction.Dakuten
            ),
            FlickKeyConfig(
                hiraLabel = "わ",
                kataLabel = "ワ",
                halfLabel = "ﾜ",
                consonant = "w",
                center = "a", left = "o", up = "nn", right = "-", down = ""
            ),
            FlickKeyConfig(
                hiraLabel = "､｡",
                center = ",", left = ".", up = "？", right = "！", down = ""
            ),
            FlickKeyConfig(
                hiraLabel = "Enter",
                action = KeyboardAction.Enter,
                iconResId = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}