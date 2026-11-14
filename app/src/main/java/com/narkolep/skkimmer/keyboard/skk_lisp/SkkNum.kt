package com.narkolep.skkimmer.keyboard.skk_lisp

internal object SkkNum {
    private const val ZENKAKU = "０１２３４５６７８９"
    private const val KANJI = "〇一二三四五六七八九"
    private const val DAIJI = "零壱弐参肆伍陸漆捌玖"
    private val KETA_KANJI = arrayOf("", "十", "百", "千")
    private val KETA_DAIJI = arrayOf("", "拾", "佰", "阡")
    private val BIG_KANJI = arrayOf("", "万", "億", "兆", "京")
    private val BIG_DAIJI = arrayOf("", "萬", "億", "兆", "京")

    fun convert(digits: String, type: Int): String {
        if (digits.isEmpty() || !digits.all { it in '0'..'9' }) bad("数字ではありません")
        return when (type) {
            0 -> digits
            1 -> digits.map { ZENKAKU[it - '0'] }.joinToString("") // 全角
            2 -> digits.map { KANJI[it - '0'] }.joinToString("") // 二〇二六
            3 -> toKanji(digits, KANJI, KETA_KANJI, BIG_KANJI) // 二千二十六
            5 -> toKanji(digits, DAIJI, KETA_DAIJI, BIG_DAIJI)
            8 -> toCommaSeparated(digits)
            else -> bad("未対応の数字変換タイプ: $type")
        }
    }

    private fun toKanji(
        digits: String,
        numberStrings: String,
        keta: Array<String>,
        big: Array<String>
    ): String {
        val groups = digits.reversed().chunked(4).map { it.reversed() }
        if (groups.size > big.size) bad("桁数が多すぎます")
        val sb = StringBuilder()
        for ((i, g) in groups.withIndex()) {
            var s = group4(g, numberStrings, keta)
            if (s.isEmpty()) continue
            if (s == "千" && i > 0) s = "一千"
            sb.insert(0, s + big[i])
        }
        return if (sb.isEmpty()) numberStrings.substring(0, 1) else sb.toString()
    }

    private fun group4(
        g: String,
        numberStrings: String,
        keta: Array<String>
    ): String {
        val sb = StringBuilder()
        for ((idx, c) in g.withIndex()) {
            val pos = g.length - 1 - idx
            if (c == '0') continue
            if (!(c == '1' && pos > 0)) sb.append(numberStrings[c - '0'])
            sb.append(keta[pos])
        }
        return sb.toString()
    }

    private fun toCommaSeparated(number: String): String {
        if (number.isEmpty()) return ""
        return number.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
    }
}