package com.narkolep.skkimmer.keyboard.mappings

import com.narkolep.skkimmer.keyboard.InputMode

object KanaMap {
    data class KanaDefinition(
        val hira: String,
        val kata: String,
        val halfkata: String
    )

    val romajiToKana = mapOf(
        // あ
        "a" to KanaDefinition("あ", "ア", "ｱ"),
        "i" to KanaDefinition("い", "イ", "ｲ"),
        "u" to KanaDefinition("う", "ウ", "ｳ"),
        "e" to KanaDefinition("え", "エ", "ｴ"),
        "o" to KanaDefinition("お", "オ", "ｵ"),
        // か
        "ka" to KanaDefinition("か", "カ", "ｶ"),
        "ki" to KanaDefinition("き", "キ", "ｷ"),
        "ku" to KanaDefinition("く", "ク", "ｸ"),
        "ke" to KanaDefinition("け", "ケ", "ｹ"),
        "ko" to KanaDefinition("こ", "コ", "ｺ"),
        // さ
        "sa" to KanaDefinition("さ", "サ", "ｻ"),
        "si" to KanaDefinition("し", "シ", "ｼ"),
        "su" to KanaDefinition("す", "ス", "ｽ"),
        "se" to KanaDefinition("せ", "セ", "ｾ"),
        "so" to KanaDefinition("そ", "ソ", "ｿ"),
        // た
        "ta" to KanaDefinition("た", "タ", "ﾀ"),
        "ti" to KanaDefinition("ち", "チ", "ﾁ"),
        "tu" to KanaDefinition("つ", "ツ", "ﾂ"),
        "te" to KanaDefinition("て", "テ", "ﾃ"),
        "to" to KanaDefinition("と", "ト", "ﾄ"),
        // な
        "na" to KanaDefinition("な", "ナ", "ﾅ"),
        "ni" to KanaDefinition("に", "ニ", "ﾆ"),
        "nu" to KanaDefinition("ぬ", "ヌ", "ﾇ"),
        "ne" to KanaDefinition("ね", "ネ", "ﾈ"),
        "no" to KanaDefinition("の", "ノ", "ﾉ"),
        // は
        "ha" to KanaDefinition("は", "ハ", "ﾊ"),
        "hi" to KanaDefinition("ひ", "ヒ", "ﾋ"),
        "hu" to KanaDefinition("ふ", "フ", "ﾌ"),
        "he" to KanaDefinition("へ", "ヘ", "ﾍ"),
        "ho" to KanaDefinition("ほ", "ホ", "ﾎ"),
        // ま
        "ma" to KanaDefinition("ま", "マ", "ﾏ"),
        "mi" to KanaDefinition("み", "ミ", "ﾐ"),
        "mu" to KanaDefinition("む", "ム", "ﾑ"),
        "me" to KanaDefinition("め", "メ", "ﾒ"),
        "mo" to KanaDefinition("も", "モ", "ﾓ"),
        // や
        "ya" to KanaDefinition("や", "ヤ", "ﾔ"),
        "yu" to KanaDefinition("ゆ", "ユ", "ﾕ"),
        "ye" to KanaDefinition("いぇ", "イェ", "ｲｪ"),
        "yo" to KanaDefinition("よ", "ヨ", "ﾖ"),
        // ら
        "ra" to KanaDefinition("ら", "ラ", "ﾗ"),
        "ri" to KanaDefinition("り", "リ", "ﾘ"),
        "ru" to KanaDefinition("る", "ル", "ﾙ"),
        "re" to KanaDefinition("れ", "レ", "ﾚ"),
        "ro" to KanaDefinition("ろ", "ロ", "ﾛ"),
        // わ
        "wa" to KanaDefinition("わ", "ワ", "ﾜ"),
        "wi" to KanaDefinition("うぃ", "ウィ", "ｳｨ"),
        "we" to KanaDefinition("うぇ", "ウェ", "ｳｪ"),
        "wo" to KanaDefinition("を", "ヲ", "ｦ"),
        // ん
        "nn" to KanaDefinition("ん", "ン", "ﾝ"),
        // が
        "ga" to KanaDefinition("が", "ガ", "ｶﾞ"),
        "gi" to KanaDefinition("ぎ", "ギ", "ｷﾞ"),
        "gu" to KanaDefinition("ぐ", "グ", "ｸﾞ"),
        "ge" to KanaDefinition("げ", "ゲ", "ｹﾞ"),
        "go" to KanaDefinition("ご", "ゴ", "ｺﾞ"),
        // ざ
        "za" to KanaDefinition("ざ", "ザ", "ｻﾞ"),
        "zi" to KanaDefinition("じ", "ジ", "ｼﾞ"),
        "zu" to KanaDefinition("ず", "ズ", "ｽﾞ"),
        "ze" to KanaDefinition("ぜ", "ゼ", "ｾﾞ"),
        "zo" to KanaDefinition("ぞ", "ゾ", "ｿﾞ"),
        // だ
        "da" to KanaDefinition("だ", "ダ", "ﾀﾞ"),
        "di" to KanaDefinition("ぢ", "ヂ", "ﾁﾞ"),
        "du" to KanaDefinition("づ", "ヅ", "ﾂﾞ"),
        "de" to KanaDefinition("で", "デ", "ﾃﾞ"),
        "do" to KanaDefinition("ど", "ド", "ﾄﾞ"),
        // ば
        "ba" to KanaDefinition("ば", "バ", "ﾊﾞ"),
        "bi" to KanaDefinition("び", "ビ", "ﾋﾞ"),
        "bu" to KanaDefinition("ぶ", "ブ", "ﾌﾞ"),
        "be" to KanaDefinition("べ", "ベ", "ﾍﾞ"),
        "bo" to KanaDefinition("ぼ", "ボ", "ﾎﾞ"),
        // ぱ
        "pa" to KanaDefinition("ぱ", "パ", "ﾊﾟ"),
        "pi" to KanaDefinition("ぴ", "ピ", "ﾋﾟ"),
        "pu" to KanaDefinition("ぷ", "プ", "ﾌﾟ"),
        "pe" to KanaDefinition("ぺ", "ペ", "ﾍﾟ"),
        "po" to KanaDefinition("ぽ", "ポ", "ﾎﾟ"),
        // きゃ
        "kya" to KanaDefinition("きゃ", "キャ", "ｷｬ"),
        "kyi" to KanaDefinition("きぃ", "キィ", "ｷｨ"),
        "kyu" to KanaDefinition("きゅ", "キュ", "ｷｭ"),
        "kye" to KanaDefinition("きぇ", "キェ", "ｷｪ"),
        "kyo" to KanaDefinition("きょ", "キョ", "ｷｮ"),
        // しゃ
        "sha" to KanaDefinition("しゃ", "シャ", "ｼｬ"),
        "sya" to KanaDefinition("しゃ", "シャ", "ｼｬ"),
        "shi" to KanaDefinition("し", "シ", "ｼ"),
        "syi" to KanaDefinition("しぃ", "シィ", "ｼｨ"),
        "shu" to KanaDefinition("しゅ", "シュ", "ｼｭ"),
        "syu" to KanaDefinition("しゅ", "シュ", "ｼｭ"),
        "she" to KanaDefinition("しぇ", "シェ", "ｼｪ"),
        "sye" to KanaDefinition("しぇ", "シェ", "ｼｪ"),
        "sho" to KanaDefinition("しょ", "ショ", "ｼｮ"),
        "syo" to KanaDefinition("しょ", "ショ", "ｼｮ"),
        // ちゃ1
        "tya" to KanaDefinition("ちゃ", "チャ", "ﾁｬ"),
        "tyi" to KanaDefinition("ちぃ", "チィ", "ﾁｨ"),
        "tyu" to KanaDefinition("ちゅ", "チュ", "ﾁｭ"),
        "tye" to KanaDefinition("ちぇ", "チェ", "ﾁｪ"),
        "tyo" to KanaDefinition("ちょ", "チョ", "ﾁｮ"),
        // ちゃ2
        "cha" to KanaDefinition("ちゃ", "チャ", "ﾁｬ"),
        "chi" to KanaDefinition("ち", "チ", "ﾁ"),
        "chu" to KanaDefinition("ちゅ", "チュ", "ﾁｭ"),
        "che" to KanaDefinition("ちぇ", "チェ", "ﾁｪ"),
        "cho" to KanaDefinition("ちょ", "チョ", "ﾁｮ"),
        // てゃ
        "tha" to KanaDefinition("てゃ", "テャ", "ﾃｬ"),
        "thi" to KanaDefinition("てぃ", "ティ", "ﾃｨ"),
        "thu" to KanaDefinition("てゅ", "テュ", "ﾃｭ"),
        "the" to KanaDefinition("てぇ", "テェ", "ﾃｪ"),
        "tho" to KanaDefinition("てょ", "テョ", "ﾃｮ"),
        // にゃ
        "nya" to KanaDefinition("にゃ", "ニャ", "ﾆｬ"),
        "nyi" to KanaDefinition("にぃ", "ニィ", "ﾆｨ"),
        "nyu" to KanaDefinition("にゅ", "ニュ", "ﾆｭ"),
        "nye" to KanaDefinition("にぇ", "ニェ", "ﾆｪ"),
        "nyo" to KanaDefinition("にょ", "ニョ", "ﾆｮ"),
        // ひゃ
        "hya" to KanaDefinition("ひゃ", "ヒャ", "ﾋｬ"),
        "hyi" to KanaDefinition("ひぃ", "ヒィ", "ﾋｨ"),
        "hyu" to KanaDefinition("ひゅ", "ヒュ", "ﾋｭ"),
        "hye" to KanaDefinition("ひぇ", "ヒェ", "ﾋｪ"),
        "hyo" to KanaDefinition("ひょ", "ヒョ", "ﾋｮ"),
        // みゃ
        "mya" to KanaDefinition("みゃ", "ミャ", "ﾐｬ"),
        "myi" to KanaDefinition("みぃ", "ミィ", "ﾐｨ"),
        "myu" to KanaDefinition("みゅ", "ミュ", "ﾐｭ"),
        "mye" to KanaDefinition("みぇ", "ミェ", "ﾐｪ"),
        "myo" to KanaDefinition("みょ", "ミョ", "ﾐｮ"),
        // りゃ
        "rya" to KanaDefinition("りゃ", "リャ", "ﾘｬ"),
        "ryi" to KanaDefinition("りぃ", "リィ", "ﾘｨ"),
        "ryu" to KanaDefinition("りゅ", "リュ", "ﾘｭ"),
        "rye" to KanaDefinition("りぇ", "リェ", "ﾘｪ"),
        "ryo" to KanaDefinition("りょ", "リョ", "ﾘｮ"),
        // ぎゃ
        "gya" to KanaDefinition("ぎゃ", "ギャ", "ｷﾞｬ"),
        "gyi" to KanaDefinition("ぎぃ", "ギィ", "ｷﾞｨ"),
        "gyu" to KanaDefinition("ぎゅ", "ギュ", "ｷﾞｭ"),
        "gye" to KanaDefinition("ぎぇ", "ギェ", "ｷﾞｪ"),
        "gyo" to KanaDefinition("ぎょ", "ギョ", "ｷﾞｮ"),
        // じゃ
        "ja" to KanaDefinition("じゃ", "ジャ", "ｼﾞｬ"),
        "ji" to KanaDefinition("じ", "ジ", "ｼﾞ"),
        "ju" to KanaDefinition("じゅ", "ジュ", "ｼﾞｭ"),
        "je" to KanaDefinition("じぇ", "ジェ", "ｼﾞｪ"),
        "jo" to KanaDefinition("じょ", "ジョ", "ｼﾞｮ"),
        // びゃ
        "bya" to KanaDefinition("びゃ", "ビャ", "ﾋﾞｬ"),
        "byi" to KanaDefinition("びぃ", "ビィ", "ﾋﾞｨ"),
        "byu" to KanaDefinition("びゅ", "ビュ", "ﾋﾞｭ"),
        "bye" to KanaDefinition("びぇ", "ビェ", "ﾋﾞｪ"),
        "byo" to KanaDefinition("びょ", "ビョ", "ﾋﾞｮ"),
        // ぴゃ
        "pya" to KanaDefinition("ぴゃ", "ピャ", "ﾋﾟｬ"),
        "pyi" to KanaDefinition("ぴぃ", "ピィ", "ﾋﾟｨ"),
        "pyu" to KanaDefinition("ぴゅ", "ピュ", "ﾋﾟｭ"),
        "pye" to KanaDefinition("ぴぇ", "ピェ", "ﾋﾟｪ"),
        "pyo" to KanaDefinition("ぴょ", "ピョ", "ﾋﾟｮ"),
        // ふぁ
        "fa" to KanaDefinition("ふぁ", "ファ", "ﾌｧ"),
        "fi" to KanaDefinition("ふぃ", "フィ", "ﾌｨ"),
        "fu" to KanaDefinition("ふ", "フ", "ﾌ"),
        "fe" to KanaDefinition("ふぇ", "フェ", "ﾌｪ"),
        "fo" to KanaDefinition("ふぉ", "フォ", "ﾌｫ"),
        // ふゃ
        "fya" to KanaDefinition("ふゃ", "フャ", "ﾌｬ"),
        "fyi" to KanaDefinition("ふぃ", "フィ", "ﾌｨ"),
        "fyu" to KanaDefinition("ふゅ", "フュ", "ﾌｭ"),
        "fye" to KanaDefinition("ふぇ", "フェ", "ﾌｪ"),
        "fyo" to KanaDefinition("ふょ", "フョ", "ﾌｮ"),
        // う゛ぁ
        "va" to KanaDefinition("う゛ぁ", "ヴァ", "ｳﾞｧ"),
        "vi" to KanaDefinition("う゛ぃ", "ヴィ", "ｳﾞｨ"),
        "vu" to KanaDefinition("う゛", "ヴ", "ｳﾞ"),
        "ve" to KanaDefinition("う゛ぇ", "ヴェ", "ｳﾞｪ"),
        "vo" to KanaDefinition("う゛ぉ", "ヴォ", "ｳﾞｫ"),
        // つぁ
        "tsa" to KanaDefinition("つぁ", "ツァ", "ﾂｧ"),
        "tsi" to KanaDefinition("つぃ", "ツィ", "ﾂｨ"),
        "tsu" to KanaDefinition("つ", "ツ", "ﾂ"),
        "tse" to KanaDefinition("つぇ", "ツェ", "ﾂｪ"),
        "tso" to KanaDefinition("つぉ", "ツォ", "ﾂｫ"),
        // でゃ
        "dha" to KanaDefinition("でゃ", "デャ", "ﾃﾞｬ"),
        "dhi" to KanaDefinition("でぃ", "ディ", "ﾃﾞｨ"),
        "dhu" to KanaDefinition("でゅ", "デュ", "ﾃﾞｭ"),
        "dhe" to KanaDefinition("でぇ", "デェ", "ﾃﾞｪ"),
        "dho" to KanaDefinition("でょ", "デョ", "ﾃﾞｮ"),
        // ぁ
        "xa" to KanaDefinition("ぁ", "ァ", "ｧ"),
        "xi" to KanaDefinition("ぃ", "ィ", "ｨ"),
        "xu" to KanaDefinition("ぅ", "ゥ", "ｩ"),
        "xe" to KanaDefinition("ぇ", "ェ", "ｪ"),
        "xo" to KanaDefinition("ぉ", "ォ", "ｫ"),
        // ゃ
        "xya" to KanaDefinition("ゃ", "ャ", "ｬ"),
        "xyu" to KanaDefinition("ゅ", "ュ", "ｭ"),
        "xyo" to KanaDefinition("ょ", "ョ", "ｮ"),
        // っ
        "xtu" to KanaDefinition("っ", "ッ", "ｯ"),
        // 矢印
        "zh" to KanaDefinition("←", "←", "←"),
        "zj" to KanaDefinition("↓", "↓", "↓"),
        "zk" to KanaDefinition("↑", "↑", "↑"),
        "zl" to KanaDefinition("→", "→", "→"),
        // 句読点など
        "." to KanaDefinition("。", "。", "｡"),
        "," to KanaDefinition("、", "、", "､"),
        "-" to KanaDefinition("ー", "ー", "ｰ"),
        "~" to KanaDefinition("～", "～", "~"),
        "「" to KanaDefinition("「", "「", "｢"),
        "」" to KanaDefinition("」", "」", "｣")
    )

    val hiraToKataMap by lazy {
        romajiToKana.values.associate { it.hira to it.kata }
    }
    val kataToHiraMap by lazy {
        romajiToKana.values.associate { it.kata to it.hira }
    }
    val hiraToHalfMap by lazy {
        romajiToKana.values.associate { it.hira to it.halfkata }
    }

    fun convertString(text: String, map: Map<String, String>): String {
        var result = text
        map.forEach { (key, value) ->
            result = result.replace(key, value)
        }
        return result
    }

    fun getOutputChar(definition: KanaDefinition, inputMode: InputMode): String {
        return when (inputMode) {
            InputMode.KATAKANA -> definition.kata
            InputMode.HALF_KATAKANA -> definition.halfkata
            else -> definition.hira
        }
    }
}