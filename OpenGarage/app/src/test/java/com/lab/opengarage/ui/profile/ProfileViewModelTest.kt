package com.lab.opengarage.ui.profile

import com.lab.opengarage.fake.FakeAuthRepository
import com.lab.opengarage.fake.FakeRecordRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun signOut_clears_user() = runTest {
        val auth = FakeAuthRepository().apply { state.value = User("uid1", "테스터", "") }
        val rec = FakeRecordRepository()
        val vm = ProfileViewModel(auth, rec)
        vm.signOut()
        advanceUntilIdle()
        assertEquals(null, auth.state.value)
    }
}
