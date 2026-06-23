package com.lab.opengarage.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
) : PhotoRepository {
    override suspend fun uploadRecordPhotos(
        uid: String,
        recordId: String,
        uris: List<Uri>,
    ): Result<List<String>> = runCatching {
        uris.mapIndexed { i, uri ->
            val ref = storage.reference.child("users/$uid/records/$recordId/photo_$i.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }
    }
}
