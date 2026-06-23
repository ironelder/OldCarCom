package com.lab.opengarage.ui.garage

import androidx.lifecycle.SavedStateHandle
import com.lab.opengarage.fake.FakeAuthRepository
import com.lab.opengarage.fake.FakeCarRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarEditViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun save_persists_car_with_modelKey_and_owner() = runTest {
        val cars = FakeCarRepository()
        val auth = FakeAuthRepository().apply { state.value = User("uid1", "테스터", "") }
        val vm = CarEditViewModel(cars, auth, SavedStateHandle())
        vm.save("현대", "프라이드", 2005, "은마")
        advanceUntilIdle()
        val saved = cars.store.value.single()
        assertEquals("현대_프라이드", saved.modelKey)
        assertEquals("uid1", saved.ownerUid)
        assertTrue(vm.saved.value)
    }
}
