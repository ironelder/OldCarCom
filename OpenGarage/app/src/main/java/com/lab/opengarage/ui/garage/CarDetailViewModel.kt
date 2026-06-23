package com.lab.opengarage.ui.garage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.ui.common.Paginator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    private val records: RecordRepository,
    private val cars: CarRepository,
    private val auth: AuthRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    private val carId: String = handle["carId"] ?: ""
    private var uid: String? = null

    val car = MutableStateFlow<Car?>(null)
    val deleted = MutableStateFlow(false)
    val paginator = Paginator { cursor, limit -> records.carRecordsPage(carId, uid ?: "", cursor, limit) }

    init {
        viewModelScope.launch { cars.getCar(carId).onSuccess { car.value = it } }
        viewModelScope.launch { ensureUid(); paginator.refresh() }
    }

    private suspend fun ensureUid() {
        if (uid == null) uid = auth.currentUser.filterNotNull().first().uid
    }

    fun refresh() = viewModelScope.launch { ensureUid(); paginator.refresh() }
    fun loadMore() = viewModelScope.launch { paginator.loadMore() }

    fun deleteCar() = viewModelScope.launch {
        cars.deleteCar(carId).onSuccess { deleted.value = true }
    }
}
