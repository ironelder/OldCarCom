package com.lab.opengarage.ui.record

import androidx.lifecycle.SavedStateHandle
import com.lab.opengarage.fake.FakeRecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.ui.common.UiState
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
class RecordDetailViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun loads_record_by_id() = runTest {
        val rec = FakeRecordRepository().apply {
            store.value = listOf(Record("r1", title = "타이밍벨트"))
        }
        val vm = RecordDetailViewModel(rec, SavedStateHandle(mapOf("recordId" to "r1")))
        advanceUntilIdle()
        val s = vm.state.first { it is UiState.Success }
        assertEquals("타이밍벨트", (s as UiState.Success).data.title)
    }
}
