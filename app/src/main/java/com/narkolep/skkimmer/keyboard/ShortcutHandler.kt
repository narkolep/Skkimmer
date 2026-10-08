package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.keyboard.handlers.convertString
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * CTRL押下時のキーボードショートカット
 * @return 一致するショートカットがあった場合は true を返す
 **/
fun handleCTRL(
    key: String,
    state: KeyboardState,
    stateFlow: MutableStateFlow<KeyboardState>,
    inputCommitter: InputCommitter,
    outputManager: OutputManager,
    service: KeyboardService
): String? {
    if (!state.isCtrlPressed) return key

    when (key) {
        "j" -> {
            /* Japanese input */
            stateFlow.update { it.copy(
                inputMode = InputMode.HIRAGANA,
                shiftState = ShiftState.LOWERCASE
            ) }
            return null
        }
        "e" -> {
            /* Emoji */
            stateFlow.update { it.copy(
                keyboardType = KeyboardType.EMOJI,
                shiftState = ShiftState.LOWERCASE
            ) }
            outputManager.commit()
            return null
        }
        "k" -> {
            /* switching Keyboards */
            stateFlow.update { it.copy(
                isFlick = !state.isFlick,
                shiftState = ShiftState.LOWERCASE,
                inputMode = InputMode.HIRAGANA,
                skkState =
                    if (state.skkState == SkkState.ABBREV) SkkState.NORMAL
                    else state.skkState
            ) }
            return null
        }
        "n" -> {
            /* Numeric keypad */
            stateFlow.update { it.copy(
                keyboardType = KeyboardType.NUMERIC,
                inputMode = InputMode.HALF_ASCII,
                shiftState = ShiftState.LOWERCASE
            ) }
            outputManager.commit()
            return null
        }
        "a" -> {
            /* select All */
            inputCommitter.selectAll()
            return null
        }
        "x" -> {
            /* Cut */
            inputCommitter.cut()
            return null
        }
        "c" -> {
            /* Copy */
            inputCommitter.copy()
            return null
        }
        "v" -> {
            /* Paste */
            return service.getClipboardText()
        }
    }

    return key
}

/**
 * 通常のキーボードショートカット
 * @return 一致するショートカットがあった場合は true を返す
 **/
fun handleKey(
    state: KeyboardState,
    stateFlow: MutableStateFlow<KeyboardState>,
    resultList: ConvertResult,
    outputManager: OutputManager
): Boolean {
    val keyChar =
        if (resultList.composingNext.isNotEmpty()) resultList.composingNext.last()
        else resultList.output.lastOrNull()

    when (keyChar) {
        'x' -> {
            if (state.skkState != SkkState.HENKAN) return false

            /* 変換中 */
            var index = state.selectedIndex
            if (index > 0) index -= 1
            stateFlow.update { it.copy(selectedIndex = index) }
            return true
        }
        'q' -> {
            /* Shiftキーが押されている、かつNORMALモード中 */
            if (state.shiftState != ShiftState.LOWERCASE && state.skkState == SkkState.NORMAL) {
                stateFlow.update {
                    it.copy(
                        skkState = SkkState.MIDASHI,
                        composingText = "\u0020"
                    )
                }
                return true
            }

            val beforeConvert =
                if (resultList.isIgnore) state.midashiText + resultList.output
                else state.midashiText
            val afterConvert: String

            if (state.isCtrlPressed) {
                if (state.skkState == SkkState.NORMAL) {
                    if (state.inputMode == InputMode.HALF_KATAKANA) {
                        stateFlow.update { it.copy(inputMode = InputMode.HIRAGANA) }
                    } else {
                        stateFlow.update { it.copy(inputMode = InputMode.HALF_KATAKANA) }
                    }
                } else {
                    afterConvert = convertString(beforeConvert, KanaMap.hiraToHalfMap)
                    stateFlow.update {
                        it.copy(
                            midashiText = afterConvert,
                            composingText = ""
                        )
                    }
                }
            } else {
                if (state.skkState == SkkState.NORMAL) {
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
                            midashiText = afterConvert,
                            composingText = ""
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

            outputManager.commit()
            return true
        }
        '/' -> {
            if (state.skkState != SkkState.NORMAL) return false

            /* NORMALモード中 */
            stateFlow.update {
                it.copy(
                    skkState = SkkState.ABBREV,
                    isFlick = false,
                    composingText = "\u0020"
                )
            }
            return true
        }
    }

    return false
}