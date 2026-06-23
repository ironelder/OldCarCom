package com.lab.opengarage.ui.record

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.data.PhotoRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.FuelType
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 기록 작성 폼 입력값. */
data class RecordForm(
    val carId: String,
    val type: RecordType,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val mileageKm: Int = 0,
    val description: String = "",
    val cost: Long = 0L,
    val liters: Double? = null,
    val fuelType: FuelType? = null,
    val isPublic: Boolean = true,
)

@HiltViewModel
class RecordEditViewModel @Inject constructor(
    private val records: RecordRepository,
    private val photos: PhotoRepository,
    private val cars: CarRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    val saved = MutableStateFlow(false)
    val saving = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun save(form: RecordForm, photoUris: List<Uri>) {
        if (saving.value) return // 중복 저장 방지
        viewModelScope.launch {
            saving.value = true
            val user = auth.currentUser.first() ?: run {
                error.value = "로그인 필요"; saving.value = false; return@launch
            }
            val car = cars.getCar(form.carId).getOrElse {
                error.value = "차량 없음"; saving.value = false; return@launch
            }
            val base = Record(
                carId = form.carId,
                ownerUid = user.uid,
                ownerNickname = user.nickname,
                make = car.make,
                modelKey = car.modelKey,
                type = form.type,
                date = form.date,
                mileageKm = form.mileageKm,
                title = form.title,
                description = form.description,
                cost = form.cost,
                liters = form.liters,
                fuelType = form.fuelType,
                shared = if (form.type == RecordType.FUEL) false else form.isPublic,
            )
            val id = records.upsertRecord(base).getOrElse {
                error.value = it.message; saving.value = false; return@launch
            }
            if (photoUris.isNotEmpty()) {
                photos.uploadRecordPhotos(user.uid, id, photoUris)
                    .onSuccess { urls -> records.upsertRecord(base.copy(recordId = id, photoUrls = urls)) }
            }
            saved.value = true
        }
    }
}
