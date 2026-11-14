package com.narkolep.skkimmer.keyboard.skk_lisp

internal data class Gengo(val name: String, val kana: String, val initial: String, val y: Int, val m: Int, val d: Int) {
    val startKey get() = y * 10000 + m * 100 + d
}
internal data class GengoYear(val gengo: Gengo, val year: Int)

internal object Gengos {
    private val LIST = listOf(
        Gengo("明治", "めいじ", "M", 1868, 10, 23),
        Gengo("大正", "たいしょう", "T", 1912, 7, 30),
        Gengo("昭和", "しょうわ", "S", 1926, 12, 25),
        Gengo("平成", "へいせい", "H", 1989, 1, 8),
        Gengo("令和", "れいわ", "R", 2019, 5, 1),
    )

    fun toAd(name: String, year: Int): Int? =
        LIST.firstOrNull { it.name == name || it.kana == name }?.let { year + it.y - 1 }

    /** month / day が 0 のときは「その年の最後の日」とみなす */
    fun fromAd(ad: Int, month: Int = 0, day: Int = 0): GengoYear? {
        val key = ad * 10000 + (if (month > 0) month else 13) * 100 + (if (day > 0) day else 32)
        val g = LIST.lastOrNull { key >= it.startKey } ?: return null
        return GengoYear(g, ad - g.y + 1)
    }
}