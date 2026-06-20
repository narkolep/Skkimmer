package com.narkolep.skkimmer.keyboard.handlers

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.OutputManager
import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import com.narkolep.skkimmer.keyboard.tourokuClear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Enterキーの処理
 */
fun enterHandler(
    stateFlow: MutableStateFlow<SkkUIState>,
    inputCommitter: InputCommitter,
    dictionaryManager: DictionaryManager,
    outputManager: OutputManager,
    editorInfo: EditorInfo?
) {
    val state = stateFlow.value

    if (state.skkState == SkkState.NORMAL) {
        if (state.tourokuFlag.isNotEmpty()) {
            val tourokuText = state.tourokuFlag.substringAfter(":")
            val commitText = tourokuText.split(";")[0] + state.oldOkuriganaText
            inputCommitter.commit(commitText)

            if (tourokuText.isNotEmpty()) {
                /* ユーザー辞書として登録 */
                CoroutineScope(Dispatchers.IO).launch {
                    dictionaryManager.learnWord(
                        state.oldMidashiText + state.oldOkuriganaTrigger,
                        tourokuText,
                        false
                    )
                }

                stateFlow.update { it.tourokuClear() }
                stateFlow.update { it.clear() }
            } else {
                /* 登録モードから抜ける */
                backspaceHandler(stateFlow, inputCommitter)
            }

            return
        }

        if (state.composingText.isEmpty()) {
            val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            val inputType = editorInfo?.inputType ?: 0
            val imeOptions = editorInfo?.imeOptions ?: 0

            val isMultiLine = (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
            val noEnterAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0

            if (isMultiLine || noEnterAction) {
                /* 改行 */
                inputCommitter.commit("\n")
                return
            }
            if (action != null) {
                /* アクション実行 */
                inputCommitter.performEditorAction(action)
                return
            }
            /* fallback (改行) */
            inputCommitter.commit("\n")
            return
        }
    }

    outputManager.commit()
}