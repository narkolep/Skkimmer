package com.narkolep.skkimmer.keyboard.handlers

import android.util.Log
import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.keyboard.ActionProcessor
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.resetAll
import com.narkolep.skkimmer.keyboard.skk_lisp.SkkQuote
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Enterキーの処理
 */
internal fun ActionProcessor.handleEnter() {
    val state = stateFlow.value

    if (state.skkState == SkkState.NORMAL) {
        if (state.isRegistering) {
            if (state.registrationKey.candidate.isNotEmpty()) {
                /* 文字列の確定 */
                inputCommitter.commit(
                    text = state.registrationKey.candidate + state.oldOkuriganaText
                )

                val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    Log.e("SKK_ERROR", "Error in enterHandler learnWord", throwable)
                }

                /* concatによるエスケープ処理 */
                val candidate = SkkQuote.quoteCandidate(state.registrationKey.candidate)

                /* ユーザー辞書として登録 */
                coroutineScope.launch(Dispatchers.IO + exceptionHandler) {
                    try {
                        dictionaryManager.learnWord(
                            lookupKey = state.registrationKey.lookupKey,
                            candidate = candidate,
                            isFromSystem = false
                        )
                    } catch (e: Exception) {
                        Log.e("SKK_ERROR", "Exception learning word in enterHandler", e)
                    }
                }

                stateFlow.update { it.resetAll() }
            } else {
                /* 登録モードから抜ける */
                handleBackspace()
            }

            return
        }

        if (state.composingText.isEmpty()) {
            val imeOptions = editorInfoProvider()?.imeOptions ?: EditorInfo.IME_ACTION_NONE
            val action = imeOptions and EditorInfo.IME_MASK_ACTION
            val noEnterAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
            val hasRequestedAction = action != EditorInfo.IME_ACTION_NONE && !noEnterAction

            if (hasRequestedAction) {
                /* アクションが指定されている場合はそれを実行 */
                inputCommitter.performEditorAction(action)
            } else {
                /* 改行を挿入 */
                inputCommitter.commit("\n")
            }

            return
        }
    }

    /* 文字列の確定 */
    outputManager.commit()
}