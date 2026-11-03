package com.narkolep.skkimmer.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.narkolep.skkimmer.data.db.AppDatabase
import com.narkolep.skkimmer.data.db.entity.EmojiEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun loadEmojis(jsonString: String): List<Category> = withContext(Dispatchers.IO) {
        db.withTransaction {
            var allEmojis = dao.getAllEmojis()
            if (allEmojis.isEmpty()) {
                val entities = parseJsonToEntities(jsonString)
                if (entities.isNotEmpty()) {
                    dao.insertAll(entities)
                    allEmojis = entities
                }
            }
            buildCategoryList(allEmojis)
        }
    }

    companion object {
        private val gson = Gson()
    }

    // JSONをRoom用のEntityに変換する処理
    private fun parseJsonToEntities(jsonString: String): List<EmojiEntity> {
        return runCatching {
            val rawData: List<List<String>>? = gson.fromJson(
                jsonString,
                object : TypeToken<List<List<String>>>() {}.type,
            )
            if (rawData.isNullOrEmpty()) return emptyList()

            val entities = mutableListOf<EmojiEntity>()
            var currentCategory = "General"
            var currentSubcategory = "General"
            var catOrder = 0
            var subCatOrder = 0
            var emojiOrder = 0

            for (i in rawData.indices) {
                val row = rawData[i]
                if (row.size == 1) {
                    val title = row.getOrNull(0) ?: continue
                    val isNextHeader = (i + 1 < rawData.size) && (rawData[i + 1].size == 1)

                    if (isNextHeader) {
                        currentCategory = title
                        catOrder++
                    } else {
                        currentSubcategory = title
                        subCatOrder++
                    }
                } else if (row.size >= 4) {
                    val idStr = row.getOrNull(0) ?: continue
                    val unicode = row.getOrNull(1) ?: continue
                    val emoji = row.getOrNull(2) ?: continue
                    val description = row.getOrNull(3) ?: ""

                    entities.add(
                        EmojiEntity(
                            idStr = idStr,
                            unicode = unicode,
                            emoji = emoji,
                            description = description,
                            categoryName = currentCategory,
                            subcategoryName = currentSubcategory,
                            categoryOrder = catOrder,
                            subcategoryOrder = subCatOrder,
                            emojiOrder = emojiOrder
                        )
                    )
                    emojiOrder++
                }
            }
            entities
        }.onFailure { exception ->
            Log.e("EmojiManager", "絵文字JSONのパース処理中にエラーが発生しました", exception)
        }.getOrDefault(emptyList())
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