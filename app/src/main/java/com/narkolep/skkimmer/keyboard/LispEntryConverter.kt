package com.narkolep.skkimmer.keyboard

import java.time.LocalDate
import java.time.chrono.JapaneseDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object LispEntryConverter {

    /**
     * 対応している候補のリスト
     */
    val conversionMap: Map<String, (Long) -> String> = mapOf(
        "#a" to { days -> relativeDateProvider(
            minus = false,
            formatString = "M月d日",
            days = days
        ) },
        "#A" to { days -> relativeDateProvider(
            minus = true,
            formatString = "M月d日",
            days = days
        ) },
        "#b" to { months -> relativeDateProvider(
            minus = false,
            formatString = "yyyy年M月d日",
            months = months
        ) },
        "#B" to { months -> relativeDateProvider(
            minus = true,
            formatString = "yyyy年M月d日",
            months = months
        ) },
        "#c" to { months -> relativeDateProvider(
            minus = false,
            japanese = true,
            formatString = "GGGGy年M月d日",
            months = months
        ) },
        "#C" to { months -> relativeDateProvider(
            minus = true,
            japanese = true,
            formatString = "GGGGy年M月d日",
            months = months
        ) },
        "#d" to { years -> relativeDateProvider(
            minus = false,
            formatString = "yyyy年M月d日",
            years = years
        ) },
        "#D" to { years -> relativeDateProvider(
            minus = true,
            formatString = "yyyy年M月d日",
            years = years
        ) },
        "#e" to { years -> relativeDateProvider(
            minus = false,
            japanese = true,
            formatString = "GGGGy年M月d日",
            years = years
        ) },
        "#E" to { years -> relativeDateProvider(
            minus = true,
            japanese = true,
            formatString = "GGGGy年M月d日",
            years = years
        ) },
        "#f" to { years -> "西暦${years}年" },
    )

    /**
     * 日付を含む候補の置き換え
     */
    fun replace(entry: String, originalText: String): String {
        return buildString {
            var i = 0

            while (i < entry.length) {
                val key = entry.substring(i, minOf(i + 2, entry.length))
                val converter = conversionMap[key]

                if (converter != null) {
                    // key の直後に続く連続する数字（#f1867 の 1867）を抽出する
                    var numEnd = i + 2
                    while (numEnd < entry.length && entry[numEnd] in '0'..'9') {
                        numEnd++
                    }

                    val entryNumber = entry.substring(i + 2, numEnd).toLongOrNull() ?: 0L
                    val originalNumber = extractFirstNumber(originalText)

                    append(converter(originalNumber + entryNumber))

                    // 処理したキー + 数字の長さ分インデックスを進める
                    i = numEnd
                } else {
                    append(entry[i])
                    i++
                }
            }
        }
    }

    /**
     * 文字列から最初に見つかった連続する数字列を Long として抽出する
     */
    private fun extractFirstNumber(text: String): Long {
        val digits = buildString {
            var found = false
            for (char in text) {
                if (char in '0'..'9') {
                    append(char)
                    found = true
                } else if (found) {
                    break
                }
            }
        }
        return digits.toLongOrNull() ?: 0L
    }

    private fun relativeDateProvider(
        minus: Boolean,
        japanese: Boolean = false,
        formatString: String,
        days: Long = 0L,
        months: Long = 0L,
        years: Long = 0L
    ): String {
        val today = LocalDate.now()
        val relativeDate = if (minus) {
            today
                .minusYears(years)
                .minusMonths(months)
                .minusDays(days)
        } else {
            today
                .plusYears(years)
                .plusMonths(months)
                .plusDays(days)
        }

        return try {
            val formatter = DateTimeFormatter.ofPattern(formatString, Locale.JAPAN)
            if (japanese) {
                JapaneseDate.from(relativeDate).format(formatter)
            } else {
                relativeDate.format(formatter)
            }
        } catch (_: Exception) {
            /* 和暦変換に非対応な日付の場合は西暦表記にフォールバック */
            val fallbackFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPAN)
            relativeDate.format(fallbackFormatter)
        }
    }

}