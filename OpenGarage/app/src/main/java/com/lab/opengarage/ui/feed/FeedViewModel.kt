package com.lab.opengarage.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Car
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
    private val repo: RecordRepository,
) : ViewModel() {

    private data class Query(val make: String, val model: String)

    private val query = MutableStateFlow(Query("", ""))

    /** 제조사 필수, 모델은 선택. 모델 비면 제조사 전체 기록을 본다. */
    fun search(make: String, model: String) {
        query.value = Query(make.trim(), model.trim())
    }

    val records: StateFlow<List<Record>> = query
        .filter { it.make.isNotBlank() }
        .flatMapLatest { q ->
            if (q.model.isBlank()) repo.observeFeedByMake(q.make)
            else repo.observeFeed(Car.makeModelKey(q.make, q.model))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
