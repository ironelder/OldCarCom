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
        /**
         * 차종 피드 매칭용 정규 키. 제조사(고정 목록 정규명)와 정규화한 모델명을 합친다.
         *
         * 모델은 표기 편차(대소문자/공백/하이픈/언더스코어)를 제거해 같은 모델이 한 키로 모이게 한다.
         * 예) "3 Series", "3-series", "3SERIES" → 모두 같은 키.
         * 단 언어가 다르면(예: "프라이드" vs "Pride") 통일되지 않는다(정규화 한계).
         */
        fun makeModelKey(make: String, model: String): String =
            "${make.trim()}_${normalizeModel(model)}"

        /** 모델명을 소문자화하고 공백·하이픈·언더스코어를 제거한 정규형. */
        fun normalizeModel(model: String): String =
            model.trim().lowercase().replace(Regex("[\\s\\-_]+"), "")
    }
}
