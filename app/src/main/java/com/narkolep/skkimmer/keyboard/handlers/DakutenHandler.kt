package com.narkolep.skkimmer.keyboard.handlers

import com.narkolep.skkimmer.keyboard.InputCommitter
import com.narkolep.skkimmer.keyboard.InputMode
import com.narkolep.skkimmer.keyboard.KeyProcessor
import com.narkolep.skkimmer.keyboard.KeyboardState
import com.narkolep.skkimmer.keyboard.mappings.DakutenMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap
import com.narkolep.skkimmer.keyboard.mappings.KanaMap.kanaToRomaji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import android.util.Log

/**
 * カーソルの左側の文字を変換する
 */
fun dakutenHandler(
    stateFlow: MutableStateFlow<KeyboardState>,
    inputCommitter: InputCommitter,
    keyProcessor: KeyProcessor
) {
    val state = stateFlow.value

    /* 文字の取得 */
    val text = inputCommitter.getText(1).toString()

    /* ローマ字に変換 */
    val info = kanaToRomaji[text] ?: return

    /* textに合わせてinputModeを変更 */
    val temporalInputMode = when (info.type) {
        KanaMap.KanaType.HIRAGANA -> InputMode.HIRAGANA
        KanaMap.KanaType.KATAKANA -> InputMode.KATAKANA
        KanaMap.KanaType.HALF_KATAKANA -> InputMode.HALF_KATAKANA
    }

    /* 子音と母音に分ける */
    val vowel =
        if (info.romaji.takeLast(1) in setOf("a","i","u","e","o")) info.romaji.takeLast(1)
        else ""
    val consonant =
        if (vowel.isNotEmpty()) info.romaji.dropLast(1)
        else info.romaji

    /* 子音を変換 */
    val match = DakutenMap.flickConvert.find { it.consonantBefore == consonant }
    if (match == null) {
        Log.d("DAKUTEN", "match: 一致なし")
        return
    }
    val newConsonant = when {
        // 例外
        info.type != KanaMap.KanaType.HIRAGANA && consonant == "x" && vowel == "u" -> "v"
        consonant == "t" && vowel == "u" -> "xt"
        // 変換後の子音を代入
        else -> match.consonantAfter
    }

    /* 取得した文字に合わせて入力モードを変更 */
    stateFlow.update {
        it.copy(
            inputMode = temporalInputMode
        )
    }

    /* 出力 */
    inputCommitter.beginBatch()
    backspaceHandler(stateFlow, inputCommitter)
    keyProcessor.handle(newConsonant + vowel)
    inputCommitter.endBatch()

    /* inputModeを元に戻す */
    stateFlow.update {
        it.copy(
            inputMode = state.inputMode
        )
    }
}