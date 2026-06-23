package com.lab.opengarage.ui.record

import com.lab.opengarage.fake.FakeAuthRepository
import com.lab.opengarage.fake.FakeCarRepository
import com.lab.opengarage.fake.FakePhotoRepository
import com.lab.opengarage.fake.FakeRecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.model.FuelType
import com.lab.opengarage.model.RecordType
import com.lab.opengarage.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordEditViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    private fun newVm(): Pair<RecordEditViewModel, FakeRecordRepository> {
        val rec = FakeRecordRepository()
        val car = FakeCarRepository()
        val auth = FakeAuthRepository().apply { state.value = User("uid1", "테스터", "") }
        val photo = FakePhotoRepository()
        car.store.value = listOf(Car("c1", "uid1", "현대", "프라이드", 2005, "현대_프라이드", "은마"))
        return RecordEditViewModel(rec, photo, car, auth) to rec
    }

    @Test fun fuel_record_forced_private() = runTest {
        val (vm, rec) = newVm()
        vm.save(
            RecordForm(
                carId = "c1", type = RecordType.FUEL, title = "주유", mileageKm = 120000,
                cost = 70000, liters = 40.0, fuelType = FuelType.GASOLINE, isPublic = true,
            ),
            emptyList(),
        )
        advanceUntilIdle()
        assertFalse(rec.store.value.single().isPublic)
    }

    @Test fun maintenance_record_keeps_public_and_modelKey() = runTest {
        val (vm, rec) = newVm()
        vm.save(
            RecordForm(
                carId = "c1", type = RecordType.MAINTENANCE, title = "타이밍벨트",
                mileageKm = 130000, cost = 300000, isPublic = true,
            ),
            emptyList(),
        )
        advanceUntilIdle()
        val r = rec.store.value.single()
        assertTrue(r.isPublic)
        assertEquals("현대_프라이드", r.modelKey)
    }
}
