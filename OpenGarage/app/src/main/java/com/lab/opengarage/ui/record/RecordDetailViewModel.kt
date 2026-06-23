package com.lab.opengarage.ui.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val records: RecordRepository,
    auth: AuthRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    private val recordId: String = handle["recordId"] ?: ""

    val state = MutableStateFlow<UiState<Record>>(UiState.Loading)
    val deleted = MutableStateFlow(false)

    /** 현재 로그인 사용자 uid(본인 기록 여부 판단용). */
    val currentUid: StateFlow<String?> =
        auth.currentUser.map { it?.uid }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            records.getRecord(recordId)
                .onSuccess { state.value = UiState.Success(it) }
                .onFailure { state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }

    fun delete() = viewModelScope.launch {
        records.deleteRecord(recordId).onSuccess { deleted.value = true }
    }
}
