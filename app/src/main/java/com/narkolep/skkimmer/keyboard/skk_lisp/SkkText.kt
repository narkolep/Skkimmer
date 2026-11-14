package com.narkolep.skkimmer.keyboard.skk_lisp

public object SkkText {
    private val MARKER = Regex("#([0-9])")

    /** k 番目の "#N" を、数字リストの k 番目を変換タイプ N で変換した文字列に置き換える */
    public fun expandNumbers(text: String, numbers: List<String>): String {
        if (numbers.isEmpty() || '#' !in text) return text
        var index = 0
        return try {
            MARKER.replace(text) { m ->
                val num = numbers.getOrNull(index++) ?: throw EvalException("数字が足りません")
                SkkNum.convert(num, m.groupValues[1].toInt())
            }
        } catch (e: LispException) {
            text // 展開できないなら中途半端に直さず元のまま
        }
    }
}