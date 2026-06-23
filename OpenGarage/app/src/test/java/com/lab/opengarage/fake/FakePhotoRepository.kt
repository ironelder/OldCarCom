package com.lab.opengarage.fake

import android.net.Uri
import com.lab.opengarage.data.PhotoRepository

class FakePhotoRepository : PhotoRepository {
    override suspend fun uploadRecordPhotos(uid: String, recordId: String, uris: List<Uri>) =
        Result.success(emptyList<String>())
}
