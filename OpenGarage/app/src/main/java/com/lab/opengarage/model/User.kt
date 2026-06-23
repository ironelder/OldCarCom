package com.lab.opengarage.model

/**
 * 앱 사용자. Firestore `users/{uid}` 문서와 1:1 매핑.
 * Firestore 역직렬화(toObject)를 위해 모든 필드는 기본값을 가진다.
 */
data class User(
    val uid: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
)
