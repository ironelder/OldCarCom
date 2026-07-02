package com.lab.opengarage.ui.garage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

@OptIn(ExperimentalMaterial3Api::class)
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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    InfiniteScrollEffect(listState) { vm.loadMore() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("내 차고") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCar,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("차 추가") },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !initialized -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                cars.isEmpty() -> Text(
                    "아직 등록한 차가 없어요.\n+ 로 첫 차를 추가하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(cars, key = { it.carId }) { car ->
                        CarCard(car) { onCarClick(car.carId) }
                    }
                    if (loading && !endReached) item { LoadingFooter() }
                }
            }
        }
    }
}
