package com.lab.opengarage.ui.garage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.CarCard
import com.lab.opengarage.ui.common.InfiniteScrollEffect
import com.lab.opengarage.ui.common.LoadingFooter

@Composable
fun GarageScreen(
    onAddCar: () -> Unit,
    onCarClick: (String) -> Unit,
    vm: GarageViewModel = hiltViewModel(),
) {
    val cars by vm.paginator.items.collectAsStateWithLifecycle()
    val loading by vm.paginator.loading.collectAsStateWithLifecycle()
    val initialized by vm.paginator.initialized.collectAsStateWithLifecycle()
    val endReached by vm.paginator.endReached.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 화면 복귀 시 새로고침(차 추가 후 반영)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    InfiniteScrollEffect(listState) { vm.loadMore() }

    Box(Modifier.fillMaxSize()) {
        when {
            !initialized && loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            initialized && cars.isEmpty() -> Text(
                "아직 등록한 차가 없어요. + 로 추가하세요.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
            )
            else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(cars, key = { it.carId }) { car ->
                    CarCard(car) { onCarClick(car.carId) }
                }
                if (loading && !endReached) item { LoadingFooter() }
            }
        }
        FloatingActionButton(
            onClick = onAddCar,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        ) {
            Text("+")
        }
    }
}
