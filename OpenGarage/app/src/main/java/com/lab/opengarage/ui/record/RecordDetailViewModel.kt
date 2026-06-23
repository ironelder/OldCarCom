package com.lab.opengarage.ui.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    records: RecordRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    val state = MutableStateFlow<UiState<Record>>(UiState.Loading)

    init {
        val id: String = handle["recordId"] ?: ""
        viewModelScope.launch {
            records.getRecord(id)
                .onSuccess { state.value = UiState.Success(it) }
                .onFailure { state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}
