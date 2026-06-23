package com.lab.opengarage.ui.garage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.model.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CarDetailViewModel @Inject constructor(
    records: RecordRepository,
    cars: CarRepository,
    auth: AuthRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    private val carId: String = handle["carId"] ?: ""

    val records: StateFlow<List<Record>> = auth.currentUser
        .filterNotNull()
        .flatMapLatest { records.observeCarRecords(carId, it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val car = MutableStateFlow<Car?>(null)

    init {
        viewModelScope.launch { cars.getCar(carId).onSuccess { car.value = it } }
    }
}
