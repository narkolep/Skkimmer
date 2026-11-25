package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.KanaDefinition

/**
 * 拡張関数
 */
fun String.isAlphabet(): Boolean = isNotEmpty() && all { it in 'a'..'z' }
fun String.isNumber(): Boolean = isNotEmpty() && all { it in '0'..'9' }


/**
 * inputModeに合わせた仮名を返す
 */
fun getOutputString(definition: KanaDefinition, inputMode: InputMode): String {
    return when (inputMode) {
        InputMode.KATAKANA -> definition.kata
        InputMode.HALF_KATAKANA -> definition.halfKata
        else -> definition.hira
    }
}


/**
 * マップを用いて仮名を変換する
 */
fun convertString(text: String, map: Map<String, String>): String {
    if (text.isEmpty() || map.isEmpty()) return text
    val sb = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        if (i + 2 <= text.length) {
            val two = text.substring(i, i + 2)
            val converted = map[two]
            if (converted != null) {
                sb.append(converted)
                i += 2
                continue
            }
        }
        val one = text.substring(i, i + 1)
        val converted = map[one]
        if (converted != null) {
            sb.append(converted)
        } else {
            sb.append(one)
        }
        i += 1
    }
    return sb.toString()
}


/**
 * 辞書検索用の形式
 * @param headword 元の見出し語
 * @param lookupKey 数字を"#"に置き換えた文字列(辞書検索用)
 * @param numbers 置き換えた数字のリスト
 */
data class NumberReplacement(
    val headword: String,
    val lookupKey: String,
    val numbers: List<String>
)

/**
 * 辞書検索用の形式に変換する
 */
fun formatString(text: String): NumberReplacement {
    /* カタカナをひらがなに直す */
    val hiragana = convertString(text, KanaMap.kataToHiraMap)

    /* 数字を"#"に置き換える */
    val numbers = mutableListOf<String>()
    val result = buildString {
        var inNumber = false
        val currentNumber = StringBuilder()

        for (char in hiragana) {
            if (char in '0'..'9') {
                currentNumber.append(char)

                if (!inNumber) {
                    append('#')
                    inNumber = true
                }
            } else {
                if (inNumber) {
                    numbers.add(currentNumber.toString())
                    currentNumber.clear()
                }

                append(char)
                inNumber = false
            }
        }

        if (inNumber) {
            numbers.add(currentNumber.toString())
        }
    }

    return NumberReplacement(
        headword = hiragana,
        lookupKey = result,
        numbers = numbers
    )
}
