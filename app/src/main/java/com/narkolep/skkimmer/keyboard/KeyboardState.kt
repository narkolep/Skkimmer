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
    NUMERIC
}

enum class InputMode {
    HALF_ASCII,
    FULL_ASCII,
    HIRAGANA,
    KATAKANA,
    HALF_KATAKANA
}

enum class SkkState {
    NORMAL,
    MIDASHI,
    OKURIGANA,
    HENKAN,
    ABBREV
}

data class RegistrationKey(
    val lookupKey: String = "",
    val candidate: String = ""
) {
    fun buildText(): String {
        return if (lookupKey.isEmpty()) "" else "[登録]$lookupKey$DICT_DELIMITER$candidate"
    }
}

/**
 * キーボードの状態
 */
data class KeyboardState(
    /* === STATE === */
    /* 入力モード(英数/日本語など) */
    val inputMode: InputMode = InputMode.HIRAGANA,
    /* 変換の状態 */
    val skkState: SkkState = SkkState.NORMAL,
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
    val previousSkkState: SkkState = SkkState.NORMAL,
    /* 日本語入力中の文字列 */
    val composingText: String = "",
    val midashiText: String = "",
    val okuriganaText: String = "",
    val okuriganaTrigger: String = "",

    /* 辞書登録のキーと候補 */
    val registrationKey: RegistrationKey = RegistrationKey(),
    /* 辞書登録前の状態 */
    val oldSkkState: SkkState = SkkState.NORMAL,
    val oldMidashiText: String = "",
    val oldOkuriganaText: String = "",
    val oldOkuriganaTrigger: String =""
) {
    /* 登録モードであるか */
    val isRegistering: Boolean
        get() = registrationKey.lookupKey.isNotEmpty()

    /* 選択されている候補 */
    val selectedCandidate: Candidate?
        get() = candidates.getOrNull(selectedIndex) ?: candidates.firstOrNull()
}

/**
 * 状態の初期化
 * (辞書登録関係は初期化しない)
 */
fun KeyboardState.clear(): KeyboardState {
    return copy(
        skkState = SkkState.NORMAL,
        midashiText = "",
        composingText = "",
        okuriganaText = "",
        okuriganaTrigger = "",
        candidates = emptyList(),
        selectedIndex = -1,
        previousSkkState = SkkState.NORMAL
    )
}

/**
 * 登録モードに入る
 */
fun KeyboardState.tourokuInit(
    keyText: String
): KeyboardState {
    return copy(
        /* 登録前の状態を保存 */
        oldSkkState = skkState,
        oldMidashiText = midashiText,
        oldOkuriganaText = okuriganaText,
        oldOkuriganaTrigger = okuriganaTrigger,
        /* 登録モード */
        registrationKey = RegistrationKey(
            lookupKey = keyText,
            candidate = ""
        )
    ).clear()
}

/**
 * 登録モード解除
 */
fun KeyboardState.tourokuClear(): KeyboardState {
    return copy(
        oldSkkState = SkkState.NORMAL,
        oldMidashiText = "",
        oldOkuriganaText = "",
        oldOkuriganaTrigger = "",
        registrationKey = RegistrationKey()
    )
}

/**
 * 状態と辞書登録モードの一括初期化
 */
fun KeyboardState.resetAll(): KeyboardState {
    return copy(
        skkState = SkkState.NORMAL,
        midashiText = "",
        composingText = "",
        okuriganaText = "",
        okuriganaTrigger = "",
        candidates = emptyList(),
        selectedIndex = -1,
        previousSkkState = SkkState.NORMAL,
        oldSkkState = SkkState.NORMAL,
        oldMidashiText = "",
        oldOkuriganaText = "",
        oldOkuriganaTrigger = "",
        registrationKey = RegistrationKey()
    )
}

/**
 * Shiftキーが押されたときの処理
 */
fun KeyboardState.processShift(now: Long = System.currentTimeMillis()): KeyboardState {
    val timing = now - lastShiftPressTime < 500L
    val isCapsLock = inputMode == InputMode.HALF_ASCII || inputMode == InputMode.FULL_ASCII || skkState == SkkState.ABBREV
    val nextShift = when (shiftState) {
        ShiftState.LOWERCASE -> ShiftState.SHIFTED
        ShiftState.SHIFTED -> if (timing && isCapsLock) ShiftState.CAPS_LOCK else ShiftState.LOWERCASE
        ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
    }

    return copy(lastShiftPressTime = now, shiftState = nextShift)
}