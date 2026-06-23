package com.lab.opengarage.data

import com.lab.opengarage.model.Record
import kotlinx.coroutines.flow.Flow

interface RecordRepository {
    /** 차종 피드(제조사+모델): 해당 modelKey 의 공개(shared=true) 기록만 최신순. */
    fun observeFeed(modelKey: String): Flow<List<Record>>
    /** 제조사 피드(모델 무관): 해당 make 의 공개 기록 전체 최신순. */
    fun observeFeedByMake(make: String): Flow<List<Record>>
    /** 특정 차(본인 소유)의 정비+주유 전체 기록(날짜 내림차순). 보안규칙상 ownerUid 제약 필요. */
    fun observeCarRecords(carId: String, ownerUid: String): Flow<List<Record>>
    /** 내가 쓴 공개 정비기록. */
    fun observeMyPublicRecords(ownerUid: String): Flow<List<Record>>
    suspend fun getRecord(recordId: String): Result<Record>
    /** 신규/수정 저장. recordId 가 비면 신규 발급. 저장된 recordId 반환. */
    suspend fun upsertRecord(record: Record): Result<String>
    suspend fun deleteRecord(recordId: String): Result<Unit>
}
