package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.SkkUIState
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.kanaToRomaji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class DakutenHandler(
    private val stateFlow: MutableStateFlow<SkkUIState>,
    private val backspaceHandler: BackspaceHandler,
    private val keyProcessor: KeyProcessor
) {
    fun handle() {
        val state = stateFlow.value

        /* 文字の取得 */
        var text = backspaceHandler.handle()
        if (text.isEmpty()) text = backspaceHandler.handle()
        if (text in setOf("ﾞ","ﾟ","゛","゜","゙","゚")) text = backspaceHandler.handle() + text

        /* ローマ字に変換 */
        val info = kanaToRomaji[text]

        /* textに合わせてinputModeを変更 */
        val temporalInputMode = when (info?.type) {
            KanaMap.KanaType.HIRAGANA -> InputMode.HIRAGANA
            KanaMap.KanaType.KATAKANA -> InputMode.KATAKANA
            KanaMap.KanaType.HALF_KATAKANA -> InputMode.HALF_KATAKANA
            else -> {
                keyProcessor.handle(text)
                return
            }
        }

        /* 取得した文字に合わせて入力モードを変更 */
        stateFlow.update {
            it.copy(
                inputMode = temporalInputMode
            )
        }

        /* 子音と母音に分ける */
        val vowel =
            if (info.romaji.takeLast(1) in setOf("a","i","u","e","o")) info.romaji.takeLast(1)
            else ""
        val consonant =
            if (vowel.isNotEmpty()) info.romaji.dropLast(1)
            else info.romaji

        /* 子音を変換 */
        val match = FlickKanaMap.flickConvert.find { it.consonantBefore == consonant }
        val newConsonant = when {
            info.type != KanaMap.KanaType.HIRAGANA && consonant == "x" && vowel == "u" -> "v"
            consonant == "t" && vowel == "u" -> "xt"
            match != null -> match.consonantAfter
            else -> consonant
        }

        /* 出力 */
        keyProcessor.handle(newConsonant + vowel)

        /* inputModeを元に戻す */
        stateFlow.update {
            it.copy(
                inputMode = state.inputMode
            )
        }
    }
}