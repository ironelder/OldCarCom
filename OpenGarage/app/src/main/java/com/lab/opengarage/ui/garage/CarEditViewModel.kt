package com.lab.opengarage.ui.garage

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
) : ViewModel() {
    val saved = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun save(make: String, model: String, year: Int, nickname: String) {
        viewModelScope.launch {
            val uid = auth.currentUser.first()?.uid ?: run {
                error.value = "로그인 필요"; return@launch
            }
            cars.upsertCar(
                Car(ownerUid = uid, make = make, model = model, year = year, nickname = nickname),
            ).onSuccess { saved.value = true }
                .onFailure { error.value = it.message }
        }
    }
}
