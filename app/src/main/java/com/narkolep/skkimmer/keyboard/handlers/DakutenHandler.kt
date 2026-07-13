package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.mappings.FlickKanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.kanaToRomaji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * カーソルの左側の文字を変換する
 */
fun dakutenHandler(
    stateFlow: MutableStateFlow<KeyboardState>,
    inputCommitter: InputCommitter,
    keyProcessor: KeyProcessor
) {
    val state = stateFlow.value

    /* beginBatchEdit */
    inputCommitter.beginBatch()

    /* 文字の取得 */
    var text = backspaceHandler(stateFlow, inputCommitter)
    if (text.isEmpty()) text = backspaceHandler(stateFlow, inputCommitter)
    if (text in setOf("ﾞ","ﾟ","゛","゜","゙","゚")) text = backspaceHandler(stateFlow, inputCommitter) + text

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
    inputCommitter.endBatch()

    /* inputModeを元に戻す */
    stateFlow.update {
        it.copy(
            inputMode = state.inputMode
        )
    }
}