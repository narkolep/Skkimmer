package com.narkolep.skkimmer.keyboard.handlers

import android.util.Log
import com.narkolep.skkimmer.data.CandidateResolver
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.data.toRawCandidates
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.clear
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 辞書の検索、モード切り換え
 */
fun getCandidatesAndChangeState(
    stateFlow: MutableStateFlow<KeyboardState>,
    dictionaryManager: DictionaryManager,
    coroutineScope: CoroutineScope
) {
    val state = stateFlow.value

    /* カタカナはひらがなに直す */
    val convertedMidashiText =
        if (state.inputMode == InputMode.KATAKANA) convertString(state.midashiText, KanaMap.kataToHiraMap)
        else state.midashiText

    val keyText =
        if (state.skkState == SkkState.OKURIGANA) convertedMidashiText + state.okuriganaTrigger
        else convertedMidashiText

    /* 数字を"#"に置き換える */
    val result = replaceNumber(keyText)

    val resolver = CandidateResolver()

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("SKK_ERROR", "Error in getCandidatesAndChangeState", throwable)
    }

    coroutineScope.launch(exceptionHandler) {
        try {
            val candidatesList = dictionaryManager.getCandidates(result.text)

            if (candidatesList.isNotEmpty()) {
                val candidates = resolver.resolve(
                    raws = candidatesList.toRawCandidates(),
                    headword = keyText,
                    numbers = result.numbers
                )

                stateFlow.update {
                    it.copy(
                        midashiText = convertedMidashiText,
                        skkState = SkkState.HENKAN,
                        candidates = candidates,
                        selectedIndex = 0,
                        previousSkkState = it.skkState
                    )
                }
            } else if (state.tourokuFlag.isEmpty()) {
                stateFlow.update {
                    it.clear().copy(
                        oldSkkState = it.skkState,
                        oldMidashiText = convertedMidashiText,
                        oldOkuriganaText = it.okuriganaText,
                        oldOkuriganaTrigger = it.okuriganaTrigger,
                        tourokuFlag = "[登録]${result.text}:"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SKK_ERROR", "Exception in getCandidatesAndChangeState", e)
        }
    }
}

/**
 * 仮名の変換
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

private data class NumberReplacement(
    val text: String,
    val numbers: List<String>
)

/**
 * 数字を"#"に置き換える
 * "#"に置き換えられた数字のリストも返す
 */
private fun replaceNumber(text: String): NumberReplacement {
    val numbers = mutableListOf<String>()

    val result = buildString {
        var inNumber = false
        val currentNumber = StringBuilder()

        for (char in text) {
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

    return NumberReplacement(result, numbers)
}