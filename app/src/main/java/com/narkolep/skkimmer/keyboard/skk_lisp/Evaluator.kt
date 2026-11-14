package com.narkolep.skkimmer.keyboard.skk_lisp

public object Evaluator {
    /**
     * 公開API
     */
    public fun evaluate(src: String, ctx: EvalContext): Result<String> = try {
        val v = eval(Reader.read(src), ctx, Env.EMPTY)
        if (v is LStr) Result.success(v.v)
        else Result.failure(EvalException("結果が文字列ではありません"))
    } catch (e: LispException) { Result.failure(e) }
    catch (e: RuntimeException) { Result.failure(e) }
    catch (e: StackOverflowError) { Result.failure(e) }

    internal fun eval(e: LispValue, ctx: EvalContext, env: Env): LispValue = when (e) {
        is LInt, is LFloat, is LStr, LNil -> e
        is LSym -> evalSymbol(e.name, ctx, env)
        is LList -> evalList(e, ctx, env)
    }

    private fun evalSymbol(name: String, ctx: EvalContext, env: Env): LispValue {
        if (name.startsWith(":")) return LSym(name)            // キーワード
        return env[name]                                        // lambda の引数が優先
            ?: Globals.get(name, ctx)
            ?: throw EvalException("void-variable: $name")
    }

    private fun evalList(l: LList, ctx: EvalContext, env: Env): LispValue {
        // Reader は空リストを LNil にするので、items[0] は必ず存在する
        val head = l.items[0] as? LSym ?: throw EvalException("関数位置にシンボルがありません")
        when (head.name) {
            "quote" -> {
                if (l.items.size != 2) throw EvalException("quote の引数は1つです")
                return l.items[1]
            }
            "lambda" -> return l
        }
        val fn = Builtins.table[head.name] ?: throw EvalException("void-function: ${head.name}")
        val args = l.items.drop(1).map { eval(it, ctx, env) }   // 引数を先に評価
        return fn(args, ctx)
    }

    /**
     *  (lambda (仮引数...) 本体...) を呼ぶ
     */
    internal fun callLambda(fn: LispValue, args: List<LispValue>, ctx: EvalContext): LispValue {
        val l = (fn as? LList)?.items ?: throw EvalException("lambda ではありません")
        if (l.size < 2 || (l[0] as? LSym)?.name != "lambda") throw EvalException("lambda ではありません")
        val names = when (val p = l[1]) {
            is LList -> p.items.map { (it as? LSym)?.name ?: throw EvalException("仮引数が不正です") }
            LNil -> emptyList()
            else -> throw EvalException("仮引数リストが不正です")
        }
        if (names.size != args.size) throw EvalException("引数の数が合いません")
        val env = Env.EMPTY.bind(names, args)
        var result: LispValue = LNil
        for (body in l.drop(2)) result = eval(body, ctx, env)
        return result
    }
}
