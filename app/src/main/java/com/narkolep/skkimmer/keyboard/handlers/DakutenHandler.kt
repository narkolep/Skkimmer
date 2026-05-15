package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import kotlinx.coroutines.flow.MutableStateFlow

class DakutenHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val backspaceHandler: BackspaceHandler,
    private val keyProcessor: KeyProcessor
) {
    fun handle() {
        val state = stateFlow.value

        if (state.secondChar.isNotEmpty()) {
            val match = FlickKanaMap.flickConvert.find { it.consonantBefore == state.firstChar }

            if (state.tourokuFlag.isNotEmpty()) {
                /* 登録モードから抜ける */
                backspaceHandler.handle()
            }

            if (state.inputMode != InputMode.HIRAGANA && state.firstChar == "x" && state.secondChar == "u") {
                /* ウ -> ヴ */
                backspaceHandler.handle()
                keyProcessor.handle("v")
                keyProcessor.handle(state.secondChar)
                return
            }

            if (state.firstChar == "t" && state.secondChar == "u") {
                /* つ -> っ */
                backspaceHandler.handle()
                keyProcessor.handle("xt")
                keyProcessor.handle(state.secondChar)
                return
            }

            if (match != null) {
                /* それ以外 */
                backspaceHandler.handle()
                /* 2文字消す場合 */
                if (match.backspace == 2 && state.inputMode == InputMode.HALF_KATAKANA) {
                    backspaceHandler.handle()
                }
                keyProcessor.handle(match.consonantAfter)
                keyProcessor.handle(state.secondChar)
                return
            }
        }
    }
}