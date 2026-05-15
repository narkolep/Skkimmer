package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.kanaToRomaji
import kotlinx.coroutines.flow.MutableStateFlow

class DakutenHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val backspaceHandler: BackspaceHandler,
    private val keyProcessor: KeyProcessor,
    private val inputCommitter: InputCommitter
) {
    fun handle() {
        val state = stateFlow.value

        inputCommitter.beginBatch()

        if (state.tourokuFlag.isNotEmpty()) {
            /* 登録モードから抜ける */
            backspaceHandler.handle()
        }

        var text = inputCommitter.getText(1)
        if (text == "ﾞ" || text == "ﾟ") {
            text = inputCommitter.getText(2)
            backspaceHandler.handle()
        }

        val romaji = kanaToRomaji[text]

        if (romaji?.all { it in 'a'..'z'} ?: false) {
            val vowel = romaji.takeLast(1)
            val consonant = romaji.dropLast(1)
            val match = FlickKanaMap.flickConvert.find { it.consonantBefore == consonant }

            backspaceHandler.handle()

            if (state.inputMode != InputMode.HIRAGANA && consonant == "x" && vowel == "u") {
                /* ウ -> ヴ */
                keyProcessor.handle("v")
            } else if ((consonant == "t" || consonant == "ts") && vowel == "u") {
                /* つ -> っ */
                keyProcessor.handle("xt")
            } else if (match != null) {
                /* それ以外 */
                keyProcessor.handle(match.consonantAfter)
            } else {
                /* 一致しなかった場合 */
                keyProcessor.handle(consonant)
            }

            keyProcessor.handle(vowel)
        }
        inputCommitter.endBatch()
    }
}