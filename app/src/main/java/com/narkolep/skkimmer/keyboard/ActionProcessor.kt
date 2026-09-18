package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.handlers.backspaceHandler
import com.narkolep.skkimmer.keyboard.handlers.ctrlHandler
import com.narkolep.skkimmer.keyboard.handlers.dakutenHandler
import com.narkolep.skkimmer.keyboard.handlers.enterHandler
import com.narkolep.skkimmer.keyboard.handlers.shiftHandler
import com.narkolep.skkimmer.keyboard.handlers.spaceHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class KeyboardAction {
    object Shift : KeyboardAction()
    object Ctrl : KeyboardAction()
    object Space : KeyboardAction()
    object Backspace : KeyboardAction()
    object Enter : KeyboardAction()
    object ToggleKeyboard : KeyboardAction()
    object ToggleWidth : KeyboardAction()
    object Emoji : KeyboardAction()
    object Left : KeyboardAction()
    object Right : KeyboardAction()
    object Dakuten : KeyboardAction()
    data class CandidateIndex(
        val index: Int
    ) : KeyboardAction()
}

class ActionProcessor(
    private val stateFlow: MutableStateFlow<KeyboardState>,
    private val outputManager: OutputManager,
    private val inputCommitter: InputCommitter,
    private val keyProcessor: KeyProcessor,
    private val dictionaryManager: DictionaryManager,
    private val editorInfo: EditorInfo?
) {
    /**
     * アクションキーの分岐
     */
    fun handle(action: KeyboardAction) {
        val state = stateFlow.value

        when(action) {
            KeyboardAction.Shift -> {
                shiftHandler(stateFlow)
            }
            KeyboardAction.Ctrl -> {
                ctrlHandler(stateFlow)
            }
            KeyboardAction.Space -> {
                spaceHandler(stateFlow, dictionaryManager, keyProcessor)
            }
            KeyboardAction.Backspace -> {
                backspaceHandler(stateFlow, inputCommitter)
            }
            KeyboardAction.Enter -> {
                enterHandler(stateFlow, inputCommitter, dictionaryManager, outputManager, editorInfo)
            }
            KeyboardAction.ToggleKeyboard -> {
                stateFlow.update { it.copy(
                    keyboardType = KeyboardType.NORMAL,
                    inputMode = InputMode.HIRAGANA
                ) }
            }
            KeyboardAction.ToggleWidth -> {
                stateFlow.update { it.copy(
                    inputMode =
                        if (state.inputMode == InputMode.FULL_ASCII) InputMode.HALF_ASCII
                        else InputMode.FULL_ASCII
                ) }
            }
            KeyboardAction.Emoji -> {
                stateFlow.update { it.copy(keyboardType = KeyboardType.EMOJI) }
            }
            KeyboardAction.Left -> {
                if (stateFlow.value.skkState == SkkState.HENKAN) {
                    keyProcessor.handle("x")
                    return
                }

                inputCommitter.moveCursor(-1)
            }
            KeyboardAction.Right -> {
                if (stateFlow.value.skkState == SkkState.HENKAN) {
                    spaceHandler(stateFlow, dictionaryManager, keyProcessor)
                    return
                }

                inputCommitter.moveCursor(1)
            }
            KeyboardAction.Dakuten -> {
                // カーソルを末尾に移動してから濁点処理を実行する
                outputManager.update()
                dakutenHandler(stateFlow, inputCommitter, keyProcessor)
            }
            is KeyboardAction.CandidateIndex -> {
                val index = action.index
                stateFlow.update { it.copy(selectedIndex = index) }
                outputManager.commit()
            }
        }
    }
}