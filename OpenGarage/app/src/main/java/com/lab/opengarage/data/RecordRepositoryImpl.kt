package com.lab.opengarage.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
) : RecordRepository {
    private val records = db.collection("records")

    private suspend fun page(base: Query, cursor: Any?, limit: Int): Page<Record> {
        var q = base.limit(limit.toLong())
        if (cursor is DocumentSnapshot) q = q.startAfter(cursor)
        val snap = q.get().await()
        val docs = snap.documents
        val items = docs.mapNotNull { it.toObject(Record::class.java) }
        return Page(items, docs.lastOrNull(), docs.size < limit)
    }

    override suspend fun feedPage(make: String?, modelKey: String?, cursor: Any?, limit: Int): Page<Record> {
        val base = when {
            modelKey != null -> records.whereEqualTo("modelKey", modelKey).whereEqualTo("shared", true)
            make != null -> records.whereEqualTo("make", make).whereEqualTo("shared", true)
            else -> records.whereEqualTo("shared", true)
        }.orderBy("createdAt", Query.Direction.DESCENDING)
        return page(base, cursor, limit)
    }

    override suspend fun carRecordsPage(carId: String, ownerUid: String, cursor: Any?, limit: Int): Page<Record> {
        val base = records.whereEqualTo("ownerUid", ownerUid)
            .whereEqualTo("carId", carId)
            .orderBy("date", Query.Direction.DESCENDING)
        return page(base, cursor, limit)
    }

    override suspend fun myRecordsPage(ownerUid: String, cursor: Any?, limit: Int): Page<Record> {
        val base = records.whereEqualTo("ownerUid", ownerUid)
            .whereEqualTo("type", RecordType.MAINTENANCE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        return page(base, cursor, limit)
    }

    override suspend fun getRecord(recordId: String): Result<Record> = runCatching {
        records.document(recordId).get().await().toObject(Record::class.java) ?: error("기록 없음")
    }

    override suspend fun upsertRecord(record: Record): Result<String> = runCatching {
        val ref = if (record.recordId.isBlank()) records.document() else records.document(record.recordId)
        val forced = record.copy(
            recordId = ref.id,
            shared = if (record.type == RecordType.FUEL) false else record.shared,
            createdAt = if (record.createdAt == 0L) System.currentTimeMillis() else record.createdAt,
        )
        ref.set(forced).await()
        ref.id
    }

    override suspend fun deleteRecord(recordId: String): Result<Unit> = runCatching {
        records.document(recordId).delete().await()
        Unit
    }

    override suspend fun deleteAllByOwner(ownerUid: String): Result<Unit> = runCatching {
        val snap = records.whereEqualTo("ownerUid", ownerUid).get().await()
        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
        Unit
    }

    override suspend fun anonymizeOwner(ownerUid: String): Result<Unit> = runCatching {
        val snap = records.whereEqualTo("ownerUid", ownerUid).get().await()
        val batch = db.batch()
        snap.documents.forEach { batch.update(it.reference, "ownerNickname", "비회원") }
        batch.commit().await()
        Unit
    }
}
