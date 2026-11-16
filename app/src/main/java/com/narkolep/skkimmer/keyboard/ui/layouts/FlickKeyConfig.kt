package com.narkolep.skkimmer.keyboard.ui.layouts

import com.narkolep.skkimmer.keyboard.KeyboardAction

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
    val consonant: String = "", // 子音 (k, s, ...)
    val flickPosition: Position = Position(),

    val action: KeyboardAction? = null, // 機能キー
    val keyRepeat: Boolean = false, // 長押しした際のリピート
    val iconResId: Int? = null // アイコン
)