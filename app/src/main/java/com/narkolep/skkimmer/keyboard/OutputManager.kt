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
import kotlin.collections.lastOrNull

class OutputManager(
    internal val stateFlow: MutableStateFlow<KeyboardState>,
    internal val inputCommitter: InputCommitter,
    internal val dictionaryManager: DictionaryManager,
    internal val coroutineScope: CoroutineScope,
) {
    /**
     * 確定入力
     * テンキー/英数/abbrevモード
     */
    fun asciiOutput(
        key: String,
        state: KeyboardState,
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
                if (state.currentSkkState.skkMode == SkkMode.ABBREV) {
                    stateFlow.update {
                        it.copy(
                            currentSkkState = it.currentSkkState.copy(
                                midashiText = it.currentSkkState.midashiText + outChar
                            )
                        )
                    }
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
        result: ConvertResult,
    ) {
        when (newState.currentSkkState.skkMode) {
            SkkMode.NORMAL -> {
                if (newState.isRegistering) {
                    stateFlow.update { it
                        .updateCandidate(
                            lookupKey = it.registrationKey.lookupKey,
                            candidate = newState.registrationKey.candidate + result.output,
                        ).copy(
                            composingText = result.composingNext
                        )
                    }
                    return
                }

                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext
                    )
                }

                inputCommitter.commit(result.output)
                /* 表示を更新する */
                update()
            }

            SkkMode.MIDASHI -> {
                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        currentSkkState = it.currentSkkState.copy(
                            midashiText = newState.currentSkkState.midashiText + result.output,
                        )
                    )
                }
            }

            SkkMode.OKURIGANA -> {
                if (result.output.isEmpty() || result.isIgnore == IsIgnore.TT_OR_N) {
                    /* outputが未確定のとき */
                    stateFlow.update {
                        it.copy(
                            composingText = result.composingNext,
                            currentSkkState = it.currentSkkState.copy(
                                midashiText = newState.currentSkkState.midashiText +
                                        if (oldState.currentSkkState.skkMode == SkkMode.MIDASHI) result.output else "",
                                okuriganaText = newState.currentSkkState.okuriganaText +
                                        if (oldState.currentSkkState.skkMode == SkkMode.OKURIGANA) result.output else "",
                            )
                        )
                    }
                    return
                }

                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        currentSkkState = it.currentSkkState.copy(
                            okuriganaText = newState.currentSkkState.okuriganaText + result.output,
                            okuriganaTrigger = result.okuriganaFlag,
                        )
                    )
                }

                getCandidatesAndChangeState(result.isIgnore == IsIgnore.XTU)
            }

            SkkMode.HENKAN -> {
                /* 変換中に文字入力があったら、確定する */
                stateFlow.update {
                    it.copy(
                        currentSkkState = it.currentSkkState.copy(
                            okuriganaText = newState.currentSkkState.okuriganaText + result.output
                        )
                    )
                }
                commit()
                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext
                    )
                }
            }

            SkkMode.ABBREV -> {
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
        val registrationText = state.buildText()
        val skkState = state.currentSkkState

        return when (skkState.skkMode) {
            SkkMode.NORMAL -> {
                if (state.composingText.isEmpty() && state.registrationKey.lookupKey.isEmpty()) {
                    return null
                }

                registrationText + state.composingText
            }
            SkkMode.MIDASHI -> {
                registrationText + skkState.midashiText + state.composingText
            }
            SkkMode.OKURIGANA -> {
                val word = state.selectedCandidate?.text ?: skkState.midashiText

                registrationText + word + OKURI_MARK + skkState.okuriganaText + state.composingText
            }
            SkkMode.HENKAN -> {
                val isFromOkurigana = state.savedSkkState.lastOrNull()?.skkMode == SkkMode.OKURIGANA
                val displayOkuri =
                    if (isFromOkurigana) OKURI_MARK + skkState.okuriganaText
                    else ""
                val word = state.selectedCandidate?.text ?: skkState.midashiText

                registrationText + word + displayOkuri + state.composingText
            }
            SkkMode.ABBREV -> {
                registrationText + skkState.midashiText.ifEmpty { state.composingText }
            }
        }
    }

    /**
     * 未確定文字列を確定する
     */
    fun commit(
        commitAll: Boolean = false
    ) {
        /* stateFlow.update 内でスナップショットの読み取りとクリアを実行 */
        var capturedState: KeyboardState? = null
        var commitText = ""

        stateFlow.update { current ->
            capturedState = current
            val text = buildCommitText(current)
            commitText = text

            if (current.isRegistering && !commitAll) {
                current.clear().updateCandidate(
                    lookupKey = current.registrationKey.lookupKey,
                    candidate = current.registrationKey.candidate + text,
                ).copy(
                    savedSkkState = current.savedSkkState.dropLast(1)
                )
            } else {
                current.resetAll()
            }
        }

        val state = capturedState ?: return

        /* 履歴の記録 */
        if (state.currentSkkState.skkMode == SkkMode.HENKAN) {
            val candidate = state.selectedCandidate

            if (candidate != null) {
                val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    Log.e("SKK_ERROR", "Error in OutputManager learnWord", throwable)
                }

                val key = formatString(
                    text = state.currentSkkState.midashiText + state.currentSkkState.okuriganaTrigger
                )

                coroutineScope.launch(Dispatchers.IO + exceptionHandler) {
                    dictionaryManager.learnWord(
                        lookupKey = key.lookupKey,
                        candidate = candidate.raw,
                        isFromSystem = true,
                    )
                }
            }
        }

        if (state.registrationKey.lookupKey.isEmpty() || commitAll) {
            inputCommitter.setComposingText("")
            if (commitText.isNotEmpty()) inputCommitter.commit(commitText)
        }
    }

    /**
     * state から確定テキストを構築する純粋関数
     */
    private fun buildCommitText(state: KeyboardState): String {
        val skkState = state.currentSkkState

        return when (skkState.skkMode) {
            SkkMode.NORMAL -> {
                state.composingText
            }
            SkkMode.MIDASHI -> {
                val composing = if (state.composingText == HALF_SPACE) "" else state.composingText

                skkState.midashiText + composing
            }
            SkkMode.HENKAN, SkkMode.OKURIGANA -> {
                val word = state.selectedCandidate?.text ?: skkState.midashiText

                word + skkState.okuriganaText + state.composingText
            }
            SkkMode.ABBREV -> {
                skkState.midashiText
            }
        }
    }
}