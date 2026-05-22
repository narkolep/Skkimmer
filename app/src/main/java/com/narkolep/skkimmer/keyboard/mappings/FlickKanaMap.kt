package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.keyboard.KeyboardAction
import com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left
import com.composables.icons.lucide.R.drawable.lucide_ic_chevron_right
import com.composables.icons.lucide.R.drawable.lucide_ic_delete
import com.composables.icons.lucide.R.drawable.lucide_ic_corner_down_left
import com.composables.icons.lucide.R.drawable.lucide_ic_refresh_cw
import com.composables.icons.lucide.R.drawable.lucide_ic_arrow_big_up_dash
import com.composables.icons.lucide.R.drawable.lucide_ic_command

object FlickKanaMap {
    data class FlickKeyConfig(
        val label: String,
        val consonant: String = "", // 子音 (k, s, など)
        val center: String = "",    // 離した時：中央 (a など)
        val left: String = "",      // 離した時：左 (i など)
        val up: String = "",        // 離した時：上 (u など)
        val right: String = "",     // 離した時：右 (e など)
        val down: String = "",      // 離した時：下 (o など)
        val action: KeyboardAction? = null, // 機能キー
        val keyRepeat: Boolean = false,     // 長押しした際のリピート
        val iconResId: Int? = null,         // アイコン
    )

    val flickLayout = listOf(
        // 1段目
        listOf(
            FlickKeyConfig("Q", "", "q", "", "/", "l", "x", iconResId = lucide_ic_refresh_cw),
            FlickKeyConfig("あ", "", "a", "i", "u", "e", "o"),
            FlickKeyConfig("か", "k", "a", "i", "u", "e", "o"),
            FlickKeyConfig("さ", "s", "a", "i", "u", "e", "o"),
            FlickKeyConfig("BS", action = KeyboardAction.Backspace, iconResId = lucide_ic_delete, keyRepeat = true)
        ),
        // 2段目
        listOf(
            FlickKeyConfig("LA", action = KeyboardAction.Left, iconResId = lucide_ic_chevron_left, keyRepeat = true),
            FlickKeyConfig("た", "t", "a", "i", "u", "e", "o"),
            FlickKeyConfig("な", "n", "a", "i", "u", "e", "o"),
            FlickKeyConfig("は", "h", "a", "i", "u", "e", "o"),
            FlickKeyConfig("RA", action = KeyboardAction.Right, iconResId = lucide_ic_chevron_right, keyRepeat = true)
        ),
        // 3段目
        listOf(
            FlickKeyConfig("Shift", action = KeyboardAction.Shift, iconResId = lucide_ic_arrow_big_up_dash),
            FlickKeyConfig("ま", "m", "a", "i", "u", "e", "o"),
            FlickKeyConfig("や", "y", "a", "「", "u", "」", "o"),
            FlickKeyConfig("ら", "r", "a", "i", "u", "e", "o"),
            FlickKeyConfig("Space", action = KeyboardAction.Space)
        ),
        // 4段目
        listOf(
            FlickKeyConfig("Ctrl", action = KeyboardAction.Ctrl, iconResId = lucide_ic_command),
            FlickKeyConfig("ﾞﾟ", action = KeyboardAction.Dakuten),
            FlickKeyConfig("わ", "w", "a", "o", "nn", "-"),
            FlickKeyConfig("､｡", "", ",", ".", "?", "!",),
            FlickKeyConfig("Enter", action = KeyboardAction.Enter, iconResId = lucide_ic_corner_down_left)
        )
    )

    data class FlickConvertConfig(
        val consonantBefore: String,
        val consonantAfter: String
    )

    val flickConvert = listOf(
        FlickConvertConfig("", "x"), // あ
        FlickConvertConfig("x", ""),
        FlickConvertConfig("v", ""),
        FlickConvertConfig("k", "g"), // か
        FlickConvertConfig("g", "k"),
        FlickConvertConfig("s", "z"), // さ
        FlickConvertConfig("sh", "z"),
        FlickConvertConfig("z", "s"),
        FlickConvertConfig("j", "s"),
        FlickConvertConfig("t", "d"), // た
        FlickConvertConfig("ch", "d"),
        FlickConvertConfig("xt", "d"),
        FlickConvertConfig("d", "t"),
        FlickConvertConfig("h", "b"), // は
        FlickConvertConfig("f", "b"),
        FlickConvertConfig("b", "p"),
        FlickConvertConfig("p", "h"),
        FlickConvertConfig("y", "xy"), // や
        FlickConvertConfig("xy", "y")
    )
}