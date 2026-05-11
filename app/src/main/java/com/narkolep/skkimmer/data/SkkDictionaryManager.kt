package com.narkolep.skkimmer.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

class SkkDictionaryManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val systemDictDao = db.systemDictDao()
    private val historyDao = db.historyDao()
    private val dictionaryInfoDao = db.dictionaryInfoDao()

    /**
     * バイト配列がUTF-8かEUC-JPか判定する。どちらでもない場合はnullを返す。
     */
    private fun detectCharset(bytes: ByteArray): Charset? {
        // バイトのルールが厳密なUTF-8を先に試し、次にEUC-JPを試す
        val charsets = listOf(Charsets.UTF_8, Charset.forName("EUC-JP"))

        for (charset in charsets) {
            try {
                val decoder = charset.newDecoder()
                    // 不正な文字が含まれている場合はエラーを投げる（REPORT）ように設定
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)

                decoder.decode(ByteBuffer.wrap(bytes))
                return charset // エラーなくデコードできたら、その文字コードと判定
            } catch (_: CharacterCodingException) {
                // エラーが発生した場合は、次の文字コードを試す
                continue
            }
        }
        return null // どちらでもない場合
    }

    /**
     * ユーザーが選択した辞書ファイル(Uri)から読み込みを行う
     */
    suspend fun importDictionaryFromUri(uri: Uri, fileName: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                // mark/resetが使えるようにBufferedInputStreamでラップ
                val bufferedStream = inputStream.buffered()

                // 文字コード判定
                bufferedStream.mark(8192) // 先頭8KBまで巻き戻せるようにマーク
                val bytes = ByteArray(8192)
                val readCount = bufferedStream.read(bytes)
                bufferedStream.reset() // 読み込み位置を先頭に戻す

                if (readCount <= 0) throw IllegalArgumentException("ファイルが空です。")

                // 実際に読み込めたサイズだけ切り出して判定にかける
                val actualBytes = bytes.copyOf(readCount)
                val charset = detectCharset(actualBytes) ?:
                throw IllegalArgumentException("EUC-JPまたはUTF-8形式のファイルを選択してください。")

                Log.d("SKK_DEBUG", "判定された文字コード: ${charset.name()}")

                // 辞書データの読み込み(判定された文字コードを使用)
                val entries = mutableListOf<SystemDictEntry>()
                var totalCount = 0

                bufferedStream.bufferedReader(charset).useLines { lines ->
                    lines.forEach { line ->
                        if (line.startsWith(";;")) return@forEach
                        val parts = line.split(" /")
                        if (parts.size >= 2) {
                            entries.add(SystemDictEntry(midashi = parts[0], candidates = parts[1]))
                            totalCount++
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

                dictionaryInfoDao.insert(DictionaryInfo(name = fileName, entryCount = totalCount))

                Log.d("SKK_DEBUG", "外部辞書のインポート完了")
            } ?: throw IllegalStateException("ファイルを開けませんでした。")
        }
    }

    /**
     * 読み込まれているすべての辞書データとファイル情報を全て削除する
     */
    suspend fun clearAllDictionaries() = withContext(Dispatchers.IO) {
        systemDictDao.deleteAll()
        dictionaryInfoDao.deleteAll()
    }

    /**
     * 候補の取得（履歴を最優先にする）
     */
    suspend fun getCandidates(key: String): List<String> = withContext(Dispatchers.IO) {
        // 履歴から取得
        val history = historyDao.getHistoryByMidashi(key).map { it.candidate }

        // システム辞書から取得
        val systemEntry = systemDictDao.getEntry(key)
        val systemCandidates = systemEntry?.candidates
            ?.split("/")
            ?.filter { it.isNotEmpty() } ?: emptyList()

        // マージして返す
        return@withContext (history + systemCandidates).distinct()
    }

    /**
     * 確定時の学習（履歴保存）ロジック
     */
    suspend fun learnWord(midashi: String, candidate: String, isFromSystem: Boolean) {
        withContext(Dispatchers.IO) {
            // 既存のエントリーを取得する
            val existingEntries = historyDao.findEntry(midashi, candidate)
            val entry = existingEntries.firstOrNull()
            val oldIsFromSystem = entry?.isFromSystemDict

            // 同一の「見出し+漢字」があれば古い方は削除する
            existingEntries.forEach { historyDao.deleteEntry(it) }

            // 同じ見出しのシステム候補について、古いものを削除する
            val sameMidashiSystemEntries = historyDao.getOldSystemEntriesByMidashi(midashi)
            if (sameMidashiSystemEntries.size >= 3) {
                historyDao.deleteEntry(sameMidashiSystemEntries[0])
            }

            // 履歴全体が1000個を超えたら、最も古いシステム候補を削除
            val totalCount = historyDao.getTotalCount()
            if (totalCount >= 1000) {
                val oldestSystem = historyDao.getOldestSystemEntry()
                if (oldestSystem != null) {
                    historyDao.deleteEntry(oldestSystem)
                }
            }

            // 新しい履歴を挿入する
            val fromSystemFlag = (oldIsFromSystem != false && isFromSystem)
            val newEntry = HistoryEntry(
                midashi = midashi,
                candidate = candidate,
                isFromSystemDict = fromSystemFlag
            )
            historyDao.insert(newEntry)
        }
    }
}