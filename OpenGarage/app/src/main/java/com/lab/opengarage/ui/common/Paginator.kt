package com.lab.opengarage.ui.common

import android.util.Log
import com.lab.opengarage.data.Page
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 커서 기반 무한스크롤 페이지네이터.
 * [source] 는 (cursor, limit) → 한 페이지를 반환. 첫 호출은 cursor=null.
 *
 * 동시 호출/끝 도달을 막아 LazyColumn 끝에서 [loadMore] 를 반복 호출해도 안전하다.
 */
class Paginator<T>(
    private val pageSize: Int = 30,
    private val source: suspend (cursor: Any?, limit: Int) -> Page<T>,
) {
    val items = MutableStateFlow<List<T>>(emptyList())
    val loading = MutableStateFlow(false)
    val endReached = MutableStateFlow(false)
    /** 첫 페이지 로드를 1회라도 끝냈는지 — 빈 결과와 로딩 전 상태 구분용. */
    val initialized = MutableStateFlow(false)

    private var cursor: Any? = null
    private var inFlight = false

    /** 미리 받아둔 첫 페이지로 즉시 초기화(프리페치 재사용). */
    fun seed(page: Page<T>) {
        cursor = page.cursor
        items.value = page.items
        endReached.value = page.endReached
        initialized.value = true
    }

    /**
     * 첫 페이지를 다시 불러와 교체(soft). 기존 items 를 비우지 않아 화면 깜빡임이 없다.
     */
    suspend fun refresh() {
        if (inFlight) return
        inFlight = true
        loading.value = true
        try {
            val p = source(null, pageSize)
            cursor = p.cursor
            items.value = p.items
            endReached.value = p.endReached
        } catch (e: Exception) {
            Log.w("Paginator", "refresh failed: ${e.message}", e)
            endReached.value = true
        } finally {
            initialized.value = true
            loading.value = false
            inFlight = false
        }
    }

    /** 다음 페이지 로드. 진행 중이거나 끝이면 무시. */
    suspend fun loadMore() {
        if (inFlight || endReached.value) return
        inFlight = true
        loading.value = true
        try {
            val p = source(cursor, pageSize)
            cursor = p.cursor
            items.value = items.value + p.items
            if (p.endReached) endReached.value = true
        } catch (e: Exception) {
            Log.w("Paginator", "load failed: ${e.message}", e)
            endReached.value = true // 권한/인덱스 오류 시 무한 재시도 방지
        } finally {
            initialized.value = true
            loading.value = false
            inFlight = false
        }
    }
}
