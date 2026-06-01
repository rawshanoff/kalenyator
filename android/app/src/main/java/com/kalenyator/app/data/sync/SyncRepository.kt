package com.kalenyator.app.data.sync

import android.content.Context
import com.google.gson.Gson
import com.kalenyator.app.data.local.FamilyEventDao
import com.kalenyator.app.data.local.FamilyEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader

class SyncRepository(
    private val context: Context,
    private val dao: FamilyEventDao,
    private val gson: Gson = Gson()
) {
    suspend fun exportJsonString(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val events = dao.getAll().map { it.toDto() }
            gson.toJson(FamilySyncFile(events = events))
        }
    }

    suspend fun exportNewJsonString(sinceMillis: Long): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val events = dao.getAll()
                .filter { it.updatedAt > sinceMillis }
                .map { it.toDto() }
            gson.toJson(FamilySyncFile(events = events, exportedAt = System.currentTimeMillis()))
        }
    }

    suspend fun exportToCache(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val json = exportJsonString().getOrThrow()
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, "kalenyator_family_${System.currentTimeMillis()}.json")
            file.writeText(json)
            file
        }
    }

    suspend fun exportNewToCache(sinceMillis: Long): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val json = exportNewJsonString(sinceMillis).getOrThrow()
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, "kalenyator_family_new_${System.currentTimeMillis()}.json")
            file.writeText(json)
            file
        }
    }

    suspend fun importFromJson(json: String, mode: ImportMode = ImportMode.MERGE): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = gson.fromJson(json, FamilySyncFile::class.java)
                val incoming = payload.events.map { it.toEntity() }
                when (mode) {
                    ImportMode.REPLACE -> {
                        dao.getAll().forEach { dao.delete(it) }
                        dao.insertAll(incoming)
                        incoming.size
                    }
                    ImportMode.SKIP_DUPLICATES -> {
                        val existing = dao.getAll()
                        val toInsert = incoming.filter { inc -> !existing.any { it.isDuplicateOf(inc) } }
                        dao.insertAll(toInsert)
                        toInsert.size
                    }
                    ImportMode.MERGE -> {
                        dao.insertAll(incoming)
                        incoming.size
                    }
                }
            }
        }

    suspend fun importFromStream(input: java.io.InputStream, mode: ImportMode = ImportMode.MERGE): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = InputStreamReader(input).readText()
                importFromJson(json, mode).getOrThrow()
            }
        }

    private fun FamilyEventEntity.isDuplicateOf(other: FamilyEventEntity): Boolean =
        title.equals(other.title, ignoreCase = true) &&
            month == other.month &&
            day == other.day &&
            type == other.type
}
