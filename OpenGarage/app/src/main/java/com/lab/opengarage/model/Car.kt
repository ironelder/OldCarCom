package com.lab.opengarage.model

/**
 * 내 차고의 차량. Firestore `cars/{carId}` 문서와 매핑.
 *
 * [modelKey] 는 제조사+모델을 정규화한 차종 피드 쿼리 키로,
 * 차 등록/수정 시 [makeModelKey] 로 자동 생성한다.
 */
data class Car(
    val carId: String = "",
    val ownerUid: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val modelKey: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
) {
    companion object {
        /** 제조사·모델을 공백 제거 후 `"{make}_{model}"` 형태로 합친 차종 키. */
        fun makeModelKey(make: String, model: String): String =
            "${make.trim()}_${model.trim()}"
    }
}
