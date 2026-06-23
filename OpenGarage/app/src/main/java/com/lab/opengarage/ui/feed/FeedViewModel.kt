package com.lab.opengarage.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.ui.common.Paginator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repo: RecordRepository,
) : ViewModel() {

    // 입력값(탭 이동 후에도 ViewModel 이 살아있어 유지됨)
    val make = MutableStateFlow("")
    val model = MutableStateFlow("")

    private var curMake: String? = null
    private var curModelKey: String? = null

    val paginator = Paginator { cursor, limit -> repo.feedPage(curMake, curModelKey, cursor, limit) }

    init {
        // 최초 진입: 필터 없이 전체 공개 피드
        viewModelScope.launch { paginator.refresh() }
    }

    fun setMake(value: String) { make.value = value }
    fun setModel(value: String) { model.value = value }

    /** 제조사 필수, 모델 선택. 모델 비면 제조사 전체. 둘 다 비면 전체 피드. */
    fun search() {
        val mk = make.value.trim()
        val md = model.value.trim()
        curMake = mk.ifBlank { null }
        curModelKey = if (mk.isBlank() || md.isBlank()) null else Car.makeModelKey(mk, md)
        viewModelScope.launch { paginator.refresh() }
    }

    fun loadMore() {
        viewModelScope.launch { paginator.loadMore() }
    }
}
