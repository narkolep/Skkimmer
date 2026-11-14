package com.narkolep.skkimmer.keyboard.skk_lisp

public object SkkQuote {
    /** 辞書の行構造を壊す文字 */
    private val LINE_BREAKERS = charArrayOf('/', ';', '\n', '\r')

    /** 辞書に書くときにエスケープが必要か */
    public fun needsQuote(word: String): Boolean =
        word.startsWith("(") || word.any { it in LINE_BREAKERS }

    /**
     * ユーザーが入力した文字列を、辞書の候補として書ける形に変換する。
     * 普通の文字列はそのまま返し、必要なときだけ (concat "...") にする。
     * ※辞書から選んだ既存の候補(raw)には使わない(二重にエスケープされる)。
     */
    public fun quoteCandidate(word: String): String {
        if (!needsQuote(word)) return word
        val sb = StringBuilder("(concat \"")
        for (ch in word) {                       // サロゲートペアもそのまま通る
            when (ch) {
                '/'  -> sb.append("\\057")
                ';'  -> sb.append("\\073")
                '\n' -> sb.append("\\012")
                '\r' -> sb.append("\\015")
                '"'  -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                else -> sb.append(ch)
            }
        }
        return sb.append("\")").toString()
    }
}