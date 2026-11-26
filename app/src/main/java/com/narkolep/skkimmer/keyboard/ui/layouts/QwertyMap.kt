package com.narkolep.skkimmer.keyboard.ui.layouts

import com.composables.icons.lucide.R
import com.narkolep.skkimmer.keyboard.KeyboardAction

object QwertyMap {

    val numericQwertyKeyLayout = listOf(
        // 1段目
        QwertyKeyConfig("1"),
        QwertyKeyConfig("2"),
        QwertyKeyConfig("3"),
        QwertyKeyConfig("4"),
        QwertyKeyConfig("5"),
        QwertyKeyConfig("6"),
        QwertyKeyConfig("7"),
        QwertyKeyConfig("8"),
        QwertyKeyConfig("9"),
        QwertyKeyConfig("0"),
    )

    val qwertyKeyLayout = listOf(
        // 2段目
        listOf(
            QwertyKeyConfig("q", "'", "‘’"),
            QwertyKeyConfig("w", """"""", "“”"),
            QwertyKeyConfig("e", "「", "『"),
            QwertyKeyConfig("r", "」", "』"),
            QwertyKeyConfig("t", "【", "〖"),
            QwertyKeyConfig("y", "】", "〗"),
            QwertyKeyConfig("u", "〔", "《"),
            QwertyKeyConfig("i", "〕", "》"),
            QwertyKeyConfig("o", "^", "~"),
            QwertyKeyConfig("p", "|", "*"),
        ),

        // 3段目
        listOf(
            QwertyKeyConfig("a", "!", "¡", weight = 1.5f, padding = KeySpacerPosition.LEFT),
            QwertyKeyConfig("s", "?", "¿"),
            QwertyKeyConfig("d", "#", "_"),
            QwertyKeyConfig("f", "$", "+"),
            QwertyKeyConfig("g", "%", "・"),
            QwertyKeyConfig("h", "&", "—"),
            QwertyKeyConfig("j", "-", "="),
            QwertyKeyConfig("k", ":", ";"),
            QwertyKeyConfig("l", "@", "\u00A5", weight = 1.5f, padding = KeySpacerPosition.RIGHT),
        ),

        // 4段目
        listOf(
            QwertyKeyConfig(
                "Shift",
                action = KeyboardAction.Shift,
                weight = 1.5f,
            ),
            QwertyKeyConfig("z", "<", "{"),
            QwertyKeyConfig("x", ">", "}"),
            QwertyKeyConfig("c", "(", "["),
            QwertyKeyConfig("v", ")", "]"),
            QwertyKeyConfig("b", ",", "‥"),
            QwertyKeyConfig("n", ".", "…"),
            QwertyKeyConfig("m", "/", "\u005C"),
            QwertyKeyConfig(
                "BS",
                action = KeyboardAction.Backspace,
                weight = 1.5f,
                keyRepeat = true,
                iconResId = R.drawable.lucide_ic_delete,
            ),
        ),

        // 5段目
        listOf(
            QwertyKeyConfig(
                "Ctrl",
                action = KeyboardAction.Ctrl,
                weight = 1.5f,
            ),
            QwertyKeyConfig(
                "Space",
                action = KeyboardAction.Space,
                weight = 5f,
            ),
            QwertyKeyConfig(
                "Left arrow",
                action = KeyboardAction.Left,
                weight = 1f,
                keyRepeat = true,
                iconResId = R.drawable.lucide_ic_chevron_left,
            ),
            QwertyKeyConfig(
                "Right arrow",
                action = KeyboardAction.Right,
                weight = 1f,
                keyRepeat = true,
                iconResId = R.drawable.lucide_ic_chevron_right,
            ),
            QwertyKeyConfig(
                "Enter",
                action = KeyboardAction.Enter,
                weight = 1.5f,
                iconResId = R.drawable.lucide_ic_corner_down_left,
            ),
        )
    )
}