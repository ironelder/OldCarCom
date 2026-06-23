package com.lab.opengarage.data

import android.net.Uri

interface PhotoRepository {
    /** 기록 사진들을 Storage 에 업로드하고 다운로드 URL 목록을 반환. */
    suspend fun uploadRecordPhotos(uid: String, recordId: String, uris: List<Uri>): Result<List<String>>
}
