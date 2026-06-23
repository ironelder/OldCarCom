package com.lab.opengarage.fake

import com.lab.opengarage.data.Page
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRecordRepository : RecordRepository {
    val store = MutableStateFlow<List<Record>>(emptyList())

    private fun paged(all: List<Record>, cursor: Any?, limit: Int): Page<Record> {
        val offset = (cursor as? Int) ?: 0
        val slice = all.drop(offset).take(limit)
        val next = offset + slice.size
        return Page(slice, next, next >= all.size)
    }

    override suspend fun feedPage(make: String?, modelKey: String?, cursor: Any?, limit: Int): Page<Record> {
        val all = store.value.filter { r ->
            r.shared && when {
                modelKey != null -> r.modelKey == modelKey
                make != null -> r.make == make
                else -> true
            }
        }.sortedByDescending { it.createdAt }
        return paged(all, cursor, limit)
    }

    override suspend fun carRecordsPage(carId: String, ownerUid: String, cursor: Any?, limit: Int): Page<Record> {
        val all = store.value.filter { it.carId == carId && it.ownerUid == ownerUid }
            .sortedByDescending { it.date }
        return paged(all, cursor, limit)
    }

    override suspend fun myRecordsPage(ownerUid: String, cursor: Any?, limit: Int): Page<Record> {
        val all = store.value.filter { it.ownerUid == ownerUid && it.type == RecordType.MAINTENANCE }
            .sortedByDescending { it.createdAt }
        return paged(all, cursor, limit)
    }

    override suspend fun getRecord(recordId: String) =
        store.value.find { it.recordId == recordId }?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("없음"))

    override suspend fun upsertRecord(record: Record): Result<String> {
        val id = record.recordId.ifBlank { "rec_${store.value.size + 1}" }
        val forced = record.copy(
            recordId = id,
            shared = if (record.type == RecordType.FUEL) false else record.shared,
        )
        store.value = store.value.filterNot { it.recordId == id } + forced
        return Result.success(id)
    }

    override suspend fun deleteRecord(recordId: String): Result<Unit> {
        store.value = store.value.filterNot { it.recordId == recordId }
        return Result.success(Unit)
    }
}
