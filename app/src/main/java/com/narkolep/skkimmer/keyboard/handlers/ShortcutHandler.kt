package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.ConvertResult
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.IsIgnore
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.KeyboardType
import com.narkolep.skkimmer.keyboard.ShiftState
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
import com.narkolep.skkimmer.keyboard.SkkMode
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import kotlinx.coroutines.flow.update

/**
 * CTRL押下時のキーボードショートカット
 * @return 入力する文字列
 **/
internal fun KeyProcessor.handleCtrl(
    key: String,
    state: KeyboardState
): String? {
    if (!state.isCtrlPressed) return key
    val firstChar = key.firstOrNull() ?: return key

    when (firstChar) {
        'j' -> {
            /* Japanese input */
            stateFlow.update { it.changeMode(SkkMode.NORMAL).copy(
                inputMode = InputMode.HIRAGANA,
                shiftState = ShiftState.LOWERCASE
            ) }
            return null
        }
        'e' -> {
            /* Emoji */
            stateFlow.update { it.copy(
                keyboardType = KeyboardType.EMOJI,
                shiftState = ShiftState.LOWERCASE
            ) }
            outputManager.commit(commitAll = true)
            return null
        }
        'k' -> {
            /* switching Keyboards */
            stateFlow.update { it
                .changeMode(
                    mode =
                        if (state.currentSkkState.skkMode == SkkMode.ABBREV) SkkMode.NORMAL
                        else state.currentSkkState.skkMode
                )
                .copy(
                    isFlick = !state.isFlick,
                    inputMode = InputMode.HIRAGANA,
                    shiftState = ShiftState.LOWERCASE
            ) }
            return null
        }
        'n' -> {
            /* Numeric keypad */
            stateFlow.update { it.copy(
                keyboardType = KeyboardType.NUMERIC,
                inputMode = InputMode.HALF_ASCII,
                shiftState = ShiftState.LOWERCASE
            ) }
            outputManager.commit(commitAll = true)
            return null
        }
        'a' -> {
            /* select All */
            inputCommitter.selectAll()
            return null
        }
        'x' -> {
            /* Cut */
            inputCommitter.cut()
            return null
        }
        'c' -> {
            /* Copy */
            inputCommitter.copy()
            return null
        }
        'v' -> {
            /* Paste */
            return keyboardService.getClipboardText()
        }
    }

    return key
}

/**
 * 通常のキーボードショートカット
 * @return 一致するショートカットがあった場合は true
 **/
internal fun KeyProcessor.handleKey(
    state: KeyboardState,
    resultList: ConvertResult
): Boolean {
    val keyChar =
        if (resultList.composingNext.isNotEmpty()) resultList.composingNext.last()
        else resultList.output.lastOrNull()

    when (keyChar) {
        'x' -> {
            if (state.currentSkkState.skkMode != SkkMode.HENKAN) return false

            /* 変換中 */
            var index = state.selectedIndex
            if (index > 0) index -= 1
            stateFlow.update { it.copy(selectedIndex = index) }
            return true
        }
        'q' -> {
            /* Shiftキーが押されている、かつNORMALモード中 */
            if (state.shiftState != ShiftState.LOWERCASE && state.currentSkkState.skkMode == SkkMode.NORMAL) {
                stateFlow.update { it
                    .changeMode(
                        mode = SkkMode.MIDASHI
                    )
                    .copy(
                        composingText = HALF_SPACE
                    )
                }
                return true
            }

            val beforeConvert =
                if (resultList.isIgnore != IsIgnore.FALSE) state.currentSkkState.midashiText + resultList.output
                else state.currentSkkState.midashiText
            val afterConvert: String

            if (state.isCtrlPressed) {
                if (state.currentSkkState.skkMode == SkkMode.NORMAL) {
                    if (state.inputMode == InputMode.HALF_KATAKANA) {
                        stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
                    } else {
                        stateFlow.update { it.copy(inputMode = InputMode.HALF_KATAKANA) }
                    }
                } else {
                    afterConvert = convertString(beforeConvert, KanaMap.hiraToHalfMap)
                    stateFlow.update {
                        it.copy(
                            composingText = "",
                            currentSkkState = it.currentSkkState.copy(
                                midashiText = afterConvert
                            )
                        )
                    }
                }
            } else {
                if (state.currentSkkState.skkMode == SkkMode.NORMAL) {
                    if (state.inputMode == InputMode.KATAKANA) {
                        stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
                    } else {
                        stateFlow.update { it.copy(inputMode = InputMode.KATAKANA) }
                    }
                } else {
                    afterConvert = if (state.inputMode == InputMode.KATAKANA) {
                        convertString(beforeConvert, KanaMap.kataToHiraMap)
                    } else {
                        convertString(beforeConvert, KanaMap.hiraToKataMap)
                    }
                    stateFlow.update {
                        it.copy(
                            composingText = "",
                            currentSkkState = it.currentSkkState.copy(
                                midashiText = afterConvert
                            )
                        )
                    }
                }
            }

            outputManager.commit()
            return true
        }
        'l' -> {
            if (state.shiftState == ShiftState.LOWERCASE) {
                stateFlow.update {
                    it.copy(
                        inputMode = InputMode.HALF_ASCII,
                        isFlick = false
                    )
                }
            } else {
                stateFlow.update {
                    it.copy(
                        inputMode = InputMode.FULL_ASCII,
                        isFlick = false
                    )
                }
            }

            outputManager.commit(commitAll = true)
            return true
        }
        '/' -> {
            if (state.currentSkkState.skkMode != SkkMode.NORMAL) return false

            /* NORMALモード中 */
            stateFlow.update { it
                .changeMode(
                    mode = SkkMode.ABBREV
                )
                .copy(
                    isFlick = false,
                    composingText = HALF_SPACE
                )
            }
            return true
        }
    }

    return false
}