package com.narkolep.skkimmer.keyboard.skk_lisp

internal typealias Builtin = (args: List<LispValue>, ctx: EvalContext) -> LispValue
internal typealias Registrar = (String, Builtin) -> Unit

internal fun bad(msg: String): Nothing = throw EvalException(msg)
internal fun LispValue.str(): String = (this as? LStr)?.v ?: bad("文字列が必要です")
internal fun LispValue.int(): Long = (this as? LInt)?.v ?: bad("整数が必要です")
internal fun List<LispValue>.arg(i: Int): LispValue = getOrNull(i) ?: LNil   // 省略 = nil
internal fun List<LispValue>.checkSize(r: IntRange) { if (size !in r) bad("引数の数が不正です") }
internal fun optStr(v: LispValue): String = if (v is LNil) "" else v.str()

internal val CTS_DOW = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
internal val CTS_MON = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

internal object Builtins {
    private val map = HashMap<String, Builtin>()
    val table: Map<String, Builtin> = map

    init {
        val def: Registrar = { name, fn -> map[name] = fn }
        registerBasic(def)
        registerSkk(def)
    }
}