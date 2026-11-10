package com.narkolep.skkimmer.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.narkolep.skkimmer.data.db.AppDatabase
import com.narkolep.skkimmer.data.db.entity.DictionaryInfoEntity
import com.narkolep.skkimmer.data.db.entity.HistoryEntity
import com.narkolep.skkimmer.data.db.entity.SystemDictEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

class DictionaryManager(
    private val context: Context
) {
    private val db = AppDatabase.getDatabase(context)
    private val systemDictDao = db.systemDictDao()
    private val historyDao = db.historyDao()
    private val dictionaryInfoDao = db.dictionaryInfoDao()

    companion object {
        private val EUC_JP: Charset = Charset.forName("EUC-JP")
        private val SUPPORTED_CHARSETS = listOf(Charsets.UTF_8, EUC_JP)
    }

    /**
     * バイト配列がUTF-8かEUC-JPか判定する
     * @return UTF-8 or EUC-JP or nullを返す
     */
    private fun detectCharset(bytes: ByteArray): Charset? {
        // 先頭のバッファ末尾でマルチバイト文字が途切れることによる検出失敗を防ぐため、
        // 4バイト以上ある場合は末尾4バイトを除いた安全な範囲で判定を試みる
        val testLength = if (bytes.size > 4) bytes.size - 4 else bytes.size

        for (charset in SUPPORTED_CHARSETS) {
            val matched = try {
                val decoder = charset
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)

                decoder.decode(ByteBuffer.wrap(bytes, 0, testLength))
                charset
            } catch (_: CharacterCodingException) {
                null
            }
            if (matched != null) return matched
        }
        return null
    }

    /**
     * ユーザーが選択した辞書ファイル(Uri)から読み込みを行う
     */
    suspend fun importDictionaryFromUri(uri: Uri, fileName: String): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("ファイルを開けませんでした。")

                inputStream.use { rawStream ->
                    /* mark/resetが使えるようにBufferedInputStreamでラップ */
                    val bufferedStream = rawStream.buffered()

                    bufferedStream.mark(8192) // 先頭8KBまで巻き戻せるようにマーク
                    val bytes = ByteArray(8192)
                    val readCount = bufferedStream.read(bytes)
                    bufferedStream.reset() // 読み込み位置を先頭に戻す

                    if (readCount <= 0) throw IllegalArgumentException("ファイルが空です。")

                    /* 実際に読み込めたサイズだけ切り出して文字コードを判定 */
                    val actualBytes = bytes.copyOf(readCount)
                    val charset = detectCharset(actualBytes)
                        ?: throw IllegalArgumentException("EUC-JPまたはUTF-8形式のファイルを選択してください。")

                    Log.d("SKK_DEBUG", "判定された文字コード: ${charset.name()}")

                    /* 辞書データの読み込み(判定された文字コードを使用) */
                    var totalCount = 0

                    db.withTransaction {
                        val entries = mutableListOf<SystemDictEntity>()

                        bufferedStream.bufferedReader(charset).useLines { lines ->
                            lines.forEach { line ->
                                val trimmed = line.trim()
                                if (trimmed.isEmpty() || trimmed.startsWith(";;")) return@forEach

                                // 最初の " /" の位置を探して見出し語と候補ブロックを確実に2分割する
                                val separatorIndex = trimmed.indexOf(" /")
                                if (separatorIndex > 0) {
                                    val midashi = trimmed.substring(0, separatorIndex)
                                    val candidates = trimmed.substring(separatorIndex + 2) // " /" の後ろすべて

                                    if (midashi.isNotEmpty() && candidates.isNotEmpty()) {
                                        entries.add(
                                            SystemDictEntity(
                                                midashi = midashi,
                                                candidates = candidates
                                            )
                                        )
                                        totalCount++
                                    }
                                }

                                if (entries.size >= 1000) {
                                    systemDictDao.insertAll(entries)
                                    entries.clear()
                                }
                            }
                        }
                        if (entries.isNotEmpty()) {
                            systemDictDao.insertAll(entries)
                        }

                        dictionaryInfoDao.insert(
                            DictionaryInfoEntity(
                                name = fileName,
                                entryCount = totalCount
                            )
                        )
                    }

                    Log.d("SKK_DEBUG", "外部辞書のインポート完了")
                    totalCount
                }
            }
        }

    /**
     * 読み込まれているすべての辞書データとファイル情報を全て削除する
     */
    suspend fun clearAllDictionaries() = withContext(Dispatchers.IO) {
        db.withTransaction {
            systemDictDao.deleteAll()
            dictionaryInfoDao.deleteAll()
        }
    }

    /**
     * 候補の取得（履歴を最優先にする）
     */
    suspend fun getCandidates(key: String): List<String> = withContext(Dispatchers.IO) {
        /* 半角数字を"#"に置き換える */
        val keyString = replaceNumber(key)

        /* 履歴から取得（"/" で分割して展開する） */
        val history = historyDao.getHistoryByMidashi(keyString)
            .flatMap { it.candidate.split("/") }
            .filter { it.isNotEmpty() }

        /* システム辞書から取得 */
        val systemEntry = systemDictDao.getEntry(keyString)
        val systemCandidates = systemEntry?.candidates
            ?.split("/")
            ?.filter { it.isNotEmpty() } ?: emptyList()

        /* 組み込み辞書から取得 */
        val lispCandidates = LispDictionary.entries[keyString] ?: emptyList()

        /* マージして返す */
        return@withContext (history + lispCandidates + systemCandidates).distinct()
    }

    /**
     * 確定時の学習（履歴保存）
     */
    suspend fun learnWord(midashi: String, candidate: String, isFromSystem: Boolean) {
        /* 半角数字を"#"に置き換える */
        val midashiString = replaceNumber(midashi)

        withContext(Dispatchers.IO) {
            db.withTransaction {
                /* 既存のエントリーを取得する */
                val existingEntries = historyDao.findEntry(midashiString, candidate)
                val entry = existingEntries.firstOrNull()
                val oldIsFromSystem = entry?.isFromSystemDict

                /* 同一の見出し、漢字があれば古い方は削除する */
                existingEntries.forEach { historyDao.deleteEntry(it) }

                /* 同じ見出しのシステム候補について、古いものを削除する */
                val sameMidashiSystemEntries = historyDao.getOldSystemEntriesByMidashi(midashiString)
                if (sameMidashiSystemEntries.size >= 3) {
                    historyDao.deleteEntry(sameMidashiSystemEntries[0])
                }

                /* 古いシステム候補を削除 */
                val totalCount = historyDao.getTotalCount()
                if (totalCount >= 1000) {
                    historyDao.getOldestSystemEntry()?.let { oldestSystem ->
                        historyDao.deleteEntry(oldestSystem)
                    }
                }

                /* 新しい履歴を挿入する */
                val fromSystemFlag = (oldIsFromSystem != false) && isFromSystem
                val newEntry = HistoryEntity(
                    midashi = midashiString,
                    candidate = candidate,
                    isFromSystemDict = fromSystemFlag
                )
                historyDao.insert(newEntry)
            }
        }
    }

    /**
     * 半角数字を"#"に置き換える
     */
    private fun replaceNumber(text: String): String {
        return buildString {
            var inNumber = false

            for (char in text) {
                if (char in '0'..'9') {
                    if (!inNumber) {
                        append('#')
                        inNumber = true
                    }
                } else {
                    append(char)
                    inNumber = false
                }
            }
        }
    }
}