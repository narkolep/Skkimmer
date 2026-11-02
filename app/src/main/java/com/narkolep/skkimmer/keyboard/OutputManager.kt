package com.narkolep.skkimmer.keyboard

import android.util.Log
import com.narkolep.skkimmer.data.DictionaryManager
import com.narkolep.skkimmer.keyboard.SkkConstants.DICT_ANNOTATION
import com.narkolep.skkimmer.keyboard.SkkConstants.FULL_SPACE
import com.narkolep.skkimmer.keyboard.SkkConstants.OKURI_MARK
import com.narkolep.skkimmer.keyboard.handlers.getCandidatesAndChangeState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OutputManager(
    private val stateFlow: MutableStateFlow<KeyboardState>,
    private val inputCommitter: InputCommitter,
    private val dictionaryManager: DictionaryManager,
    private val coroutineScope: CoroutineScope
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
                    stateFlow.update { it.copy(midashiText = state.midashiText + outChar) }
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
                if (newState.tourokuFlag.isNotEmpty()) {
                    stateFlow.update {
                        it.copy(
                            composingText = result.composingNext,
                            tourokuFlag = newState.tourokuFlag + result.output
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
                            if (it.tourokuFlag.isEmpty()) result.okuriganaFlag
                            else it.oldOkuriganaTrigger
                    )
                }
            }

            SkkState.OKURIGANA -> {
                if (result.output.isEmpty() || result.isIgnore) {
                    /* outputが"ん"、"っ"、もしくは未確定のとき */
                    stateFlow.update {
                        it.copy(
                            composingText = result.composingNext,
                            midashiText = newState.midashiText + if (oldState.skkState == SkkState.MIDASHI) result.output else "",
                            okuriganaText = newState.okuriganaText + if (oldState.skkState == SkkState.OKURIGANA) result.output else "",
                            oldOkuriganaTrigger =
                                if (it.tourokuFlag.isEmpty()) result.okuriganaFlag
                                else it.oldOkuriganaTrigger
                        )
                    }
                    return
                }

                /* 候補を検索する */
                var flag = result.okuriganaFlag
                val firstOkuri = oldState.okuriganaText.firstOrNull()
                if (firstOkuri == 'ん' || firstOkuri == 'ン') flag = "n"
                if (firstOkuri == 'っ' || firstOkuri == 'ッ') flag = "t"

                stateFlow.update {
                    it.copy(
                        composingText = result.composingNext,
                        okuriganaText = newState.okuriganaText + result.output,
                        okuriganaTrigger = flag,
                        oldOkuriganaTrigger =
                            if (it.tourokuFlag.isEmpty()) result.okuriganaFlag
                            else it.oldOkuriganaTrigger
                    )
                }

                getCandidatesAndChangeState(stateFlow, dictionaryManager, coroutineScope)
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
                            if (it.tourokuFlag.isEmpty()) result.okuriganaFlag
                            else it.oldOkuriganaTrigger
                    )
                }
            }

            SkkState.ABBREV -> {
                // abbrevの場合は連結処理がないので、何もしない
            }
        }
    }

    /**
     * 表示する未確定文字列の更新
     */
    fun update() {
        val state = stateFlow.value

        val displayText = when (state.skkState) {
            SkkState.NORMAL -> {
                if (state.composingText.isEmpty() && state.tourokuFlag.isEmpty()) {
                    inputCommitter.commit("")
                    return
                }
                state.tourokuFlag + state.composingText
            }
            SkkState.MIDASHI -> {
                state.tourokuFlag + state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                state.tourokuFlag + state.midashiText + OKURI_MARK + state.okuriganaText + state.composingText
            }
            SkkState.HENKAN -> {
                val displayOkuri =
                    if (state.okuriganaText.isNotEmpty()) OKURI_MARK + state.okuriganaText + state.composingText
                    else ""
                val candidate = state.candidates.getOrNull(state.selectedIndex)
                    ?: state.candidates.firstOrNull()
                val word = candidate?.split(DICT_ANNOTATION)?.get(0) ?: state.midashiText

                state.tourokuFlag + word + displayOkuri
            }
            SkkState.ABBREV -> {
                state.tourokuFlag + state.midashiText.ifEmpty { state.composingText }
            }
        }

        inputCommitter.setComposingText(displayText)
    }

    /**
     * 未確定文字列を確定する
     */
    fun commit() {
        val state = stateFlow.value

        val commitText = when (state.skkState) {
            SkkState.NORMAL -> {
                state.composingText
            }
            SkkState.MIDASHI -> {
                state.midashiText + state.composingText
            }
            SkkState.OKURIGANA -> {
                state.midashiText + state.composingText
            }
            SkkState.HENKAN -> {
                /* 履歴を記録 */
                val candidate = state.candidates.getOrNull(state.selectedIndex)
                    ?: state.candidates.firstOrNull()

                if (candidate != null) {
                    val learnText = state.midashiText + state.okuriganaTrigger
                    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                        Log.e("SKK_ERROR", "Error in OutputManager learnWord", throwable)
                    }

                    coroutineScope.launch(Dispatchers.IO + exceptionHandler) {
                        try {
                            dictionaryManager.learnWord(
                                midashi = learnText,
                                candidate = candidate,
                                isFromSystem = true
                            )
                        } catch (e: Exception) {
                            Log.e("SKK_ERROR", "Exception learning word in OutputManager commit", e)
                        }
                    }

                    val word = candidate.split(DICT_ANNOTATION)[0]
                    word + state.okuriganaText
                } else {
                    state.midashiText + state.okuriganaText
                }
            }
            SkkState.ABBREV -> {
                state.midashiText
            }
        }

        /* 状態をアトミックにリセット・更新 */
        stateFlow.update { current ->
            val cleared = current.clear()
            if (state.tourokuFlag.isNotEmpty()) {
                cleared.copy(tourokuFlag = state.tourokuFlag + commitText)
            } else {
                cleared
            }
        }

        if (state.tourokuFlag.isEmpty()) {
            inputCommitter.setComposingText("")
            if (commitText.isNotEmpty()) inputCommitter.commit(commitText)
        }
    }
}