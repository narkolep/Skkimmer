package com.narkolep.skkimmer.keyboard.handlers

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.data.SkkDictionaryManager
import com.narkolep.skkimmer.keyboard.ComposingManager
import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.clear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnterHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val editorInfo: EditorInfo?,
    private val inputCommitter: InputCommitter,
    private val dictionaryManager: SkkDictionaryManager,
    private val composingManager: ComposingManager
) {
    fun handle() {
        val state = stateFlow.value

        if (state.skkState == SkkState.NORMAL) {
            if (state.tourokuFlag.isNotEmpty()) {
                val textToCommit = state.tourokuFlag.split(":")[1]
                inputCommitter.commit(textToCommit.replace("*", ""))

                /* ユーザー辞書として登録 */
                val learnText = textToCommit.split("*")[0]
                if (learnText.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        dictionaryManager.learnWord(
                            state.tourokuText,
                            learnText,
                            false
                        )
                        stateFlow.update {
                            it.copy(
                                tourokuText = "",
                                tourokuFlag = ""
                            )
                        }
                    }
                }

                stateFlow.update {
                    it.clear()
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

        composingManager.commit()
    }
}