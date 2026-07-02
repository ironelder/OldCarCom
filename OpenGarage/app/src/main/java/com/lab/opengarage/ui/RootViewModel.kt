package com.lab.opengarage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.FeedCache
import com.lab.opengarage.data.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/** 앱 진입 인증 상태. Loading=세션 복원 중(스플래시), 이후 로그인/비로그인 확정. */
enum class AuthUi { Loading, SignedIn, SignedOut }

private const val FEED_PAGE = 30
private const val PREFETCH_TIMEOUT_MS = 5000L

@HiltViewModel
class RootViewModel @Inject constructor(
    auth: AuthRepository,
    private val records: RecordRepository,
    private val feedCache: FeedCache,
) : ViewModel() {
    val authState: StateFlow<AuthUi> = auth.currentUser
        .map { if (it != null) AuthUi.SignedIn else AuthUi.SignedOut }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUi.Loading)

    /** 스플래시 유지 조건: false 동안 시스템 스플래시 표시(세션 복원 + 첫 피드 프리페치). */
    val ready = MutableStateFlow(false)
    private var prefetched = false

    init {
        viewModelScope.launch {
            authState.collect { state ->
                when (state) {
                    AuthUi.Loading -> Unit
                    AuthUi.SignedOut -> ready.value = true
                    AuthUi.SignedIn -> if (!prefetched) {
                        prefetched = true
                        // 첫 피드 페이지 프리페치(타임아웃으로 무한 대기 방지)
                        withTimeoutOrNull(PREFETCH_TIMEOUT_MS) {
                            runCatching { feedCache.firstPage = records.feedPage(null, null, null, FEED_PAGE) }
                        }
                        ready.value = true
                    }
                }
            }
        }
    }
}
