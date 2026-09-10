package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.KanaDefinition
import kotlin.collections.contains

data class ConvertResult(
    val composingNext: String,
    val output: String,
    val okuriganaFlag: String,
    val isIgnore: Boolean
)

/**
 * ローマ字入力の連結処理
 */
fun romajiConverter(
    composing: String,
    key: String,
    inputMode: InputMode
): ConvertResult {
    /* 母音 + y */
    val vowels = setOf('a','i','u','e','o','y')
    /* アルファベットか判定する */
    val isAlphabet = key.matches(Regex("^[a-z0-9]+$"))
    /* 現在の未確定文字列 */
    var composingNow: String = composing + key

    /* マップに一致するまで、先頭から順に削除する */
    while (true) {
        /* 完全一致 */
        if (KanaMap.romajiToKana.containsKey(composingNow)) {
            val kana = getOutputString(
                KanaMap.romajiToKana[composingNow]!!,
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
        if (isAlphabet && composing == key) {
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
        if (composing == "n" && key.firstOrNull() !in vowels) {
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
private fun getOutputString(definition: KanaDefinition, inputMode: InputMode): String {
    return when (inputMode) {
        InputMode.KATAKANA -> definition.kata
        InputMode.HALF_KATAKANA -> definition.halfkata
        else -> definition.hira
    }
}