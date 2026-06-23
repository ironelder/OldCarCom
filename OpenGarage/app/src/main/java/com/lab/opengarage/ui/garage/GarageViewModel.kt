package com.lab.opengarage.ui.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.model.Car
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageViewModel @Inject constructor(
    cars: CarRepository,
    auth: AuthRepository,
) : ViewModel() {
    val cars: StateFlow<List<Car>> = auth.currentUser
        .filterNotNull()
        .flatMapLatest { cars.observeMyCars(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
