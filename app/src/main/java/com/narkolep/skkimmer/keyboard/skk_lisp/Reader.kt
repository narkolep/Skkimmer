package com.narkolep.skkimmer.keyboard.skk_lisp

open class LispException(message: String) : Exception(message)
class ReaderException(message: String, pos: Int) : LispException("$message (位置 $pos)")

internal class Reader private constructor(private val src: String) {
    private var pos = 0
    private var depth = 0

    companion object {
        private const val MAX_DEPTH = 32
        private const val MAX_LENGTH = 4096
        private val INT_RE = Regex("[+-]?[0-9]+")
        private val FLOAT_RE = Regex("[+-]?([0-9]+\\.[0-9]+|\\.[0-9]+)")

        fun read(src: String): LispValue {
            if (src.length > MAX_LENGTH) throw ReaderException("式が長すぎます", 0)
            val r = Reader(src)
            val v = r.readExpr()
            r.skipBlank()
            if (r.pos < src.length) throw ReaderException("式の後ろに余分な文字があります", r.pos)
            return v
        }
    }

    // ---- 共通 ----

    private fun isDelimiter(c: Char) = c.isWhitespace() || c in "()\"';"

    private fun skipBlank() {
        while (pos < src.length) {
            val c = src[pos]
            when {
                c.isWhitespace() -> pos++
                c == ';' -> while (pos < src.length && src[pos] != '\n') pos++
                else -> return
            }
        }
    }

    private fun readExpr(): LispValue {
        if (++depth > MAX_DEPTH) throw ReaderException("ネストが深すぎます", pos)
        try {
            skipBlank()
            if (pos >= src.length) throw ReaderException("式が途中で終わっています", pos)
            return when (src[pos]) {
                '(' -> readList()
                ')' -> throw ReaderException("対応する ( がない ) です", pos)
                '\'' -> readQuote()
                '"' -> readString()
                '?' -> readChar()
                '#' -> readHash()
                else -> readAtom()
            }
        } finally {
            depth--
        }
    }

    // ---- リスト ----

    private fun readList(): LispValue {
        pos++ // '('
        val items = ArrayList<LispValue>()
        while (true) {
            skipBlank()
            if (pos >= src.length) throw ReaderException("括弧が閉じていません", pos)
            if (src[pos] == ')') { pos++; break }
            items += readExpr()
        }
        return if (items.isEmpty()) LNil else LList(items)   // () と nil を同一視
    }

    private fun readQuote(): LispValue {
        pos++ // '\''
        return LList(listOf(LSym("quote"), readExpr()))
    }

    // ---- 文字列・文字 ----

    private fun readString(): LispValue {
        pos++ // 開きの "
        val sb = StringBuilder()
        while (pos < src.length) {
            val c = src[pos++]
            when (c) {
                '"' -> return LStr(sb.toString())
                '\\' -> {
                    val cp = readEscape()
                    if (cp >= 0) sb.appendCodePoint(cp)   // -1 は「何も出さない」
                }
                else -> sb.append(c)
            }
        }
        throw ReaderException("文字列が閉じていません", pos)
    }

    /** '\' を読み終えた位置から呼ぶ。コードポイントを返す。-1 は出力なし。 */
    private fun readEscape(): Int {
        if (pos >= src.length) throw ReaderException("バックスラッシュで終わっています", pos)
        val c = src[pos++]
        return when (c) {
            in '0'..'7' -> {                       // \057 など(最大3桁)
                var n = c - '0'
                var count = 1
                while (count < 3 && pos < src.length && src[pos] in '0'..'7') {
                    n = n * 8 + (src[pos++] - '0'); count++
                }
                n
            }
            'x' -> {                               // \x2f など
                var n = 0
                var count = 0
                while (count < 6 && pos < src.length && src[pos].digitToIntOrNull(16) != null) {
                    n = n * 16 + src[pos++].digitToInt(16); count++
                }
                if (count == 0 || n > 0x10FFFF) throw ReaderException("不正な \\x 表記です", pos)
                n
            }
            'n' -> '\n'.code
            't' -> '\t'.code
            ' ', '\n' -> -1                        // "\ " と行末の \ は無視される
            else -> {                              // \" \\ など。その文字自身
                pos--
                val cp = src.codePointAt(pos)
                pos += Character.charCount(cp)
                cp
            }
        }
    }

    private fun readChar(): LispValue {
        pos++ // '?'
        if (pos >= src.length) throw ReaderException("? の後ろに文字がありません", pos)
        val cp = if (src[pos] == '\\') {
            pos++
            readEscape().also { if (it < 0) throw ReaderException("不正な文字リテラルです", pos) }
        } else {
            src.codePointAt(pos).also { pos += Character.charCount(it) }
        }
        if (pos < src.length && !isDelimiter(src[pos])) {
            throw ReaderException("文字リテラルの後ろが不正です", pos)
        }
        return LInt(cp.toLong())
    }

    // ---- 数値・シンボル ----

    private fun readHash(): LispValue {
        pos++ // '#'
        val radix = when (src.getOrNull(pos)) {
            'x', 'X' -> 16
            'o', 'O' -> 8
            'b', 'B' -> 2
            else -> throw ReaderException("未対応の # 表記です", pos)
        }
        pos++
        val start = pos
        while (pos < src.length && !isDelimiter(src[pos])) pos++
        val n = src.substring(start, pos).toLongOrNull(radix)
            ?: throw ReaderException("不正な数値です", start)
        return LInt(n)
    }

    private fun readAtom(): LispValue {
        val start = pos
        while (pos < src.length && !isDelimiter(src[pos])) pos++
        val tok = src.substring(start, pos)
        return when {
            tok == "nil" -> LNil
            tok == "." -> throw ReaderException("ドット対は未対応です", start)
            INT_RE.matches(tok) ->
                LInt(tok.toLongOrNull() ?: throw ReaderException("整数が大きすぎます", start))
            FLOAT_RE.matches(tok) -> LFloat(tok.toDouble())
            else -> LSym(tok)
        }
    }
}