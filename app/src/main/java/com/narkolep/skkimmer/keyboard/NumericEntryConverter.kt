package com.narkolep.skkimmer.keyboard

/**
 * 数値エントリの変換処理
 */
object NumericEntryConverter {

    /**
     * 実装済みの #n のリスト
     */
    val conversionMap: Map<String, (String) -> String> = mapOf(
        "#0" to { it },
        "#1" to { number -> toFullWidth(number) },
        "#2" to { number -> toKanjiDigits(number) },
        "#3" to { number -> toKanjiNumber(number) },
        "#5" to { number -> toDaiji(number) },
        "#8" to { number -> toCommaSeparated(number) }
    )

    /**
     * 数値エントリの処理
     * @param entry #0~8を含む変換候補
     * @param originalText 半角数字を含む見出し文字列
     * @return 半角数字部分を置き換えた文字列
     */
    fun replace(entry: String, originalText: String): String {
        return buildString {
            var numberCount = 0
            var i = 0

            while (i < entry.length) {
                val key = entry.substring(i, minOf(i + 2, entry.length))
                val converter = conversionMap[key]

                if (converter != null) {
                    numberCount++
                    val number = getNumber(originalText, numberCount)
                    append(converter(number))
                    i += 2
                } else {
                    append(entry[i])
                    i++
                }
            }
        }
    }

    private fun getNumber(text: String, flag: Int): String {
        return buildString {
            var inNumber = false
            var numberCount = 0

            for (char in text) {
                if (char in '0'..'9') {
                    if (!inNumber) {
                        inNumber = true
                        numberCount++
                    }

                    if (numberCount == flag) {
                        append(char)
                    }
                } else {
                    inNumber = false
                }
            }
        }
    }

    private fun toFullWidth(number: String): String {
        if (number.isEmpty()) return ""
        return buildString(number.length) {
            for (char in number) {
                if (char in '0'..'9') {
                    append((char.code + 0xFEE0).toChar())
                } else {
                    append(char)
                }
            }
        }
    }

    private const val KANJI_DIGITS = "〇一二三四五六七八九"

    private fun toKanjiDigits(number: String): String {
        if (number.isEmpty()) return ""
        return buildString(number.length) {
            for (char in number) {
                if (char in '0'..'9') {
                    append(KANJI_DIGITS[char - '0'])
                } else {
                    append(char)
                }
            }
        }
    }

    private fun toCommaSeparated(number: String): String {
        if (number.isEmpty()) return ""
        return number.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
    }

    fun toKanjiNumber(number: String): String {
        if (number.isEmpty()) return ""
        return toPositionalKanji(
            number = number,
            digits = KANJI_DIGITS,
            units = listOf("", "十", "百", "千"),
            largeUnits = listOf(
                "", "万", "億", "兆", "京", "垓", "秭", "穣", "溝", "澗", "正", "載", "極",
                "恒河沙", "阿僧祇", "那由他", "不可思議", "無量大数"
            )
        )
    }

    private fun toDaiji(number: String): String {
        if (number.isEmpty()) return ""
        return toPositionalKanji(
            number = number,
            digits = "零壱弐参肆伍陸漆捌玖",
            units = listOf("", "拾", "佰", "阡"),
            largeUnits = listOf(
                "", "萬", "億", "兆", "京", "垓", "秭", "穣", "溝", "澗", "正", "載", "極",
                "恒河沙", "阿僧祇", "那由他", "不可思議", "無量大数"
            )
        )
    }

    private fun toPositionalKanji(
        number: String,
        digits: String,
        units: List<String>,
        largeUnits: List<String>
    ): String {
        /* 早期リターン */
        if (number.isEmpty()) return ""

        require(digits.length == 10)
        require(units.size == 4)
        require(units[0].isEmpty())

        /* 先頭のゼロを除去 */
        val normalized = number.trimStart('0').ifEmpty { "0" }
        if (normalized == "0") return digits[0].toString()

        /* 4桁ずつ右から分割 */
        val groups = normalized
            .reversed()
            .chunked(4)
            .map { it.reversed() }

        /* 対応できる位取り単位を超えた場合 */
        if (groups.size > largeUnits.size) {
            return ""
        }

        return groups
            .mapIndexed { index, group ->
                val converted = convertFourDigits(
                    number = group,
                    digits = digits,
                    units = units
                )

                if (converted.isEmpty()) {
                    ""
                } else {
                    converted + largeUnits[index]
                }
            }
            .asReversed()
            .joinToString("")
    }

    private fun convertFourDigits(
        number: String,
        digits: String,
        units: List<String>
    ): String {
        val padded = number.padStart(4, '0')

        return buildString {
            padded.forEachIndexed { index, char ->
                val digit = char - '0'
                if (digit == 0) return@forEachIndexed
                val unitIndex = 3 - index

                /* 十・百・千の位で1を省略 */
                if (digit != 1 || unitIndex == 0) {
                    append(digits[digit])
                }
                append(units[unitIndex])
            }
        }
    }
}