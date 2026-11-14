package com.narkolep.skkimmer.keyboard.skk_lisp

internal object Globals {
    fun get(name: String, ctx: EvalContext): LispValue? = when (name) {
        "fill-column" -> LInt(70)
        "comment-start" -> LStr("#")
        "user-full-name" -> LStr(ctx.userFullName)
        "user-mail-address" -> LStr(ctx.userMailAddress)
        "skk-num-list" -> if (ctx.numList.isEmpty()) LNil else LList(ctx.numList.map(::LStr))
        else -> null
    }
}