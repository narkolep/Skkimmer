package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

enum class KeySpacerPosition {
    NONE,
    LEFT,
    RIGHT
}

object QwertyMap {
    data class KeyDef(
        val main: String,
        val flick: String = "",
        val shiftFlick: String = "",

        val action: KeyboardAction? = null,
        val keyRepeat: Boolean = false,
        val weight: Float = 1f,
        val padding: KeySpacerPosition = KeySpacerPosition.NONE,
        val textSize: Float = 24f,
        val iconResId: Int? = null
    )

    val numericKeyDefinitions = listOf(
        // 1段目
        KeyDef("1"),
        KeyDef("2"),
        KeyDef("3"),
        KeyDef("4"),
        KeyDef("5"),
        KeyDef("6"),
        KeyDef("7"),
        KeyDef("8"),
        KeyDef("9"),
        KeyDef("0")
    )

    val keyDefinitions = listOf(
        // 2段目
        listOf(
            KeyDef("q", "'", "'"),
            KeyDef("w", """"""", """""""),
            KeyDef("e", "「", "『"),
            KeyDef("r", "」", "』"),
            KeyDef("t", "【", "《"),
            KeyDef("y", "】", "》"),
            KeyDef("u", "〔", "‘’"),
            KeyDef("i", "〕", "°"),
            KeyDef("o", "・", "‥"),
            KeyDef("p", "…", "…")
        ),

        // 3段目
        listOf(
            KeyDef("a", "!", "|", weight = 1.5f, padding = KeySpacerPosition.LEFT),
            KeyDef("s", "?", "*"),
            KeyDef("d", "#", "_"),
            KeyDef("f", "$", "+"),
            KeyDef("g", "%", "="),
            KeyDef("h", "&", "^"),
            KeyDef("j", "-", "~"),
            KeyDef("k", ":", ";"),
            KeyDef("l", "@", "@", weight = 1.5f, padding = KeySpacerPosition.RIGHT)
        ),

        // 4段目
        listOf(
            KeyDef(
                "Shift",
                action = KeyboardAction.Shift,
                weight = 1.5f,
                textSize = 18f
            ),
            KeyDef("z", "<", "{"),
            KeyDef("x", ">", "}"),
            KeyDef("c", "(", "["),
            KeyDef("v", ")", "]"),
            KeyDef("b", ",", ","),
            KeyDef("n", ".", "."),
            KeyDef("m", "/", """\"""),
            KeyDef(
                "BS",
                action = KeyboardAction.Backspace,
                weight = 1.5f,
                textSize = 20f,
                keyRepeat = true,
                iconResId = R.drawable.lucide_ic_delete
            ),
        ),

        // 5段目
        listOf(
            KeyDef(
                "Ctrl",
                action = KeyboardAction.Ctrl,
                weight = 1.5f,
                iconResId = R.drawable.lucide_ic_circle
            ),
            KeyDef(
                "Space",
                action = KeyboardAction.Space,
                weight = 5f,
                textSize = 18f
            ),
            KeyDef(
                "Left arrow",
                action = KeyboardAction.Left,
                weight = 1f,
                keyRepeat = true,
                iconResId = R.drawable.lucide_ic_chevron_left
            ),
            KeyDef(
                "Right arrow",
                action = KeyboardAction.Right,
                weight = 1f,
                keyRepeat = true,
                iconResId = R.drawable.lucide_ic_chevron_right
            ),
            KeyDef(
                "Enter",
                action = KeyboardAction.Enter,
                weight = 1.5f,
                iconResId = R.drawable.lucide_ic_corner_down_left
            )
        )
    )
}