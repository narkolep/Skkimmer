package com.narkolep.skkimmer.keyboard.handlers

import android.util.Log
import com.narkolep.skkimmer.data.CandidateResolver
import com.narkolep.skkimmer.keyboard.SkkMode
import com.narkolep.skkimmer.keyboard.OutputManager
import com.narkolep.skkimmer.keyboard.formatString
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 辞書の検索、モード切り換え
 * @param isIgnore true の場合、モード切り換えを行なわない
 */
internal fun OutputManager.getCandidatesAndChangeState(
    isIgnore: Boolean = false
) {
    val state = stateFlow.value

    val result = formatString(
        text = state.currentSkkState.midashiText + state.currentSkkState.okuriganaKey
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
                        /* モード切り換え */
                        mode = if (isIgnore) it.currentSkkState.skkMode else SkkMode.HENKAN
                    )
                    .copy(
                        /* 変換候補の保存 */
                        candidates = candidates,
                        selectedIndex = 0,
                        /* 状態の更新 */
                        savedSkkState = it.savedSkkState + it.currentSkkState
                    )
                }
            } else {
                /* 辞書登録モードに入る */
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
