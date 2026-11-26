package com.narkolep.skkimmer.keyboard

/**
 * 文字入力以外の操作
 */
sealed interface KeyboardAction {
    data object Shift : KeyboardAction
    data object Ctrl : KeyboardAction
    data object Space : KeyboardAction
    data object Backspace : KeyboardAction
    data object Enter : KeyboardAction
    data object ToggleKeyboard : KeyboardAction
    data object ToggleWidth : KeyboardAction
    data object ToggleCandidate : KeyboardAction
    data object Emoji : KeyboardAction
    data object Left : KeyboardAction
    data object Right : KeyboardAction
    data object Dakuten : KeyboardAction
    data class CandidateIndex(
        val index: Int
    ) : KeyboardAction
}