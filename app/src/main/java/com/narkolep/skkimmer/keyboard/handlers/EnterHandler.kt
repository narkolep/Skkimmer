package com.narkolep.skkimmer.keyboard.handlers

import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.OutputManager
import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.KeyboardState
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
    stateFlow: MutableStateFlow<KeyboardState>,
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
            val imeOptions = editorInfo?.imeOptions ?: EditorInfo.IME_ACTION_NONE
            val action = imeOptions and EditorInfo.IME_MASK_ACTION
            val noEnterAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
            val hasRequestedAction = action != EditorInfo.IME_ACTION_NONE && !noEnterAction

            if (hasRequestedAction) {
                // 検索/送信/完了などのアクションが指定されている場合はそれを実行
                inputCommitter.performEditorAction(action)
            } else {
                // 改行を挿入
                inputCommitter.commit("\n")
            }

            return
        }
    }

    /* 文字列の確定 */
    outputManager.commit()
}