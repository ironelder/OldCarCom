package com.lab.opengarage.ui.feed

import com.lab.opengarage.fake.FakeRecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
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
class FeedViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    private fun seeded(): FakeRecordRepository = FakeRecordRepository().apply {
        store.value = listOf(
            Record("r1", make = "현대", modelKey = "현대_프라이드", type = RecordType.MAINTENANCE, shared = true, createdAt = 2),
            Record("r2", make = "현대", modelKey = "현대_프라이드", type = RecordType.FUEL, shared = false, createdAt = 3),
            Record("r3", make = "현대", modelKey = "현대_아반떼", type = RecordType.MAINTENANCE, shared = true, createdAt = 1),
            Record("r4", make = "기아", modelKey = "기아_프라이드", type = RecordType.MAINTENANCE, shared = true, createdAt = 5),
        )
    }

    @Test fun feed_by_modelKey_shows_only_public_of_that_model() = runTest {
        val vm = FeedViewModel(seeded())
        vm.search("현대", "프라이드")
        advanceUntilIdle()
        val list = vm.records.first { it.isNotEmpty() }
        assertEquals(listOf("r1"), list.map { it.recordId }) // 주유(r2) 제외, 타모델/타제조사 제외
    }

    @Test fun feed_by_make_only_shows_all_public_of_manufacturer() = runTest {
        val vm = FeedViewModel(seeded())
        vm.search("현대", "") // 모델 빈값 → 제조사 전체
        advanceUntilIdle()
        val list = vm.records.first { it.isNotEmpty() }
        assertEquals(listOf("r1", "r3"), list.map { it.recordId }) // 현대 공개 정비 2건(주유 제외), createdAt desc
    }
}
