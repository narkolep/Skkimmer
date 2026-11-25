package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.KanaDefinition

enum class IsIgnore {
    FALSE,
    EXPLICIT, // 明示的な「っ」
    IMPLICIT, // 暗黙的な「っ」と「ん」
}

data class ConvertResult(
    val composingNext: String,
    val output: String,
    val okuriganaKey: String,
    val isIgnore: IsIgnore = IsIgnore.FALSE,
)

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
                    okuriganaKey = "t",
                    isIgnore = IsIgnore.EXPLICIT,
                )
            }

            return ConvertResult(
                composingNext = "",
                output = kana,
                okuriganaKey = composingNow.firstOrNull().toString(),
                isIgnore = IsIgnore.FALSE,
            )
        }

        /* 部分一致 */
        if (key.isNotEmpty() && KanaMap.romajiToKana.keys.any { it.startsWith(composingNow) }) {
            return ConvertResult(
                composingNext = composingNow,
                output = "",
                okuriganaKey = composingNow.firstOrNull().toString(),
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
                okuriganaKey = "t",
                isIgnore = IsIgnore.IMPLICIT,
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
                okuriganaKey = "n",
                isIgnore = IsIgnore.IMPLICIT,
            )
        }

        if (composingNow == key) break

        composingNow = composingNow.drop(1)
    }

    /* 一致なし */
    return ConvertResult(
        composingNext = "",
        output = key,
        okuriganaKey = "",
        isIgnore = IsIgnore.FALSE,
    )
}
