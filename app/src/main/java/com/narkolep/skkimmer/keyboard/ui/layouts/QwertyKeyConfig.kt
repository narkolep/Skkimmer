package com.narkolep.skkimmer.keyboard.ui.layouts

import com.narkolep.skkimmer.keyboard.KeyboardAction

enum class KeySpacerPosition {
    NONE,
    LEFT,
    RIGHT
}

data class QwertyKeyConfig(
    val main: String,
    val flick: String = "",
    val shiftFlick: String = "",

    val action: KeyboardAction? = null,
    val keyRepeat: Boolean = false,
    val weight: Float = 1f,
    val padding: KeySpacerPosition = KeySpacerPosition.NONE,
    val iconResId: Int? = null
)