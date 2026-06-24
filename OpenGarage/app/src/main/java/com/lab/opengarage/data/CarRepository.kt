package com.lab.opengarage.data

import com.lab.opengarage.model.Car

interface CarRepository {
    /** 내 차고 한 페이지(createdAt 내림차순). */
    suspend fun myCarsPage(ownerUid: String, cursor: Any?, limit: Int): Page<Car>
    suspend fun getCar(carId: String): Result<Car>
    /** 신규/수정 저장. carId 가 비면 신규 발급. 저장된 carId 반환. */
    suspend fun upsertCar(car: Car): Result<String>
    suspend fun deleteCar(carId: String): Result<Unit>
    /** 해당 사용자의 모든 차량 삭제(회원 탈퇴 - 차고 삭제 선택 시). */
    suspend fun deleteAllByOwner(ownerUid: String): Result<Unit>
}
