package com.lab.opengarage.ui.profile

import com.lab.opengarage.fake.FakeAuthRepository
import com.lab.opengarage.fake.FakeCarRepository
import com.lab.opengarage.fake.FakeRecordRepository
import com.lab.opengarage.model.Car
import com.lab.opengarage.model.Record
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    private fun authWith(uid: String) = FakeAuthRepository().apply { state.value = User(uid, "테스터", "") }

    @Test fun signOut_clears_user() = runTest {
        val auth = authWith("uid1")
        val vm = ProfileViewModel(auth, FakeRecordRepository(), FakeCarRepository())
        vm.signOut()
        advanceUntilIdle()
        assertEquals(null, auth.state.value)
    }

    @Test fun withdraw_deleteContent_removes_records_and_cars() = runTest {
        val auth = authWith("uid1")
        val rec = FakeRecordRepository().apply {
            store.value = listOf(
                Record("r1", ownerUid = "uid1", type = RecordType.MAINTENANCE),
                Record("r2", ownerUid = "other", type = RecordType.MAINTENANCE),
            )
        }
        val cars = FakeCarRepository().apply { store.value = listOf(Car("c1", "uid1"), Car("c2", "other")) }
        val vm = ProfileViewModel(auth, rec, cars)
        vm.withdraw(deleteContent = true)
        advanceUntilIdle()
        assertEquals(listOf("r2"), rec.store.value.map { it.recordId }) // 내 기록만 삭제
        assertEquals(listOf("c2"), cars.store.value.map { it.carId })   // 내 차만 삭제
        assertEquals(null, auth.state.value)                            // 계정 삭제
    }

    @Test fun withdraw_keepContent_anonymizes_records() = runTest {
        val auth = authWith("uid1")
        val rec = FakeRecordRepository().apply {
            store.value = listOf(Record("r1", ownerUid = "uid1", ownerNickname = "테스터", type = RecordType.MAINTENANCE))
        }
        val vm = ProfileViewModel(auth, rec, FakeCarRepository())
        vm.withdraw(deleteContent = false)
        advanceUntilIdle()
        assertTrue(rec.store.value.isNotEmpty())                        // 글 유지
        assertEquals("비회원", rec.store.value.single().ownerNickname)   // 작성자 익명화
        assertEquals(null, auth.state.value)
    }
}
