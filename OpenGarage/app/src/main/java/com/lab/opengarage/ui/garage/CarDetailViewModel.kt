package com.lab.opengarage.ui.garage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.model.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    records: RecordRepository,
    cars: CarRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    private val carId: String = handle["carId"] ?: ""

    val records: StateFlow<List<Record>> =
        records.observeCarRecords(carId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val car = MutableStateFlow<Car?>(null)

    init {
        viewModelScope.launch { cars.getCar(carId).onSuccess { car.value = it } }
    }
}
