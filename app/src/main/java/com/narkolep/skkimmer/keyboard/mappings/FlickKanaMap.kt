package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.R

object FlickKanaMap {
    data class FlickKeyConfig(
        val label: String,
        val consonant: String = "", // 子音 (k, s, など)
        val center: String = "",    // 離した時：中央 (a など)
        val left: String = "",      // 離した時：左 (i など)
        val up: String = "",        // 離した時：上 (u など)
        val right: String = "",     // 離した時：右 (e など)
        val down: String = "",      // 離した時：下 (o など)
        val action: String? = null, // 機能キー
        val iconResId: Int? = null, // アイコン
    )

    val flickLayout = listOf(
        // 1段目
        listOf(
            FlickKeyConfig("Q", "q", iconResId = R.drawable.lucide_refresh_cw),
            FlickKeyConfig("あ", "", "a", "i", "u", "e", "o"),
            FlickKeyConfig("か", "k", "a", "i", "u", "e", "o"),
            FlickKeyConfig("さ", "s", "a", "i", "u", "e", "o"),
            FlickKeyConfig("BS", action = "BACKSPACE", iconResId = R.drawable.lucide_delete)
        ),
        // 2段目
        listOf(
            FlickKeyConfig("LA", action = "LEFT", iconResId = R.drawable.lucide_chevron_left),
            FlickKeyConfig("た", "t", "a", "i", "u", "e", "o"),
            FlickKeyConfig("な", "n", "a", "i", "u", "e", "o"),
            FlickKeyConfig("は", "h", "a", "i", "u", "e", "o"),
            FlickKeyConfig("RA", action = "RIGHT", iconResId = R.drawable.lucide_chevron_right)
        ),
        // 3段目
        listOf(
            FlickKeyConfig("Shift", action = "SHIFT", iconResId = R.drawable.lucide_arrow_up),
            FlickKeyConfig("ま", "m", "a", "i", "u", "e", "o"),
            FlickKeyConfig("や", "y", "a", "「", "u", "」", "o"),
            FlickKeyConfig("ら", "r", "a", "i", "u", "e", "o"),
            FlickKeyConfig("Space", action = "SPACE")
        ),
        // 4段目
        listOf(
            FlickKeyConfig("Ctrl", action = "CTRL"),
            FlickKeyConfig("ﾞﾟ", action = "DAKUTEN"),
            FlickKeyConfig("わ", "w", "a", "o", "nn", "-", "~"),
            FlickKeyConfig("､｡", "", ",", ".", "?", "!", "…"),
            FlickKeyConfig("Enter", action = "ENTER", iconResId = R.drawable.bootstrap_enter)
        )
    )

    data class FlickConvertConfig(
        val consonantBefore: String,
        val consonantAfter: String,
        val backspace: Int = 1
    )

    val flickConvert = listOf(
        FlickConvertConfig("", "x"), // あ
        FlickConvertConfig("x", ""),
        FlickConvertConfig("v", "", 2),
        FlickConvertConfig("k", "g"), // か
        FlickConvertConfig("g", "k", 2),
        FlickConvertConfig("s", "z"), // さ
        FlickConvertConfig("z", "s", 2),
        FlickConvertConfig("t", "d"), // た
        FlickConvertConfig("xt", "d"),
        FlickConvertConfig("d", "t", 2),
        FlickConvertConfig("h", "b"), // は
        FlickConvertConfig("b", "p", 2),
        FlickConvertConfig("p", "h", 2),
        FlickConvertConfig("y", "xy"), // や
        FlickConvertConfig("xy", "y")
    )
}