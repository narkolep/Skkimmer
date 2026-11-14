package com.narkolep.skkimmer.keyboard.skk_lisp

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * SKKの固有関数
 */
internal fun registerSkk(def: Registrar) {
    def("skk-gengo-to-ad") { a, ctx ->
        a.checkSize(0..2)
        val head = optStr(a.arg(0)); val tail = optStr(a.arg(1))
        val year = ctx.numList.firstOrNull()?.toIntOrNull() ?: bad("数字がありません")
        val key = ctx.henkanKey ?: bad("見出し語がありません")
        val name = key.takeWhile { it !in '0'..'9' }          // "へいせい17ねん" → "へいせい"
        val ad = Gengos.toAd(name, year) ?: bad("未知の元号: $name")
        LStr(head + ad + tail)
    }

    def("skk-ad-to-gengo") { a, ctx ->
        a.checkSize(1..4)
        val index = a[0].int().toInt().also { if (it !in 0..1) bad("gengo-index は 0 か 1") }
        val divider = optStr(a.arg(1)); val tail = optStr(a.arg(2))
        val notGannen = a.arg(3) !is LNil
        val ad = ctx.numList.firstOrNull()?.toIntOrNull() ?: bad("数字がありません")
        val gy = Gengos.fromAd(ad) ?: bad("元号より前の年です")
        val name = if (index == 0) gy.gengo.name else gy.gengo.initial
        val nengo = if (gy.year == 1 && !notGannen) "元" else gy.year.toString()
        LStr(name + divider + nengo + tail)
    }

    def("skk-default-current-date") { a, _ ->
        a.checkSize(7..8)                       // 第6引数(月の表示形式)と第8引数(時刻)は無視
        defaultCurrentDate(a[0], a[1], a[2], a[3], a[4], a[6])
    }

    def("skk-current-date") { a, ctx ->
        a.checkSize(0..3)
        val info = dateInfo(ZonedDateTime.now(ctx.clock))
        present(a.arg(0), info, a.arg(1), LInt(1), a.arg(2), ctx)
    }

    def("skk-relative-date") { a, ctx ->
        if (a.size < 3) bad("引数が足りません")
        var yy = 0L; var mm = 0L; var dd = 0L
        var i = 3
        while (i < a.size) {                    // :yy 1 :mm 2 :dd 3 の組を読む
            val key = a[i] as? LSym ?: bad("キーワードが必要です")
            val v = (a.getOrNull(i + 1) as? LInt)?.v ?: bad("キーワードの値がありません")
            when (key.name) { ":yy" -> yy = v; ":mm" -> mm = v; ":dd" -> dd = v }
            i += 2
        }
        val t = ZonedDateTime.now(ctx.clock)
        // 月・日あふれは繰り上げる(C の mktime と同じ)
        val date = LocalDate.of(t.year, 1, 1).plusYears(yy)
            .plusMonths(t.monthValue - 1L + mm).plusDays(t.dayOfMonth - 1L + dd)
        val shifted = date.atTime(t.toLocalTime()).atZone(t.zone)
        present(a[0], dateInfo(shifted), a[1], LNil, a[2], ctx)
    }

    def("skk-version") { _, _ -> LStr("Skkimmer/1.0.0") }

    def("skk-times") { a, ctx ->                // 数字リストの積
        a.checkSize(0..0)
        LStr(ctx.numList.fold(1L) { acc, s ->
            Math.multiplyExact(acc, s.toLongOrNull() ?: bad("数字ではありません"))
        }.toString())
    }

    def("skk-gadget-units-conversion") { a, _ ->
        a.checkSize(3..3)
        val from = a[0].str()
        val value = a[1].number()
        val to = a[2].str()
        val rule = UNIT_RULES.firstOrNull { it.from == from && it.to == to }
            ?: bad("未対応の単位変換: $from → $to")
        LStr(formatG(value * rule.ratio) + rule.to)
    }
}

private val DOW_JA = listOf("月", "火", "水", "木", "金", "土", "日")
private data class UnitRule(val from: String, val to: String, val ratio: Double)

/* 単位変換表 */
private val UNIT_RULES = listOf(
    UnitRule("mile", "km",   1.6093),
    UnitRule("mile", "yard", 1760.0),
    UnitRule("yard", "feet", 3.0),
    UnitRule("yard", "cm",   91.44),
    UnitRule("feet", "inch", 12.0),
    UnitRule("feet", "cm",   30.48),
    UnitRule("inch", "feet", 0.0833),
    UnitRule("inch", "cm",   2.54),
)

private fun dateInfo(t: ZonedDateTime): LispValue = LList(listOf(
    LStr(t.year.toString()), LStr(CTS_MON[t.monthValue - 1]), LStr(t.dayOfMonth.toString()),
    LStr(CTS_DOW[t.dayOfWeek.value - 1]), LStr(t.hour.toString()),
    LStr(t.minute.toString()), LStr(t.second.toString())))

private fun simpleFormat(fmt: String, vararg args: String): String {
    val sb = StringBuilder(); var i = 0; var ai = 0
    while (i < fmt.length) {
        val c = fmt[i++]
        if (c != '%') { sb.append(c); continue }
        when (fmt.getOrNull(i++)) {
            's' -> sb.append(args.getOrNull(ai++) ?: bad("format: 引数が足りません"))
            '%' -> sb.append('%')
            else -> bad("format: 未対応の指定です")
        }
    }
    return sb.toString()
}

private fun defaultCurrentDate(
    info: LispValue, format: LispValue, numType: LispValue,
    gengo: LispValue, gengoIndex: LispValue, dowIndex: LispValue,
): LispValue {
    val f = (info as? LList)?.items?.takeIf { it.size == 7 } ?: bad("date-information が不正です")
    val year = f[0].str().toIntOrNull() ?: bad("年が不正です")
    val month = CTS_MON.indexOf(f[1].str()) + 1
    val day = f[2].str().toIntOrNull() ?: bad("日が不正です")
    if (month < 1 || day !in 1..31) bad("日付が不正です")
    val type = numType.int().toInt()

    val yearStr = if (gengo is LNil) {
        SkkNum.convert(year.toString(), type)                     // 西暦
    } else {
        val gy = Gengos.fromAd(year, month, day) ?: bad("元号より前の年です")
        val name = if (gengoIndex.int() == 0L) gy.gengo.name else gy.gengo.initial
        name + (if (gy.year == 1) "元" else SkkNum.convert(gy.year.toString(), type))
    }
    val dowEn = f[3].str()
    val dowIdx = CTS_DOW.indexOf(dowEn).also { if (it < 0) bad("曜日が不正です") }
    val dow = if (dowIndex is LNil) dowEn else DOW_JA[dowIdx]     // nil なら英語のまま
    val fmt = if (format is LNil) "%s年%s月%s日(%s)" else format.str()
    return LStr(simpleFormat(fmt, yearStr,
        SkkNum.convert(month.toString(), type), SkkNum.convert(day.toString(), type), dow))
}

// pp が nil なら既定の整形、あれば lambda を呼ぶ
private fun present(pp: LispValue, info: LispValue, format: LispValue,
                    gengo: LispValue, andTime: LispValue, ctx: EvalContext): LispValue =
    if (pp is LNil) defaultCurrentDate(info, LNil, LInt(1), LInt(1), LInt(0), LInt(0))
    else Evaluator.callLambda(pp, listOf(info, format, gengo, andTime), ctx)

private fun LispValue.number(): Double = when (this) {
    is LInt -> v.toDouble()
    is LFloat -> v
    else -> bad("数値が必要です")
}

/* 有効桁数6桁でフォーマットする */
private fun formatG(x: Double): String {
    if (x.isNaN() || x.isInfinite()) bad("数値が不正です")
    if (x == 0.0) return "0"
    val bd = BigDecimal(x).round(MathContext(6, RoundingMode.HALF_EVEN))
    val exp = bd.precision() - bd.scale() - 1          // 10進の指数部
    if (exp in -4..5) return bd.stripTrailingZeros().toPlainString()

    /* 指数表記: 仮数部の末尾の0を削って  d.ddde+XX  の形にする */
    val digits = bd.unscaledValue().abs().toString().trimEnd('0').ifEmpty { "0" }
    val mantissa = if (digits.length > 1) "${digits[0]}.${digits.substring(1)}" else digits
    val sign = if (bd.signum() < 0) "-" else ""
    val expSign = if (exp < 0) "-" else "+"
    return "$sign${mantissa}e$expSign${abs(exp).toString().padStart(2, '0')}"
}