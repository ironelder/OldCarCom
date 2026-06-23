package com.lab.opengarage.ui.garage

import androidx.lifecycle.SavedStateHandle
import com.lab.opengarage.fake.FakeAuthRepository
import com.lab.opengarage.fake.FakeCarRepository
import com.lab.opengarage.fake.FakeRecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import com.lab.opengarage.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarDetailViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun shows_both_fuel_and_maintenance_for_car() = runTest {
        val rec = FakeRecordRepository()
        rec.store.value = listOf(
            Record("r1", "c1", ownerUid = "uid1", type = RecordType.MAINTENANCE, date = 2, modelKey = "현대_프라이드", isPublic = true),
            Record("r2", "c1", ownerUid = "uid1", type = RecordType.FUEL, date = 1, isPublic = false),
            Record("r3", "cX", ownerUid = "uid1", type = RecordType.MAINTENANCE, date = 9, isPublic = true),
        )
        val car = FakeCarRepository().apply { store.value = listOf(Car("c1", "uid1", "현대", "프라이드")) }
        val auth = FakeAuthRepository().apply { state.value = User("uid1", "테스터", "") }
        val vm = CarDetailViewModel(rec, car, auth, SavedStateHandle(mapOf("carId" to "c1")))
        advanceUntilIdle()
        val list = vm.records.first { it.isNotEmpty() }
        assertEquals(listOf("r1", "r2"), list.map { it.recordId })
    }
}
