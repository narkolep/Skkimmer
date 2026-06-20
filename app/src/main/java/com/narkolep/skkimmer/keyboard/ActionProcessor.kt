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
    object Left : KeyboardAction()
    object Right : KeyboardAction()
    object Dakuten : KeyboardAction()
    data class CandidateIndex(
        val index: Int
    ) : KeyboardAction()
}

class ActionProcessor(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val outputManager: OutputManager,
    private val inputCommitter: InputCommitter,
    private val keyProcessor: KeyProcessor,
    private val dictionaryManager: DictionaryManager,
    private val editorInfo: EditorInfo?
) {
    /**
     * アクションキーの分岐
     **/
    fun handle(action: KeyboardAction) {

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
                stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
            }
            KeyboardAction.Left -> {
                if (stateFlow.value.skkState == SkkState.HENKAN) {
                    keyProcessor.handle("x")
                    return
                }

                moveCursor(-1)
            }
            KeyboardAction.Right -> {
                if (stateFlow.value.skkState == SkkState.HENKAN) {
                    spaceHandler(stateFlow, dictionaryManager, keyProcessor)
                    return
                }

                moveCursor(1)
            }
            KeyboardAction.Dakuten -> {
                dakutenHandler(stateFlow, inputCommitter, keyProcessor)
            }
            is KeyboardAction.CandidateIndex -> {
                val index = action.index
                stateFlow.update { it.copy(selectedIndex = index) }
                outputManager.commit()
            }
        }
    }

    /* カーソル移動 */
    private fun moveCursor(offset: Int) {
        val extracted = inputCommitter.getExtractedText() ?: return
        val textLength = extracted.text?.length ?: 0
        val current = extracted.selectionStart
        val newPos = (current + offset).coerceIn(0, textLength)

        inputCommitter.setSelection(newPos, newPos)
    }
}