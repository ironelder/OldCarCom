package com.lab.opengarage.ui.garage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.InfiniteScrollEffect
import com.lab.opengarage.ui.common.LoadingFooter
import com.lab.opengarage.ui.common.OverflowMenu
import com.lab.opengarage.ui.common.RecordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    onBack: () -> Unit,
    onEditCar: (String) -> Unit,
    onAddRecord: (String) -> Unit,
    onRecordClick: (String) -> Unit,
    vm: CarDetailViewModel = hiltViewModel(),
) {
    val car by vm.car.collectAsStateWithLifecycle()
    val deleted by vm.deleted.collectAsStateWithLifecycle()
    val records by vm.paginator.items.collectAsStateWithLifecycle()
    val loading by vm.paginator.loading.collectAsStateWithLifecycle()
    val initialized by vm.paginator.initialized.collectAsStateWithLifecycle()
    val endReached by vm.paginator.endReached.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var confirmDelete by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    InfiniteScrollEffect(listState) { vm.loadMore() }
    LaunchedEffect(deleted) { if (deleted) onBack() }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("차량 삭제") },
            text = { Text("이 차량을 삭제할까요? (기록은 남습니다)") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.deleteCar() }) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("취소") } },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(car?.nickname?.ifBlank { "${car?.make} ${car?.model}" } ?: "차 상세") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    car?.let { c -> OverflowMenu(onEdit = { onEditCar(c.carId) }, onDelete = { confirmDelete = true }) }
                },
            )
        },
        floatingActionButton = {
            car?.let { c ->
                FloatingActionButton(onClick = { onAddRecord(c.carId) }) {
                    Icon(Icons.Filled.Add, contentDescription = "기록 추가")
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            car?.let { c ->
                Text(
                    text = "${c.make} ${c.model}" + if (c.year > 0) " · ${c.year}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (initialized && records.isEmpty()) {
                Text(
                    "기록이 없어요. + 로 정비/주유를 남겨보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(records, key = { it.recordId }) { rec ->
                        RecordCard(rec) { onRecordClick(rec.recordId) }
                    }
                    if (loading && !endReached) item { LoadingFooter() }
                }
            }
        }
    }
}
