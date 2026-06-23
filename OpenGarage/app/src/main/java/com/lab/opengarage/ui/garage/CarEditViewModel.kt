package com.lab.opengarage.ui.garage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.model.Car
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarEditViewModel @Inject constructor(
    private val cars: CarRepository,
    private val auth: AuthRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    private val carId: String = handle["carId"] ?: ""
    val editing: Boolean = carId.isNotBlank()

    /** 수정 모드 프리필용 기존 차량. */
    val initial = MutableStateFlow<Car?>(null)
    val saved = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    init {
        if (editing) viewModelScope.launch { cars.getCar(carId).onSuccess { initial.value = it } }
    }

    fun save(make: String, model: String, year: Int, nickname: String) {
        viewModelScope.launch {
            val uid = auth.currentUser.first()?.uid ?: run {
                error.value = "로그인 필요"; return@launch
            }
            val prev = initial.value
            cars.upsertCar(
                Car(
                    carId = carId,
                    ownerUid = uid,
                    make = make,
                    model = model,
                    year = year,
                    nickname = nickname,
                    photoUrl = prev?.photoUrl ?: "",
                    createdAt = prev?.createdAt ?: 0L,
                ),
            ).onSuccess { saved.value = true }
                .onFailure { error.value = it.message }
        }
    }
}
