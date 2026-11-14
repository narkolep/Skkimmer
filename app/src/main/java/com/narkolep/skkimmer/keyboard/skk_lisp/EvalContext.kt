package com.narkolep.skkimmer.keyboard.skk_lisp

import java.time.Clock


internal class EvalException(message: String) : LispException(message)

public class EvalContext(
    public val numList: List<String> = emptyList(),
    public val henkanKey: String? = null,
    public val clock: Clock = Clock.systemDefaultZone(),
    public val userFullName: String = "",
    public val userMailAddress: String = "",
)

/** lambda の引数だけを持つ不変の環境 */
internal class Env private constructor(private val vars: Map<String, LispValue>) {
    operator fun get(name: String): LispValue? = vars[name]
    fun bind(names: List<String>, values: List<LispValue>) = Env(vars + names.zip(values))
    companion object { val EMPTY = Env(emptyMap()) }
}