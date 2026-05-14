package com.narkolep.skkimmer.keyboard

import android.view.inputmethod.EditorInfo
import com.narkolep.skkimmer.data.SkkDictionaryManager
import com.narkolep.skkimmer.keyboard.handlers.BackspaceHandler
import com.narkolep.skkimmer.keyboard.handlers.CursorHandler
import com.narkolep.skkimmer.keyboard.handlers.DakutenHandler
import com.narkolep.skkimmer.keyboard.handlers.EnterHandler
import com.narkolep.skkimmer.keyboard.handlers.ModifierHandler
import com.narkolep.skkimmer.keyboard.handlers.SpaceHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class KeyboardAction {
    object Shift : KeyboardAction()
    object Ctrl : KeyboardAction()
    object Space : KeyboardAction()
    object Backspace : KeyboardAction()
    object Enter : KeyboardAction()
    object ToggleEmoji : KeyboardAction()
    object ToggleNumeric : KeyboardAction()
    object Left : KeyboardAction()
    object Right : KeyboardAction()
    object Dakuten : KeyboardAction()
    data class CandidateIndex(
        val index: Int
    ) : KeyboardAction()
}

class ActionProcessor(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val composingManager: ComposingManager,
    editorInfo: EditorInfo?,
    inputCommitter: InputCommitter,
    keyProcessor: KeyProcessor,
    dictionaryManager: SkkDictionaryManager,
) {
    /* Handlers */
    private val modifierHandler = ModifierHandler(stateFlow)
    private val spaceHandler = SpaceHandler(stateFlow, keyProcessor, dictionaryManager)
    private val backspaceHandler = BackspaceHandler(stateFlow, inputCommitter)
    private val enterHandler =
        EnterHandler(stateFlow, editorInfo, inputCommitter, dictionaryManager, composingManager)
    private val cursorHandler = CursorHandler(stateFlow, inputCommitter)
    private val dakutenHandler = DakutenHandler(stateFlow, backspaceHandler, keyProcessor)

    /**
     * アクションキーの分岐
     **/
    fun handle(action: KeyboardAction) {

        when(action) {
            KeyboardAction.Shift -> {
                modifierHandler.handleShift()
            }
            KeyboardAction.Ctrl -> {
                modifierHandler.handleCtrl()
            }
            KeyboardAction.Space -> {
                spaceHandler.handle()
            }
            KeyboardAction.Backspace -> {
                backspaceHandler.handle()
            }
            KeyboardAction.Enter -> {
                enterHandler.handle()
            }
            KeyboardAction.ToggleEmoji -> {
                stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
            }
            KeyboardAction.ToggleNumeric -> {
                stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
            }
            KeyboardAction.Left -> {
                cursorHandler.handleLeft()
            }
            KeyboardAction.Right -> {
                cursorHandler.handleRight()
            }
            KeyboardAction.Dakuten -> {
                dakutenHandler.handle()
            }
            is KeyboardAction.CandidateIndex -> {
                val index = action.index
                stateFlow.update { it.copy(selectedIndex = index) }
                composingManager.commit()
            }
        }
    }
}