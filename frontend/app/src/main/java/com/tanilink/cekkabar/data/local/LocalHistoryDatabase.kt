package com.tanilink.cekkabar.data.local

import android.content.ContentValues
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tanilink.cekkabar.data.model.DetectResponse
import com.tanilink.cekkabar.data.model.HistoryCounts
import com.tanilink.cekkabar.data.model.HistoryItem
import com.tanilink.cekkabar.data.model.HistoryListResponse
import com.tanilink.cekkabar.data.model.InfluentialWord
import com.tanilink.cekkabar.data.model.TokenItem
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalHistoryDatabase(
    context: Context,
    keystoreManager: KeystoreManager
) : SQLiteOpenHelper(
    context.applicationContext,
    DB_NAME,
    keystoreManager.getOrCreateDatabasePassphrase(),
    null,
    DB_VERSION,
    0,
    null,
    null,
    false
) {
    private val gson = Gson()

    init {
        System.loadLibrary("sqlcipher")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_HISTORIES (
                id INTEGER PRIMARY KEY,
                text TEXT NOT NULL,
                verdict TEXT NOT NULL,
                hoax_probability REAL NOT NULL,
                fakta_probability REAL NOT NULL,
                confidence_label TEXT NOT NULL,
                word_count INTEGER NOT NULL DEFAULT 0,
                top_keywords_json TEXT NOT NULL,
                tokens_json TEXT NOT NULL,
                influential_words_json TEXT NOT NULL,
                created_at TEXT NOT NULL,
                formatted_time TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORIES")
        onCreate(db)
    }

    fun saveDetection(response: DetectResponse) {
        val db = writableDatabase
        val rowId = response.id ?: System.currentTimeMillis()
        val now = Date()
        val createdAt = response.createdAt
            ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(now)
        val formattedTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(now) + " WIB"

        val values = ContentValues().apply {
            put("id", rowId)
            put("text", response.text)
            put("verdict", response.verdict)
            put("hoax_probability", response.hoaxProbability)
            put("fakta_probability", response.faktaProbability)
            put("confidence_label", response.confidenceLabel)
            put("word_count", response.wordCount)
            put("top_keywords_json", gson.toJson(response.topKeywords))
            put("tokens_json", gson.toJson(response.tokens))
            put("influential_words_json", gson.toJson(response.topInfluentialWords))
            put("created_at", createdAt)
            put("formatted_time", formattedTime)
        }
        db.insertWithOnConflict(TABLE_HISTORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getHistories(filter: String = "all", query: String? = null): HistoryListResponse {
        val db = readableDatabase

        val allCount = countByVerdict(db, null)
        val hoaksCount = countByVerdict(db, "HOAKS")
        val faktaCount = countByVerdict(db, "FAKTA")

        val whereParts = mutableListOf<String>()
        val args = mutableListOf<String>()

        when (filter.lowercase()) {
            "hoaks" -> {
                whereParts.add("UPPER(verdict) = ?")
                args.add("HOAKS")
            }
            "fakta" -> {
                whereParts.add("UPPER(verdict) = ?")
                args.add("FAKTA")
            }
        }

        if (!query.isNullOrBlank()) {
            whereParts.add("text LIKE ?")
            args.add("%$query%")
        }

        val selection = if (whereParts.isEmpty()) null else whereParts.joinToString(" AND ")
        val selectionArgs = if (args.isEmpty()) null else args.toTypedArray()

        val listType = object : TypeToken<List<String>>() {}.type
        val items = mutableListOf<HistoryItem>()

        db.query(
            TABLE_HISTORIES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "id DESC",
            "50"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val keywords: List<String> = gson.fromJson(
                    cursor.getString(cursor.getColumnIndexOrThrow("top_keywords_json")),
                    listType
                ) ?: emptyList()

                items.add(
                    HistoryItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                        verdict = cursor.getString(cursor.getColumnIndexOrThrow("verdict")),
                        hoaxProbability = cursor.getDouble(cursor.getColumnIndexOrThrow("hoax_probability")),
                        faktaProbability = cursor.getDouble(cursor.getColumnIndexOrThrow("fakta_probability")),
                        confidenceLabel = cursor.getString(cursor.getColumnIndexOrThrow("confidence_label")),
                        topKeywords = keywords,
                        wordCount = cursor.getInt(cursor.getColumnIndexOrThrow("word_count")),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        formattedTime = cursor.getString(cursor.getColumnIndexOrThrow("formatted_time"))
                    )
                )
            }
        }

        return HistoryListResponse(
            counts = HistoryCounts(all = allCount, hoaks = hoaksCount, fakta = faktaCount),
            items = items
        )
    }

    fun getHistoryDetail(id: Long): DetectResponse? {
        val db = readableDatabase
        val keywordsType = object : TypeToken<List<String>>() {}.type
        val tokensType = object : TypeToken<List<TokenItem>>() {}.type
        val wordsType = object : TypeToken<List<InfluentialWord>>() {}.type

        db.query(
            TABLE_HISTORIES,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val keywords: List<String> = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow("top_keywords_json")),
                keywordsType
            ) ?: emptyList()
            val tokens: List<TokenItem> = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow("tokens_json")),
                tokensType
            ) ?: emptyList()
            val influential: List<InfluentialWord> = gson.fromJson(
                cursor.getString(cursor.getColumnIndexOrThrow("influential_words_json")),
                wordsType
            ) ?: emptyList()

            return DetectResponse(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                verdict = cursor.getString(cursor.getColumnIndexOrThrow("verdict")),
                hoaxProbability = cursor.getDouble(cursor.getColumnIndexOrThrow("hoax_probability")),
                faktaProbability = cursor.getDouble(cursor.getColumnIndexOrThrow("fakta_probability")),
                confidenceLabel = cursor.getString(cursor.getColumnIndexOrThrow("confidence_label")),
                wordCount = cursor.getInt(cursor.getColumnIndexOrThrow("word_count")),
                topKeywords = keywords,
                tokens = tokens,
                topInfluentialWords = influential,
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            )
        }
    }

    fun deleteHistory(id: Long) {
        writableDatabase.delete(TABLE_HISTORIES, "id = ?", arrayOf(id.toString()))
    }

    fun clearHistories() {
        writableDatabase.delete(TABLE_HISTORIES, null, null)
    }

    private fun countByVerdict(db: SQLiteDatabase, verdict: String?): Int {
        val sql = if (verdict == null) {
            "SELECT COUNT(*) FROM $TABLE_HISTORIES"
        } else {
            "SELECT COUNT(*) FROM $TABLE_HISTORIES WHERE UPPER(verdict) = ?"
        }
        val args = if (verdict == null) emptyArray() else arrayOf(verdict)
        db.rawQuery(sql, args).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    companion object {
        private const val DB_NAME = "cekkabar_encrypted.db"
        private const val DB_VERSION = 1
        private const val TABLE_HISTORIES = "local_histories"
    }
}

