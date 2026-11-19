package com.narkolep.skkimmer.keyboard.handlers

import android.util.Log
import com.narkolep.skkimmer.data.CandidateResolver
import com.narkolep.skkimmer.keyboard.SkkMode
import com.narkolep.skkimmer.keyboard.OutputManager
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 辞書の検索、モード切り換え
 */
internal fun OutputManager.getCandidatesAndChangeState() {
    val state = stateFlow.value

    val result = formatString(
        text = state.currentSkkState.midashiText + state.currentSkkState.okuriganaTrigger
    )

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("SKK_ERROR", "Error in getCandidatesAndChangeState", throwable)
    }

    coroutineScope.launch(exceptionHandler) {
        try {
            val candidatesList = dictionaryManager.getCandidates(result.lookupKey)

            if (candidatesList.isNotEmpty()) {
                val candidates = CandidateResolver().resolve(
                    raws = candidatesList,
                    headword = result.headword,
                    numbers = result.numbers
                )

                stateFlow.update { it
                    .changeMode(
                        mode = SkkMode.HENKAN
                    )
                    .copy(
                        /* 変換候補 */
                        candidates = candidates,
                        selectedIndex = 0,
                        /* 状態の更新 */
                        savedSkkState = it.savedSkkState + it.currentSkkState
                    )
                }
            } else {
                stateFlow.update {
                    it.initRegistration(result.lookupKey).copy(
                        savedSkkState = it.savedSkkState + it.currentSkkState
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SKK_ERROR", "Exception in getCandidatesAndChangeState", e)
        }
    }
}

/**
 * 辞書検索用の形式に変換する
 */
data class NumberReplacement(
    val headword: String,
    val lookupKey: String,
    val numbers: List<String>
)

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
