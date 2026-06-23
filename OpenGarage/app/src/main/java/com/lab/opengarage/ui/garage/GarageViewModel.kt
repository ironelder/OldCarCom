package com.lab.opengarage.ui.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.ui.common.Paginator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GarageViewModel @Inject constructor(
    private val cars: CarRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private var uid: String? = null

    val paginator = Paginator { cursor, limit -> cars.myCarsPage(uid ?: "", cursor, limit) }

    init { viewModelScope.launch { ensureUid(); paginator.refresh() } }

    private suspend fun ensureUid() {
        if (uid == null) uid = auth.currentUser.filterNotNull().first().uid
    }

    fun refresh() = viewModelScope.launch { ensureUid(); paginator.refresh() }
    fun loadMore() = viewModelScope.launch { paginator.loadMore() }
}
