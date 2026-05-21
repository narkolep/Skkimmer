package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkState
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.kanaToRomaji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class DakutenHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val backspaceHandler: BackspaceHandler,
    private val keyProcessor: KeyProcessor,
    private val inputCommitter: InputCommitter
) {
    fun handle() {
        val state = stateFlow.value

        inputCommitter.beginBatch()

        if (
            state.tourokuFlag.isNotEmpty() &&
            state.tourokuFlag.substringAfter(":").isEmpty() &&
            state.skkState == SkkState.NORMAL
        ) {
            /* 登録モードから抜ける */
            backspaceHandler.handle()
        }

        /* 文字の取得 */
        var text = inputCommitter.getText(1)
        if (text == "ﾞ" || text == "ﾟ") {
            text = inputCommitter.getText(2)
            backspaceHandler.handle()
        }

        /* ローマ字に変換 */
        val info = kanaToRomaji[text]
        val temporalInputMode = when (info?.type) {
            KanaMap.KanaType.HIRAGANA -> InputMode.HIRAGANA
            KanaMap.KanaType.KATAKANA -> InputMode.KATAKANA
            KanaMap.KanaType.HALF_KATAKANA -> InputMode.HALF_KATAKANA
            else -> return
        }
        stateFlow.update {
            it.copy(
                inputMode = temporalInputMode
            )
        }

        /* 出力 */
        if (info.romaji.all { it in 'a'..'z'}) {
            val vowel = info.romaji.takeLast(1)
            val consonant = info.romaji.dropLast(1)
            val match = FlickKanaMap.flickConvert.find { it.consonantBefore == consonant }

            backspaceHandler.handle()

            if (info.type != KanaMap.KanaType.HIRAGANA && consonant == "x" && vowel == "u") {
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

        /* inputModeを元に戻す */
        stateFlow.update {
            it.copy(
                inputMode = state.inputMode
            )
        }
        inputCommitter.endBatch()
    }
}