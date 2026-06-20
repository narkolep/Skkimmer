package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 辞書の検索、モード切り換え
 */
fun getCandidatesAndChangeState(
    stateFlow: MutableStateFlow<SkkUIState>,
    dictionaryManager: DictionaryManager
) {
    val state = stateFlow.value

    /* カタカナはひらがなに直す */
    val convertedMidashiText =
        if (state.inputMode == InputMode.KATAKANA) convertString(state.midashiText, KanaMap.kataToHiraMap)
        else state.midashiText

    val keyText =
        if (state.skkState == SkkState.OKURIGANA) convertedMidashiText + state.okuriganaTrigger
        else convertedMidashiText

    CoroutineScope(Dispatchers.Main).launch {
        val candidatesList = dictionaryManager.getCandidates(keyText)

        if (candidatesList.isNotEmpty()) {
            stateFlow.update {
                it.copy(
                    midashiText = convertedMidashiText,
                    skkState = SkkState.HENKAN,
                    candidates = candidatesList,
                    selectedIndex = 0
                )
            }
        } else if (state.tourokuFlag.isEmpty()) {
            stateFlow.update { it.clear() }

            stateFlow.update {
                it.copy(
                    oldMidashiText = convertedMidashiText,
                    oldOkuriganaText = state.okuriganaText,
                    oldOkuriganaTrigger = state.okuriganaTrigger,
                    tourokuFlag = "[登録]$keyText:"
                )
            }
        }
    }
}

/**
 * 仮名の変換
 */
fun convertString(text: String, map: Map<String, String>): String {
    var result = text
    map.forEach { (key, value) ->
        result = result.replace(key, value)
    }
    return result
}