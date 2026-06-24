package com.lab.opengarage.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.User
import com.lab.opengarage.ui.common.Paginator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val records: RecordRepository,
    private val cars: CarRepository,
) : ViewModel() {
    private var uid: String? = null

    val withdrawing = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    val user: StateFlow<User?> =
        auth.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val paginator = Paginator { cursor, limit -> records.myRecordsPage(uid ?: "", cursor, limit) }

    init { viewModelScope.launch { ensureUid(); paginator.refresh() } }

    private suspend fun ensureUid() {
        if (uid == null) uid = auth.currentUser.filterNotNull().first().uid
    }

    fun refresh() = viewModelScope.launch { ensureUid(); paginator.refresh() }
    fun loadMore() = viewModelScope.launch { paginator.loadMore() }
    fun signOut() = viewModelScope.launch { auth.signOut() }

    /**
     * 회원 탈퇴. [deleteContent]=true 면 내 기록·차량 전부 삭제,
     * false 면 글은 남기고 작성자 표기만 "비회원"으로 익명화한 뒤 계정 삭제.
     */
    fun withdraw(deleteContent: Boolean) {
        if (withdrawing.value) return
        viewModelScope.launch {
            withdrawing.value = true
            ensureUid()
            val id = uid ?: run { error.value = "로그인 필요"; withdrawing.value = false; return@launch }
            if (deleteContent) {
                records.deleteAllByOwner(id)
                cars.deleteAllByOwner(id)
            } else {
                records.anonymizeOwner(id)
            }
            auth.deleteAccount().onFailure {
                error.value = "탈퇴 실패: 다시 로그인 후 시도해주세요"
                withdrawing.value = false
            }
            // 성공 시 currentUser==null → 자동으로 로그인 화면 이동
        }
    }
}

