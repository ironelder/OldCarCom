package com.lab.opengarage.data

import com.lab.opengarage.model.Record

interface RecordRepository {
    /**
     * 차종 피드 한 페이지. 공개(shared=true) 기록만 createdAt 내림차순.
     * - [modelKey] 지정 → 해당 차종
     * - [modelKey] null & [make] 지정 → 해당 제조사 전체
     * - 둘 다 null → 전체 공개 피드
     */
    suspend fun feedPage(make: String?, modelKey: String?, cursor: Any?, limit: Int): Page<Record>

    /** 특정 차(본인 소유)의 정비+주유 기록 한 페이지(date 내림차순). */
    suspend fun carRecordsPage(carId: String, ownerUid: String, cursor: Any?, limit: Int): Page<Record>

    /** 내 정비기록 한 페이지(createdAt 내림차순). */
    suspend fun myRecordsPage(ownerUid: String, cursor: Any?, limit: Int): Page<Record>

    suspend fun getRecord(recordId: String): Result<Record>
    suspend fun upsertRecord(record: Record): Result<String>
    suspend fun deleteRecord(recordId: String): Result<Unit>
    /** 해당 사용자의 모든 기록 삭제(회원 탈퇴 - 글 삭제 선택 시). */
    suspend fun deleteAllByOwner(ownerUid: String): Result<Unit>
    /** 해당 사용자의 모든 기록 작성자 표기를 "비회원"으로 치환(글 유지 선택 시). */
    suspend fun anonymizeOwner(ownerUid: String): Result<Unit>
}
