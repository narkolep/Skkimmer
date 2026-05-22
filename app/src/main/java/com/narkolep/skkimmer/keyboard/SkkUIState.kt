package com.narkolep.skkimmer.keyboard

enum class ShiftState {
    LOWERCASE,
    SHIFTED,
    CAPS_LOCK
}
enum class InputMode {
    HALF_ASCII,
    FULL_ASCII,
    HIRAGANA,
    KATAKANA,
    HALF_KATAKANA,
    EMOJI,
    NUMERIC
}
enum class SkkState {
    NORMAL,
    MIDASHI,
    OKURIGANA,
    HENKAN,
    ABBREV
}

/**
 * キーボードの状態
 */
data class SkkUIState(
    /* === STATE === */
    /* 入力モード */
    val inputMode: InputMode = InputMode.HIRAGANA,
    /* 日本語入力の状態 */
    val skkState: SkkState = SkkState.NORMAL,
    /* Shiftキーの状態 */
    val shiftState: ShiftState = ShiftState.LOWERCASE,
    val lastShiftPressTime: Int = 0,
    /* Controlキーの状態 */
    val isCtrlPressed: Boolean = false,
    /* フリック入力のON/OFF */
    val isFlick: Boolean = false,

    /* === BUFFER === */
    /* 変換候補 */
    val candidates: List<String> = emptyList(),
    val selectedIndex: Int = -1,
    /* 日本語入力中の文字列 */
    val composingText: String = "",
    val midashiText: String = "",
    val okuriganaText: String = "",
    val okuriganaTrigger: String = "",
    /* 辞書登録モードのトリガー */
    val tourokuFlag: String = "",
    /* 辞書登録前の状態を保存しておく */
    val oldMidashiText: String = "",
    val oldOkuriganaText: String = "",
    val oldOkuriganaTrigger: String =""
)

/**
 * 状態の初期化
 */
fun SkkUIState.clear(): SkkUIState {
    return copy(
        skkState = SkkState.NORMAL,
        midashiText = "",
        composingText = "",
        okuriganaText = "",
        okuriganaTrigger = "",
        candidates = emptyList(),
        selectedIndex = -1
    )
}

/**
 * 登録モード解除
 */
fun SkkUIState.tourokuClear(): SkkUIState {
    return copy(
        tourokuFlag = "",
        oldMidashiText = "",
        oldOkuriganaText = "",
        oldOkuriganaTrigger = ""
    )
}