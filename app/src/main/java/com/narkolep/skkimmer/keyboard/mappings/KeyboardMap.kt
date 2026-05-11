package com.narkolep.skkimmer.keyboard.mappings

object KeyboardMap {
    data class KeyDef(
        val main: String,
        val flick: String = "",
        val shiftFlick: String = ""
    )

    val keyDefinitions = mapOf(
        // 1段目
        "1" to KeyDef("1"),
        "2" to KeyDef("2"),
        "3" to KeyDef("3"),
        "4" to KeyDef("4"),
        "5" to KeyDef("5"),
        "6" to KeyDef("6"),
        "7" to KeyDef("7"),
        "8" to KeyDef("8"),
        "9" to KeyDef("9"),
        "0" to KeyDef("0"),
        // 2段目
        "q" to KeyDef("q", "'", "'"),
        "w" to KeyDef("w", """"""", """""""),
        "e" to KeyDef("e", "「", "『"),
        "r" to KeyDef("r", "」", "』"),
        "t" to KeyDef("t", "【", "《"),
        "y" to KeyDef("y", "】", "》"),
        "u" to KeyDef("u", "〔", "‘’"),
        "i" to KeyDef("i", "〕", "°"),
        "o" to KeyDef("o", "・", "‥"),
        "p" to KeyDef("p", "…", "…"),
        // 3段目
        "a" to KeyDef("a", "!", "|"),
        "s" to KeyDef("s", "?", "*"),
        "d" to KeyDef("d", "#", "_"),
        "f" to KeyDef("f", "$", "+"),
        "g" to KeyDef("g", "%", "="),
        "h" to KeyDef("h", "&", "^"),
        "j" to KeyDef("j", "-", "~"),
        "k" to KeyDef("k", ":", ";"),
        "l" to KeyDef("l", "@", "@"),
        // 4段目
        "z" to KeyDef("z", "[", "{"),
        "x" to KeyDef("x", "]", "}"),
        "c" to KeyDef("c", "(", "<"),
        "v" to KeyDef("v", ")", ">"),
        "b" to KeyDef("b", ",", ","),
        "n" to KeyDef("n", ".", "."),
        "m" to KeyDef("m", "/", """\"""),
    )

    /* キーボードの各行の並び */
    val rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )
}