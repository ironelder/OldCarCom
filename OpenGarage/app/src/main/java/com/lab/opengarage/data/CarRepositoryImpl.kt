package com.lab.opengarage.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lab.opengarage.model.Car
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
) : CarRepository {
    private val cars = db.collection("cars")

    override fun observeMyCars(ownerUid: String): Flow<List<Car>> = callbackFlow {
        val reg = cars.whereEqualTo("ownerUid", ownerUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e); return@addSnapshotListener
                }
                trySend(snap?.toObjects(Car::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getCar(carId: String): Result<Car> = runCatching {
        cars.document(carId).get().await().toObject(Car::class.java) ?: error("차량 없음")
    }

    override suspend fun upsertCar(car: Car): Result<String> = runCatching {
        val ref = if (car.carId.isBlank()) cars.document() else cars.document(car.carId)
        val toSave = car.copy(
            carId = ref.id,
            modelKey = Car.makeModelKey(car.make, car.model),
            createdAt = if (car.createdAt == 0L) System.currentTimeMillis() else car.createdAt,
        )
        ref.set(toSave).await()
        ref.id
    }

    override suspend fun deleteCar(carId: String): Result<Unit> = runCatching {
        cars.document(carId).delete().await()
        Unit
    }
}
