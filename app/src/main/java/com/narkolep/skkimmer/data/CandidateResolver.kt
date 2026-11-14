package com.narkolep.skkimmer.data

import com.narkolep.skkimmer.keyboard.SkkConstants.DICT_ANNOTATION
import com.narkolep.skkimmer.keyboard.skk_lisp.EvalContext
import com.narkolep.skkimmer.keyboard.skk_lisp.Evaluator
import com.narkolep.skkimmer.keyboard.skk_lisp.SkkText
import java.time.Clock

data class RawCandidate(
    val text: String,
    val annotation: String? = null
)

fun List<String>.toRawCandidates(): List<RawCandidate> {
    return map { candidate ->
        val separatorIndex = candidate.indexOf(DICT_ANNOTATION)

        if (separatorIndex == -1) {
            RawCandidate(text = candidate)
        } else {
            RawCandidate(
                text = candidate.substring(0, separatorIndex),
                annotation = candidate.substring(separatorIndex + 1)
            )
        }
    }
}

data class Candidate(
    val raw: String,              // 辞書に書かれていた文字列(学習・保存はこちら)
    val text: String,             // 表示・確定する文字列
    val annotation: String? = null,
)

class CandidateResolver(
    private val clockProvider: () -> Clock = { Clock.systemDefaultZone() },
    private val onFailure: (expr: String, cause: Throwable) -> Unit = { _, _ -> },
) {
    fun resolve(raws: List<RawCandidate>, headword: String, numbers: List<String>): List<Candidate> {
        val ctx = EvalContext(numList = numbers, henkanKey = headword, clock = clockProvider())
        val seen = HashSet<String>()
        val out = ArrayList<Candidate>(raws.size)
        for (r in raws) {
            val expanded = SkkText.expandNumbers(r.text, numbers)
            val text = if (expanded.startsWith("(")) {
                Evaluator.evaluate(expanded, ctx).getOrElse { e -> onFailure(expanded, e); expanded }
            } else expanded
            if (seen.add(text)) out += Candidate(raw = r.text, text = text, annotation = r.annotation)
        }
        return out
    }
}