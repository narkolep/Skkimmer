package com.narkolep.skkimmer.keyboard.mappings

object DakutenMap {
    data class FlickConvertConfig(
        val consonantBefore: String,
        val consonantAfter: String
    )

    val flickConvert = listOf(
        FlickConvertConfig("", "x"), // あ
        FlickConvertConfig("x", ""),
        FlickConvertConfig("v", ""),
        FlickConvertConfig("k", "g"), // か
        FlickConvertConfig("g", "k"),
        FlickConvertConfig("s", "z"), // さ
        FlickConvertConfig("z", "s"),
        FlickConvertConfig("t", "d"), // た
        FlickConvertConfig("xt", "d"),
        FlickConvertConfig("d", "t"),
        FlickConvertConfig("h", "b"), // は
        FlickConvertConfig("b", "p"),
        FlickConvertConfig("p", "h"),
        FlickConvertConfig("y", "xy"), // や
        FlickConvertConfig("xy", "y"),
        FlickConvertConfig(".", ","), // 記号
        FlickConvertConfig(",", "."),
        FlickConvertConfig("-", "z-"),
        FlickConvertConfig("z-", "-"),
        FlickConvertConfig("(", "「"),
        FlickConvertConfig("「", "("),
        FlickConvertConfig(")", "」"),
        FlickConvertConfig("」", ")"),
    )
}