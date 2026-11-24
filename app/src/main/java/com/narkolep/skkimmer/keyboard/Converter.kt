package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.KanaDefinition

enum class IsIgnore {
    FALSE,
    XTU,
    TT_OR_N,
}

data class ConvertResult(
    val composingNext: String,
    val output: String,
    val okuriganaFlag: String,
    val isIgnore: IsIgnore = IsIgnore.FALSE,
)

/**
 * 拡張
 */
fun String.isAlphabet(): Boolean = isNotEmpty() && all { it in 'a'..'z' }
fun String.isNumber(): Boolean = isNotEmpty() && all { it in '0'..'9' }

private val ROMAJI_VOWELS = setOf('a', 'i', 'u', 'e', 'o', 'y')

/**
 * ローマ字入力の連結処理
 * @return isIgnore:
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
            /* xtu のとき */
            if (kana == "っ" || kana == "ッ") {
                return ConvertResult(
                    composingNext = "",
                    output = kana,
                    okuriganaFlag = "t",
                    isIgnore = IsIgnore.XTU,
                )
            }

            return ConvertResult(
                composingNext = "",
                output = kana,
                okuriganaFlag = composingNow.firstOrNull().toString(),
                isIgnore = IsIgnore.FALSE,
            )
        }

        /* 部分一致 */
        if (key.isNotEmpty() && KanaMap.romajiToKana.keys.any { it.startsWith(composingNow) }) {
            return ConvertResult(
                composingNext = composingNow,
                output = "",
                okuriganaFlag = composingNow.firstOrNull().toString(),
                isIgnore = IsIgnore.FALSE,
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
                isIgnore = IsIgnore.TT_OR_N,
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
                isIgnore = IsIgnore.TT_OR_N,
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
        isIgnore = IsIgnore.FALSE,
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