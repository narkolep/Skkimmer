package com.narkolep.skkimmer.data

object LispDictionary {
    val entries: Map<String, List<String>> = mapOf(
        "#にちご" to listOf("#a"),
        "#にちまえ" to listOf("#A"),
        "#かげつご" to listOf("#b", "#c"),
        "#かげつまえ" to listOf("#B", "#C"),
        "#ねんご" to listOf("#d", "#e"),
        "#ねんまえ" to listOf("#D", "#E"),

        "きょう" to listOf("#a0"),
        "あした" to listOf("#a1"),
        "あす" to listOf("#a1"),
        "あさって" to listOf("#a2"),
        "きのう" to listOf("#A1"),
        "おととい" to listOf("#A2"),

        "らいげつ" to listOf("#b1", "#c1"),
        "さらいげつ" to listOf("#b2", "#c2"),
        "せんげつ" to listOf("#D1", "#C1"),
        "せんせんげつ" to listOf("#D2", "#C2"),

        "ことし" to listOf("#d0", "#e0"),
        "らいねん" to listOf("#d1", "#e1"),
        "さらいねん" to listOf("#d2", "#e2"),
        "きょねん" to listOf("#D1", "#E1"),
        "さくねん" to listOf("#D1", "#E1"),

        "めいじ#ねん" to listOf("#f1867"),
        "たいしょう#ねん" to listOf("#f1911"),
        "しょうわ#ねん" to listOf("#f1925"),
        "へいせい#ねん" to listOf("#f1988"),
        "れいわ#ねん" to listOf("#f2018"),
    )
}