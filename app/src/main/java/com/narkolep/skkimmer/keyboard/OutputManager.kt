package com.narkolep.skkimmer.keyboard

import android.util.Log
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.SkkConstants.FULL_SPACE
import com.narkolep.skkimmer.keyboard.SkkConstants.HALF_SPACE
import com.narkolep.skkimmer.keyboard.SkkConstants.OKURI_MARK
import com.narkolep.skkimmer.keyboard.handlers.formatString
import com.narkolep.skkimmer.keyboard.handlers.getCandidatesAndChangeState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OutputManager(
    internal val stateFlow: MutableStateFlow<KeyboardState>,
    internal val inputCommitter: InputCommitter,
    internal val dictionaryManager: DictionaryManager,
    internal val coroutineScope: CoroutineScope
) {
    /**
     * 確定入力
     * テンキー/英数/abbrevモード
     */
    fun asciiOutput(
        key: String,
        state: KeyboardState
    ): Boolean {
        val outChar = if (state.shiftState == ShiftState.LOWERCASE) key else key.uppercase()

        val firstChar = outChar.firstOrNull() ?: return false
        when (state.inputMode) {
            InputMode.HALF_ASCII -> {
                inputCommitter.commit(outChar)
                return true
            }
            InputMode.FULL_ASCII -> {
                val fullChar = when (val code = firstChar.code) {
                    /* Space */
                    0x20 -> FULL_SPACE
                    /* !から~までの半角英数字 */
                    in 0x21..0x7E -> (code + 0xFEE0).toChar().toString()
                    else -> outChar
                }
                inputCommitter.commit(fullChar)
                return true
            }
            else -> {
                if (state.skkState == SkkState.ABBREV) {
                    stateFlow.update { it.copy(midashiText = it.midashiText + outChar) }
                    return true
                }
            }
        }

        return false
    }

    /**
     * ConvertResultの値をstateに保存
     * 仮名を出力
     */
    fun kanaOutput(
        newState: KeyboardState,
        oldState: KeyboardState,
        result: ConvertResult
    ) {
        when (newState.skkState) {
            SkkState.NORMAL -> {
                if (newState.isRegistering) {
                    stateFlow.update {
                        it.copy(
                            composingText = result.composingNext,
                            registrationKey = RegistrationKey(
                                lookupKey = newState.registrationKey.lookupKey,
                                candidate = newState.registrationKey.candidate + result.output
                            )
                        )
                    }
                    return
                }

                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        oldOkuriganaTrigger = result.okuriganaFlag
                    )
                }
                inputCommitter.commit(result.output)

                if (!newState.isFlick) update()
            }

            SkkState.MIDASHI -> {
                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        midashiText = newState.midashiText + result.output,
                        oldOkuriganaTrigger =
                            if (it.registrationKey.lookupKey.isEmpty()) result.okuriganaFlag
                            else it.oldOkuriganaTrigger
                    )
                }
            }

            SkkState.OKURIGANA -> {
                if (result.output.isEmpty() || result.isIgnore) {
                    /* outputが未確定のとき */
                    stateFlow.update {
                        it.copy(
                            composingText = result.composingNext,
                            midashiText = newState.midashiText + if (oldState.skkState == SkkState.MIDASHI) result.output else "",
                            okuriganaText = newState.okuriganaText + if (oldState.skkState == SkkState.OKURIGANA) result.output else "",
                            oldOkuriganaTrigger =
                                if (it.registrationKey.lookupKey.isEmpty()) result.okuriganaFlag
                                else it.oldOkuriganaTrigger
                        )
                    }
                    return
                }

                /* 候補を検索する */
                val firstOkuri = oldState.okuriganaText.firstOrNull()
                val flag = when (firstOkuri) {
                    'ん', 'ン' -> "n"
                    'っ', 'ッ' -> "t"
                    else -> result.okuriganaFlag
                }

                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        okuriganaText = newState.okuriganaText + result.output,
                        okuriganaTrigger = flag,
                        oldOkuriganaTrigger =
                            if (it.registrationKey.lookupKey.isEmpty()) result.okuriganaFlag
                            else it.oldOkuriganaTrigger
                    )
                }

                getCandidatesAndChangeState()
            }

            SkkState.HENKAN -> {
                /* 変換中に文字入力があったら、確定する */
                stateFlow.update {
                    it.copy(
                        okuriganaText = newState.okuriganaText + result.output
                    )
                }
                commit()
                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        oldOkuriganaTrigger =
                            if (it.registrationKey.lookupKey.isEmpty()) result.okuriganaFlag
                            else it.oldOkuriganaTrigger
                    )
                }
            }

            SkkState.ABBREV -> {
                /* abbrevの場合は連結処理がないので、何もしない */
            }
        }
    }

    /**
     * 表示する未確定文字列の更新
     * stateFlow.value のスナップショットを1回だけ取得して処理する
     */
    fun update() {
        val state = stateFlow.value

        val displayText = buildDisplayText(state)
        if (displayText == null) {
            inputCommitter.commit("")
            return
        }

        inputCommitter.setComposingText(displayText)
    }

    /**
     * state から表示テキストを構築する純粋関数
     * @return NORMAL かつ文字列が空の場合は null を返す
     */
    private fun buildDisplayText(state: KeyboardState): String? {
        return when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isEmpty() && state.registrationKey.lookupKey.isEmpty()) {
                    return null
                }

                state.registrationKey.buildText() + state.composingText
            }
            SkkState.MIDASHI -> {
                state.registrationKey.buildText() + state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                state.registrationKey.buildText() + state.midashiText + OKURI_MARK + state.okuriganaText + state.composingText
            }
            SkkState.HENKAN -> {
                val displayOkuri =
                    if (state.okuriganaText.isNotEmpty()) OKURI_MARK + state.okuriganaText + state.composingText
                    else ""
                val candidate = state.selectedCandidate
                val word = candidate?.text ?: state.midashiText

                state.registrationKey.buildText() + word + displayOkuri
            }
            SkkState.ABBREV -> {
                state.registrationKey.buildText() + state.midashiText.ifEmpty { state.composingText }
            }
        }
    }

    /**
     * 未確定文字列を確定する
     */
    fun commit() {
        // stateFlow.update 内でスナップショットの読み取りとクリアを実行
        var capturedState: KeyboardState? = null
        var commitText = ""

        stateFlow.update { current ->
            capturedState = current
            val text = buildCommitText(current)
            commitText = text

            val cleared = current.clear()
            if (current.isRegistering) {
                cleared.copy(
                    registrationKey = RegistrationKey(
                        lookupKey = current.registrationKey.lookupKey,
                        candidate = current.registrationKey.candidate + text
                    )
                )
            } else {
                cleared
            }
        }

        val state = capturedState ?: return

        // 履歴の記録
        // 非同期、状態に影響しない副作用
        if (state.skkState == SkkState.HENKAN) {
            val candidate = state.selectedCandidate

            if (candidate != null) {
                val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    Log.e("SKK_ERROR", "Error in OutputManager learnWord", throwable)
                }

                val key = formatString(state.midashiText + state.okuriganaTrigger)

                coroutineScope.launch(Dispatchers.IO + exceptionHandler) {
                    dictionaryManager.learnWord(
                        lookupKey = key.lookupKey,
                        candidate = candidate.raw,
                        isFromSystem = true
                    )
                }
            }
        }

        if (state.registrationKey.lookupKey.isEmpty()) {
            inputCommitter.setComposingText("")
            if (commitText.isNotEmpty()) inputCommitter.commit(commitText)
        }
    }

    /**
     * state から確定テキストを構築する純粋関数
     */
    private fun buildCommitText(state: KeyboardState): String {
        return when (state.skkState) {
            SkkState.NORMAL -> {
                state.composingText
            }
            SkkState.MIDASHI -> {
                val composing = if (state.composingText == HALF_SPACE) "" else state.composingText

                state.midashiText + composing
            }
            SkkState.OKURIGANA -> {
                state.midashiText + state.composingText
            }
            SkkState.HENKAN -> {
                val candidate = state.selectedCandidate
                val originalText = state.midashiText + state.okuriganaText

                if (candidate != null) {
                    candidate.text + state.okuriganaText
                } else {
                    originalText
                }
            }
            SkkState.ABBREV -> {
                state.midashiText
            }
        }
    }
}