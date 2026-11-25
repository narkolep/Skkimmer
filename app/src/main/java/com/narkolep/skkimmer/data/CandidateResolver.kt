package com.narkolep.skkimmer.data

import com.narkolep.skkimmer.keyboard.SkkConstants.DICT_ANNOTATION
import com.narkolep.skkimmer.keyboard.skk_lisp.EvalContext
import com.narkolep.skkimmer.keyboard.skk_lisp.Evaluator
import com.narkolep.skkimmer.keyboard.skk_lisp.SkkText
import java.time.Clock

/**
 * 辞書から得られた候補
 * @param raw 辞書に書かれていた文字列 (学習・保存用)
 * @param text 変換後の文字列 (表示・入力用)
 * @param annotation 辞書の注釈
 */
data class Candidate(
    val raw: String,
    val text: String,
    val annotation: String? = null,
)

/**
 * 候補のリストを変換する
 */
class CandidateResolver(
    private val clockProvider: () -> Clock = { Clock.systemDefaultZone() },
    private val onFailure: (expr: String, cause: Throwable) -> Unit = { _, _ -> },
) {
    fun resolve(
        raws: List<String>,
        headword: String,
        numbers: List<String>
    ): List<Candidate> {
        /* annotationを分離 */
        val annotation = raws.map { it.substringAfter(DICT_ANNOTATION, "") }
        val separated = raws.map { it.substringBefore(DICT_ANNOTATION) }

        val ctx = EvalContext(numList = numbers, henkanKey = headword, clock = clockProvider())

        val candidates = separated.map { text ->
            /* 数値の変換 */
            val expanded = SkkText.expandNumbers(text, numbers)

            /* Lisp式の評価 */
            if (expanded.startsWith("(")) {
                Evaluator.evaluate(expanded, ctx).getOrElse { e ->
                    onFailure(expanded, e); expanded
                }
            } else expanded
        }

        return raws.zip(candidates).zip(annotation) {
            (raw, text), annotation ->
                Candidate(
                    raw = raw,
                    text = text,
                    annotation = annotation.ifEmpty { null }
                )
            }.distinctBy { it.text }
    }
}