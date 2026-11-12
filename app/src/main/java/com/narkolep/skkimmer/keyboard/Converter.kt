package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.KanaDefinition

data class ConvertResult(
    val composingNext: String,
    val output: String,
    val okuriganaFlag: String,
    val isIgnore: Boolean
)

/**
 * 拡張
 */
fun String.isAlphabet(): Boolean = isNotEmpty() && all { it in 'a'..'z' }
fun String.isNumber(): Boolean = isNotEmpty() && all { it in '0'..'9' }

private val ROMAJI_VOWELS = setOf('a', 'i', 'u', 'e', 'o', 'y')

/**
 * ローマ字入力の連結処理
 */
fun romajiConverter(
    composing: String,
    key: String,
    inputMode: InputMode
): ConvertResult {
    /* 現在の未確定文字列 */
    var composingNow: String = composing + key

    /* マップに一致するまで、先頭から順に削除する */
    while (true) {
        /* 完全一致 */
        val matchedKana = KanaMap.romajiToKana[composingNow]
        if (matchedKana != null) {
            val kana = getOutputString(
                matchedKana,
                inputMode
            )
            val flag =
                if (composingNow.firstOrNull() == 'x') composingNow.getOrElse(1) { 'x' }.toString()
                else composingNow.firstOrNull().toString()

            return ConvertResult(
                composingNext = "",
                output = kana,
                okuriganaFlag = flag,
                isIgnore = composingNow.firstOrNull() == 'x'
            )
        }

        /* 部分一致 */
        if (key.isNotEmpty() && KanaMap.romajiToKana.keys.any { it.startsWith(composingNow) }) {
            return ConvertResult(
                composingNext = composingNow,
                output = "",
                okuriganaFlag = composingNow.firstOrNull().toString(),
                isIgnore = false
            )
        }

        /* 促音 */
        if (key.isAlphabet() && composing == key) {
            val kana = getOutputString(
                KanaDefinition("っ", "ッ", "ｯ"),
                inputMode
            )

            return ConvertResult(
                composingNext = key,
                output = kana,
                okuriganaFlag = "t",
                isIgnore = true
            )
        }

        /* 撥音 */
        if (composing == "n" && key.firstOrNull() !in ROMAJI_VOWELS) {
            val kana = getOutputString(
                KanaDefinition("ん", "ン", "ﾝ"),
                inputMode
            )

            return ConvertResult(
                composingNext = key,
                output = kana,
                okuriganaFlag = "n",
                isIgnore = true
            )
        }

        if (composingNow == key) break

        composingNow = composingNow.drop(1)
    }

    /* 一致なし */
    return ConvertResult(
        composingNext = "",
        output = key,
        okuriganaFlag = "",
        isIgnore = false
    )
}

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