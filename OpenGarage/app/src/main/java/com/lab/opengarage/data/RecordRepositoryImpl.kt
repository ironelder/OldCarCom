package com.lab.opengarage.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
) : RecordRepository {
    private val records = db.collection("records")

    private fun queryFlow(q: Query): Flow<List<Record>> = callbackFlow {
        val reg = q.addSnapshotListener { snap, e ->
            if (e != null) {
                close(e); return@addSnapshotListener
            }
            trySend(snap?.toObjects(Record::class.java) ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    override fun observeFeed(modelKey: String): Flow<List<Record>> = queryFlow(
        records.whereEqualTo("modelKey", modelKey)
            .whereEqualTo("isPublic", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
    )

    override fun observeCarRecords(carId: String): Flow<List<Record>> = queryFlow(
        records.whereEqualTo("carId", carId)
            .orderBy("date", Query.Direction.DESCENDING)
    )

    override fun observeMyPublicRecords(ownerUid: String): Flow<List<Record>> = queryFlow(
        records.whereEqualTo("ownerUid", ownerUid)
            .whereEqualTo("type", RecordType.MAINTENANCE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
    )

    override suspend fun getRecord(recordId: String): Result<Record> = runCatching {
        records.document(recordId).get().await().toObject(Record::class.java) ?: error("기록 없음")
    }

    override suspend fun upsertRecord(record: Record): Result<String> = runCatching {
        val ref = if (record.recordId.isBlank()) records.document() else records.document(record.recordId)
        val forced = record.copy(
            recordId = ref.id,
            isPublic = if (record.type == RecordType.FUEL) false else record.isPublic,
            createdAt = if (record.createdAt == 0L) System.currentTimeMillis() else record.createdAt,
        )
        ref.set(forced).await()
        ref.id
    }

    override suspend fun deleteRecord(recordId: String): Result<Unit> = runCatching {
        records.document(recordId).delete().await()
        Unit
    }
}
