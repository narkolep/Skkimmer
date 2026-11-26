package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.handlers.handleBackspace
import com.narkolep.skkimmer.keyboard.handlers.handleDakuten
import com.narkolep.skkimmer.keyboard.handlers.handleEnter
import com.narkolep.skkimmer.keyboard.handlers.handleSpace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class ActionProcessor(
    internal val stateFlow: MutableStateFlow<KeyboardState>,
    internal val outputManager: OutputManager,
    internal val inputCommitter: InputCommitter,
    internal val keyProcessor: KeyProcessor,
    internal val dictionaryManager: DictionaryManager,
    internal val editorInfoProvider: () -> EditorInfo?,
    internal val coroutineScope: CoroutineScope
) {

    /**
     * アクションキーの分岐
     */
    fun handle(action: KeyboardAction) {
        when(action) {
            KeyboardAction.Shift -> {
                stateFlow.update { it.processShift() }
            }
            KeyboardAction.Ctrl -> {
                stateFlow.update { it.copy(isCtrlPressed = !it.isCtrlPressed) }
            }
            KeyboardAction.Space -> handleSpace()
            KeyboardAction.Backspace -> handleBackspace()
            KeyboardAction.Enter -> handleEnter()
            KeyboardAction.ToggleKeyboard -> {
                stateFlow.update { it.copy(
                    keyboardType = KeyboardType.NORMAL,
                    inputMode = InputMode.HIRAGANA
                ) }
            }
            KeyboardAction.ToggleWidth -> {
                stateFlow.update { it.copy(
                    inputMode =
                        if (it.inputMode == InputMode.FULL_ASCII) InputMode.HALF_ASCII
                        else InputMode.FULL_ASCII
                ) }
            }
            KeyboardAction.ToggleCandidate -> {
                stateFlow.update { it.copy(
                    keyboardType =
                        if (it.keyboardType == KeyboardType.CANDIDATE) KeyboardType.NORMAL
                        else KeyboardType.CANDIDATE
                ) }
            }
            KeyboardAction.Emoji -> {
                stateFlow.update { it.copy(keyboardType = KeyboardType.EMOJI) }
            }
            KeyboardAction.Left -> {
                if (stateFlow.value.candidates.isNotEmpty()) {
                    /* 候補が存在するとき */
                    keyProcessor.handle("x")
                    return
                }

                inputCommitter.moveCursor(-1)
            }
            KeyboardAction.Right -> {
                if (stateFlow.value.candidates.isNotEmpty()) {
                    /* 候補が存在するとき */
                    handleSpace()
                    return
                }

                inputCommitter.moveCursor(1)
            }
            KeyboardAction.Dakuten -> {
                /* カーソルを末尾に移動 */
                outputManager.update()
                handleDakuten()
            }
            is KeyboardAction.CandidateIndex -> {
                val index = action.index
                stateFlow.update { it.copy(selectedIndex = index) }
                outputManager.commit()
            }
        }
    }
}