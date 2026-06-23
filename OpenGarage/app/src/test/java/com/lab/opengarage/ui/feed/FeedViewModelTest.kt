package com.lab.opengarage.ui.feed

import com.lab.opengarage.fake.FakeRecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
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
class FeedViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    private fun seeded() = FakeRecordRepository().apply {
        store.value = listOf(
            Record("r1", make = "현대", modelKey = "현대_프라이드", type = RecordType.MAINTENANCE, shared = true, createdAt = 2),
            Record("r2", make = "현대", modelKey = "현대_프라이드", type = RecordType.FUEL, shared = false, createdAt = 3),
            Record("r3", make = "현대", modelKey = "현대_아반떼", type = RecordType.MAINTENANCE, shared = true, createdAt = 1),
            Record("r4", make = "기아", modelKey = "기아_프라이드", type = RecordType.MAINTENANCE, shared = true, createdAt = 5),
        )
    }

    @Test fun initial_feed_shows_all_public_records() = runTest {
        val vm = FeedViewModel(seeded())
        advanceUntilIdle()
        assertEquals(listOf("r4", "r1", "r3"), vm.paginator.items.value.map { it.recordId }) // 공개만, createdAt desc
    }

    @Test fun search_by_model_shows_only_that_model() = runTest {
        val vm = FeedViewModel(seeded())
        vm.setMake("현대"); vm.setModel("프라이드"); vm.search()
        advanceUntilIdle()
        assertEquals(listOf("r1"), vm.paginator.items.value.map { it.recordId })
    }

    @Test fun search_by_make_only_shows_all_of_manufacturer() = runTest {
        val vm = FeedViewModel(seeded())
        vm.setMake("현대"); vm.setModel(""); vm.search()
        advanceUntilIdle()
        assertEquals(listOf("r1", "r3"), vm.paginator.items.value.map { it.recordId })
    }
}
