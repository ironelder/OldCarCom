package com.lab.opengarage.data

/**
 * 페이지네이션 한 페이지 결과.
 *
 * @param items 이번 페이지 항목들
 * @param cursor 다음 페이지 조회에 넘길 불투명 커서(구현체 내부 타입: Firestore DocumentSnapshot / Fake Int). null이면 처음부터.
 * @param endReached 더 이상 페이지가 없으면 true
 */
data class Page<T>(
    val items: List<T>,
    val cursor: Any?,
    val endReached: Boolean,
)
