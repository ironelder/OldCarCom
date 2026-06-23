package com.lab.opengarage.ui.login

import com.lab.opengarage.fake.FakeAuthRepository
import com.lab.opengarage.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tear() = Dispatchers.resetMain()

    @Test fun signIn_success_sets_Success_state() = runTest {
        val auth = FakeAuthRepository()
        val vm = LoginViewModel(auth)
        vm.onIdToken("token")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UiState.Success)
        assertTrue(auth.state.value != null)
    }
}
