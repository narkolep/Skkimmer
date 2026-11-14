package com.narkolep.skkimmer.keyboard.skk_lisp

/**
 * データ構造
 */
internal sealed interface LispValue

internal data class LInt(val v: Long) : LispValue
internal data class LFloat(val v: Double) : LispValue
internal data class LStr(val v: String) : LispValue
internal data class LSym(val name: String) : LispValue
internal data class LList(val items: List<LispValue>) : LispValue
internal data object LNil : LispValue