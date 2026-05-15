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
 **/
class Converter {
    fun convert(
        composing: String,
        key: String,
        inputMode: InputMode
    ): ConvertResult {
        /* 母音 + y */
        val vowels = setOf('a','i','u','e','o','y')
        /* アルファベットか判定する */
        val isAlphabet = key.matches(Regex("^[a-z0-9]+$"))
        /* 現在の未確定文字列 */
        val composingNow = composing + key

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

        /* composingNowが一致しない、かつkeyが完全一致の場合 */
        if (KanaMap.romajiToKana.containsKey(key)) {
            val kana = getOutputString(
                KanaMap.romajiToKana[key]!!,
                inputMode
            )

            return ConvertResult(
                composingNext = "",
                output = kana,
                okuriganaFlag = key,
                isIgnore = false
            )
        }

        /* composingNowが一致しない、かつkeyIdが部分一致の場合 */
        if (key.isNotEmpty() && KanaMap.romajiToKana.keys.any { it.startsWith(key) }) {
            return ConvertResult(
                composingNext = key,
                output = "",
                okuriganaFlag = key,
                isIgnore = false
            )
        }

        /* 一致なし */
        return ConvertResult(
            composingNext = "",
            output = composingNow,
            okuriganaFlag = "",
            isIgnore = false
        )
    }

    private fun getOutputString(definition: KanaDefinition, inputMode: InputMode): String {
        return when (inputMode) {
            InputMode.KATAKANA -> definition.kata
            InputMode.HALF_KATAKANA -> definition.halfkata
            else -> definition.hira
        }
    }
}