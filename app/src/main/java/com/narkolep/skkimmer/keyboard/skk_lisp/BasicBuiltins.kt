package com.narkolep.skkimmer.keyboard.skk_lisp

import java.time.ZonedDateTime

/**
 * 基本関数
 */
internal fun registerBasic(def: Registrar) {
    def("concat") { a, _ -> LStr(a.joinToString("") { it.str() }) }   // 1個ならそのまま返る

    def("car") { a, _ ->
        a.checkSize(1..1)
        when (val x = a[0]) {
            is LList -> x.items[0]
            LNil -> LNil
            else -> bad("car: リストが必要です")
        }
    }

    def("-") { a, _ ->
        when {
            a.isEmpty() -> LInt(0)
            a.all { it is LInt } -> {
                val n = a.map { (it as LInt).v }
                LInt(if (n.size == 1) Math.negateExact(n[0])
                else n.drop(1).fold(n[0]) { x, y -> Math.subtractExact(x, y) })
            }
            else -> {
                val d = a.map { when (it) { is LInt -> it.v.toDouble(); is LFloat -> it.v; else -> bad("数値が必要です") } }
                LFloat(if (d.size == 1) -d[0] else d.drop(1).fold(d[0]) { x, y -> x - y })
            }
        }
    }

    def("make-string") { a, _ ->
        a.checkSize(2..2)
        val n = a[0].int(); val ch = a[1].int()
        if (n !in 0L..MAX_STRING_LEN || ch !in 0L..0x10FFFFL) bad("make-string: 範囲外です")
        val sb = StringBuilder()
        repeat(n.toInt()) { sb.appendCodePoint(ch.toInt()) }
        LStr(sb.toString())
    }

    def("string-to-char") { a, _ ->
        a.checkSize(1..1)
        val s = a[0].str()
        LInt(if (s.isEmpty()) 0 else s.codePointAt(0).toLong())
    }

    def("symbol-value") { a, ctx ->
        a.checkSize(1..1)
        val s = a[0] as? LSym ?: bad("symbol-value: シンボルが必要です")
        Globals.get(s.name, ctx) ?: bad("void-variable: ${s.name}")
    }

    def("substring") { a, _ ->
        a.checkSize(2..3)
        val s = a[0].str()
        val len = s.codePointCount(0, s.length)
        var from = a[1].int().toInt()
        var to = if (a.arg(2) is LNil) len else a[2].int().toInt()
        if (from < 0) from += len
        if (to < 0) to += len
        if (from !in 0..len || to !in from..len) bad("substring: 範囲外です")
        val b = s.offsetByCodePoints(0, from)
        LStr(s.substring(b, s.offsetByCodePoints(b, to - from)))
    }


    def("string-to-number") { a, _ ->
        a.checkSize(1..1)
        val m = NUM_PREFIX.find(a[0].str())?.value?.trim()
        when {
            m == null -> LInt(0) // 数字でなければ 0
            '.' in m -> LFloat(m.toDouble())
            else -> LInt(m.toLongOrNull() ?: bad("整数が大きすぎます"))
        }
    }

    def("pwd") { _, _ -> LStr("/") }

    def("window-width") { _, _ -> LInt(80) }

    def("current-time-string") { a, ctx ->
        a.checkSize(0..0)
        val t = ZonedDateTime.now(ctx.clock)
        // 例: "Fri Nov 13 12:00:00 2026"  日は空白埋め(Emacs と同じ)
        LStr("%s %s %2d %02d:%02d:%02d %d".format(
            CTS_DOW[t.dayOfWeek.value - 1], CTS_MON[t.monthValue - 1],
            t.dayOfMonth, t.hour, t.minute, t.second, t.year))
    }
}

private const val MAX_STRING_LEN = 10_000L
private val NUM_PREFIX = Regex("^[ \\t]*[+-]?(?:[0-9]+(?:\\.[0-9]+)?|\\.[0-9]+)")