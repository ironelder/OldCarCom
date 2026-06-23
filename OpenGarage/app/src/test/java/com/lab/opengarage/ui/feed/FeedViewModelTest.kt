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

    @Test fun feed_shows_only_public_maintenance_of_modelKey() = runTest {
        val rec = FakeRecordRepository()
        rec.store.value = listOf(
            Record("r1", modelKey = "현대_프라이드", type = RecordType.MAINTENANCE, isPublic = true, createdAt = 2),
            Record("r2", modelKey = "현대_프라이드", type = RecordType.FUEL, isPublic = false, createdAt = 3),
            Record("r3", modelKey = "기아_프라이드", type = RecordType.MAINTENANCE, isPublic = true, createdAt = 1),
        )
        val vm = FeedViewModel(rec)
        vm.setModelKey("현대_프라이드")
        advanceUntilIdle()
        val list = vm.records.first { it.isNotEmpty() }
        assertEquals(listOf("r1"), list.map { it.recordId })
    }
}
