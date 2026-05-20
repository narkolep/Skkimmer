package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * キーボードショートカットをまとめたclass
 **/
class ShortcutHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val inputCommitter: InputCommitter,
    private val composingManager: ComposingManager
) {
    /**
     * CTRL押下時のキーボードショートカット
     **/
    fun handleCTRL(
        key: String,
        state: SkkUIState
    ): Boolean {
        if (!state.isCtrlPressed) return false

        when (key) {
            "j" -> {
                /* Japanese input */
                stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
                return true
            }
            "e" -> {
                /* Emoji */
                stateFlow.update { it.copy(inputMode = InputMode.EMOJI) }
                return true
            }
            "k" -> {
                /* switching Keyboards */
                stateFlow.update { it.copy(
                    isFlick = !state.isFlick,
                    inputMode = InputMode.HIRAGANA
                ) }
                return true
            }
            "n" -> {
                /* Numeric keypad */
                stateFlow.update { it.copy(inputMode = InputMode.NUMERIC) }
                return true
            }
            "a" -> {
                /* select All */
                inputCommitter.selectAll()
                return true
            }
            "x" -> {
                /* Cut */
                inputCommitter.cut()
                return true
            }
            "c" -> {
                /* Copy */
                inputCommitter.copy()
                return true
            }
            "v" -> {
                /* Paste */
                inputCommitter.paste()
                return true
            }
        }

        return false
    }

    /**
     * 通常のキーボードショートカット
     **/
    fun handleKey(
        state: SkkUIState,
        resultList: ConvertResult
    ): Boolean {
        val keyChar =
            if (resultList.composingNext.isNotEmpty()) resultList.composingNext.last()
            else resultList.output.lastOrNull()

        when (keyChar) {
            'x' -> {
                if (state.skkState == SkkState.HENKAN) {
                    var index = state.selectedIndex
                    if (index > 0) index -= 1
                    stateFlow.update { it.copy(selectedIndex = index) }
                    return true
                }
            }
            'q' -> {
                val beforeConvert =
                    if (resultList.isIgnore) state.midashiText + resultList.output
                    else state.midashiText
                val afterConvert: String

                if (state.isCtrlPressed) {
                    if (state.skkState != SkkState.NORMAL) {
                        afterConvert = composingManager.convertString(beforeConvert, KanaMap.hiraToHalfMap)
                        stateFlow.update { it.copy(
                            midashiText = afterConvert,
                            composingText = ""
                        ) }
                    } else {
                        if (state.inputMode == InputMode.HALF_KATAKANA) {
                            stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
                        } else {
                            stateFlow.update { it.copy(inputMode = InputMode.HALF_KATAKANA) }
                        }
                    }
                } else {
                    if (state.skkState != SkkState.NORMAL) {
                        afterConvert = if (state.inputMode == InputMode.KATAKANA) {
                            composingManager.convertString(beforeConvert, KanaMap.kataToHiraMap)
                        } else {
                            composingManager.convertString(beforeConvert, KanaMap.hiraToKataMap)
                        }
                        stateFlow.update { it.copy(
                            midashiText = afterConvert,
                            composingText = ""
                        ) }
                    } else {
                        if (state.inputMode == InputMode.KATAKANA) {
                            stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
                        } else {
                            stateFlow.update { it.copy(inputMode = InputMode.KATAKANA) }
                        }
                    }
                }
                composingManager.commit()
                return true
            }
            'l' -> {
                if (state.shiftState != ShiftState.LOWERCASE) {
                    stateFlow.update {
                        it.copy(
                            inputMode = InputMode.FULL_ASCII,
                            isFlick = false
                        )
                    }
                } else {
                    stateFlow.update {
                        it.copy(
                            inputMode = InputMode.HALF_ASCII,
                            isFlick = false
                        )
                    }
                }
                composingManager.commit()
                return true
            }
            '/' -> {
                if (state.skkState == SkkState.NORMAL) {
                    stateFlow.update {
                        it.copy(
                            skkState = SkkState.ABBREV,
                            isFlick = false,
                            composingText = " "
                        )
                    }
                    return true
                }
            }
        }

        return false
    }
}