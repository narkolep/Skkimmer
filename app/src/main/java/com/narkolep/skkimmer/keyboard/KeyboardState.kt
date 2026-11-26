package com.narkolep.skkimmer.keyboard

import com.narkolep.skkimmer.data.Candidate
import com.narkolep.skkimmer.keyboard.SkkConstants.DICT_DELIMITER

enum class ShiftState {
    LOWERCASE,
    SHIFTED,
    CAPS_LOCK
}

enum class KeyboardType {
    NORMAL,
    EMOJI,
    NUMERIC,
    CANDIDATE,
}

enum class InputMode {
    HALF_ASCII,
    FULL_ASCII,
    HIRAGANA,
    KATAKANA,
    HALF_KATAKANA
}

enum class SkkMode {
    NORMAL,
    MIDASHI,
    OKURIGANA,
    HENKAN,
    ABBREV
}

data class RegistrationKey(
    val lookupKey: String = "",
    val candidate: String = ""
)

data class SkkState(
    val skkMode: SkkMode = SkkMode.NORMAL,
    val midashiText: String = "",
    val okuriganaText: String = "",
    val okuriganaKey: String = "",
)

/**
 * キーボードの状態
 */
data class KeyboardState(
    /* === STATE === */
    /* 入力モード(英数/日本語など) */
    val inputMode: InputMode = InputMode.HIRAGANA,
    /* キーボードのタイプ */
    val keyboardType: KeyboardType = KeyboardType.NORMAL,
    /* Shiftキーの状態 */
    val shiftState: ShiftState = ShiftState.LOWERCASE,
    val lastShiftPressTime: Long = 0L,
    /* Controlキーの状態 */
    val isCtrlPressed: Boolean = false,
    /* フリック入力のON/OFF */
    val isFlick: Boolean = false,

    /* === BUFFER === */
    /* 変換候補 */
    val candidates: List<Candidate> = emptyList(),
    val selectedIndex: Int = -1,
    /* 日本語入力の状態 */
    val composingText: String = "",
    val currentSkkState: SkkState = SkkState(),

    /* 辞書登録のキーと候補 */
    val registrationKeyList: List<RegistrationKey> = emptyList(),
    /* 変換前の状態 */
    val savedSkkState: List<SkkState> = emptyList(),
) {
    /* 登録モード */
    val isRegistering: Boolean
        get() = registrationKeyList.lastOrNull()?.lookupKey?.isNotEmpty() ?: false
    val registrationKey: RegistrationKey
        get() = registrationKeyList.lastOrNull() ?: RegistrationKey()

    /**
     * 登録モードに入る
     */
    fun initRegistration(
        keyText: String
    ): KeyboardState {
        return copy(
            currentSkkState = SkkState(),
            registrationKeyList = registrationKeyList + RegistrationKey(
                lookupKey = keyText,
                candidate = "",
            ),
        ).clear()
    }

    /**
     * 辞書登録時の文字列を組み立てる
     */
    fun buildText(): String {
        return buildString {
            registrationKeyList.forEach { (lookupKey, candidate) ->
                if (lookupKey.isNotEmpty()) {
                    append("[登録]$lookupKey$DICT_DELIMITER$candidate")
                }
            }
        }
    }

    /* 選択されている候補 */
    val selectedCandidate: Candidate?
        get() = candidates.getOrNull(selectedIndex) ?: candidates.firstOrNull()

    /**
     *  currentSkkStateのモード切り換え
     */
    fun changeMode(mode: SkkMode): KeyboardState {
        return copy(
            currentSkkState = currentSkkState.copy(
                skkMode = mode,
            ),
        )
    }

    /**
     * 辞書登録キーの編集
     */
    fun updateCandidate(lookupKey: String, candidate: String): KeyboardState {
        val list = registrationKeyList.dropLast(1)
        return copy(
            registrationKeyList = list + RegistrationKey(
                lookupKey = lookupKey,
                candidate = candidate,
            )
        )
    }

    /**
     * 状態の初期化
     * (辞書登録関係は初期化しない)
     */
    fun clear(): KeyboardState {
        return copy(
            composingText = "",
            candidates = emptyList(),
            selectedIndex = -1,
            currentSkkState = SkkState(),
        )
    }

    /**
     * 状態と辞書登録モードの一括初期化
     */
    fun resetAll(): KeyboardState {
        return copy(
            composingText = "",
            candidates = emptyList(),
            selectedIndex = -1,
            currentSkkState = SkkState(),
            registrationKeyList = emptyList(),
            savedSkkState = emptyList(),
        )
    }

    /**
     * Shiftキーが押されたときの処理
     */
    fun processShift(now: Long = System.currentTimeMillis()): KeyboardState {
        val timing = now - lastShiftPressTime < 500L
        val isCapsLock =
            inputMode == InputMode.HALF_ASCII || inputMode == InputMode.FULL_ASCII || currentSkkState.skkMode == SkkMode.ABBREV

        val nextShift = when (shiftState) {
            ShiftState.LOWERCASE -> ShiftState.SHIFTED
            ShiftState.SHIFTED -> if (timing && isCapsLock) ShiftState.CAPS_LOCK else ShiftState.LOWERCASE
            ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
        }

        return copy(lastShiftPressTime = now, shiftState = nextShift)
    }
}
