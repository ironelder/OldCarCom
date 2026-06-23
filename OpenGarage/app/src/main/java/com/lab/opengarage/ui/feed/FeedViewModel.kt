package com.lab.opengarage.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    records: RecordRepository,
) : ViewModel() {
    private val modelKey = MutableStateFlow("")

    fun setModelKey(key: String) {
        modelKey.value = key
    }

    val records: StateFlow<List<Record>> = modelKey
        .filter { it.isNotBlank() }
        .flatMapLatest { records.observeFeed(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
