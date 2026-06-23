package com.lab.opengarage.fake

import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecordRepository : RecordRepository {
    val store = MutableStateFlow<List<Record>>(emptyList())

    override fun observeFeed(modelKey: String) =
        store.map { l -> l.filter { it.modelKey == modelKey && it.shared }.sortedByDescending { it.createdAt } }

    override fun observeFeedByMake(make: String) =
        store.map { l -> l.filter { it.make == make && it.shared }.sortedByDescending { it.createdAt } }

    override fun observeCarRecords(carId: String, ownerUid: String) =
        store.map { l -> l.filter { it.carId == carId && it.ownerUid == ownerUid }.sortedByDescending { it.date } }

    override fun observeMyPublicRecords(ownerUid: String) =
        store.map { l -> l.filter { it.ownerUid == ownerUid && it.type == RecordType.MAINTENANCE } }

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
