package com.lab.opengarage.fake

import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.model.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCarRepository : CarRepository {
    val store = MutableStateFlow<List<Car>>(emptyList())

    override fun observeMyCars(ownerUid: String) =
        store.map { list -> list.filter { it.ownerUid == ownerUid } }

    override suspend fun getCar(carId: String) =
        store.value.find { it.carId == carId }?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("없음"))

    override suspend fun upsertCar(car: Car): Result<String> {
        val id = car.carId.ifBlank { "car_${store.value.size + 1}" }
        val saved = car.copy(carId = id, modelKey = Car.makeModelKey(car.make, car.model))
        store.value = store.value.filterNot { it.carId == id } + saved
        return Result.success(id)
    }

    override suspend fun deleteCar(carId: String): Result<Unit> {
        store.value = store.value.filterNot { it.carId == carId }
        return Result.success(Unit)
    }
}
