package com.lab.opengarage.model

/** 기록 종류. MAINTENANCE=공유 정비노트, FUEL=비공개 주유 차계부. */
enum class RecordType { MAINTENANCE, FUEL }

/** 주유 시 유종. */
enum class FuelType { GASOLINE, DIESEL, LPG }

/**
 * 정비·주유 기록. Firestore `records/{recordId}` 문서와 매핑.
 *
 * 차종 피드를 join 없이 1쿼리로 처리하기 위해 [make], [modelKey], [ownerNickname] 을 비정규화 저장한다.
 * [type]=FUEL 기록은 항상 [shared]=false 로 강제되어 피드에 노출되지 않는다(작성 단계에서 보장).
 * [liters], [fuelType] 은 FUEL 기록에서만 채워진다.
 *
 * 주의: 공개 여부 필드명을 `isPublic` 으로 두면 Kotlin `is`-getter + Firestore JavaBean 매핑 때문에
 * 실제 저장 필드가 `public` 이 되어 `whereEqualTo("isPublic", ...)` 가 매칭되지 않는다.
 * 이를 피하려 [shared] 로 명명한다.
 */
data class Record(
    val recordId: String = "",
    val carId: String = "",
    val ownerUid: String = "",
    val ownerNickname: String = "",
    val make: String = "",
    val modelKey: String = "",
    val years: String = "", // 연식/세대 표기 (예: "1965", "1세대(1964-1973)")
    val type: RecordType = RecordType.MAINTENANCE,
    val date: Long = 0L,
    val mileageKm: Int = 0,
    val title: String = "",
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val cost: Long = 0L,
    val liters: Double? = null,
    val fuelType: FuelType? = null,
    val shared: Boolean = true,
    val createdAt: Long = 0L,
)
