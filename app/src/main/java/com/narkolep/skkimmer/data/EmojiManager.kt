package com.narkolep.skkimmer.data

import android.content.Context
import androidx.compose.runtime.Immutable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EmojiManager(context: Context) {

    // 再描画しないように@Immutableを追加
    @Immutable
    data class Emoji(val id: String, val unicode: String, val emoji: String, val description: String)
    @Immutable
    data class Subcategory(val name: String, val emojis: List<Emoji>)
    @Immutable
    data class Category(val name: String, val subcategories: List<Subcategory>)

    // データベースの準備
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.emojiDao()

    suspend fun loadEmojis(jsonString: String): List<Category> {
        var allEmojis = dao.getAllEmojis()
        if (allEmojis.isEmpty()) {
            val entities = parseJsonToEntities(jsonString)
            dao.insertAll(entities)
            allEmojis = entities
        }
        return buildCategoryList(allEmojis)
    }

    // JSONをRoom用のEntityに変換する処理
    private fun parseJsonToEntities(jsonString: String): List<EmojiEntity> {
        val rawData: List<List<String>> = Gson().fromJson(jsonString, object : TypeToken<List<List<String>>>() {}.type)
        val entities = mutableListOf<EmojiEntity>()

        var currentCategory = "General"
        var currentSubcategory = "General"
        var catOrder = 0
        var subCatOrder = 0
        var emojiOrder = 0

        for (i in rawData.indices) {
            val row = rawData[i]
            if (row.size == 1) {
                val title = row[0]
                val isNextHeader = (i + 1 < rawData.size && rawData[i + 1].size == 1)

                if (isNextHeader) {
                    currentCategory = title
                    catOrder++
                } else {
                    currentSubcategory = title
                    subCatOrder++
                }
            } else if (row.size >= 4) {
                entities.add(
                    EmojiEntity(
                        idStr = row[0],
                        unicode = row[1],
                        emoji = row[2],
                        description = row[3],
                        categoryName = currentCategory,
                        subcategoryName = currentSubcategory,
                        categoryOrder = catOrder,
                        subcategoryOrder = subCatOrder,
                        emojiOrder = emojiOrder++
                    )
                )
            }
        }
        return entities
    }

    // EntityをUI用の階層構造に変換する処理
    private fun buildCategoryList(entities: List<EmojiEntity>): List<Category> {
        return entities
            .groupBy { it.categoryName }
            .map { (catName, catEmojis) ->
                Category(
                    name = catName,
                    subcategories = catEmojis
                        .groupBy { it.subcategoryName }
                        .map { (subName, subEmojis) ->
                            Subcategory(
                                name = subName,
                                emojis = subEmojis.map { Emoji(it.idStr, it.unicode, it.emoji, it.description) }
                            )
                        }
                )
            }
    }
}