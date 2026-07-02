package com.lab.opengarage.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.BrandPicker
import com.lab.opengarage.ui.common.InfiniteScrollEffect
import com.lab.opengarage.ui.common.LoadingFooter
import com.lab.opengarage.ui.common.RecordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onRecordClick: (String) -> Unit,
    vm: FeedViewModel = hiltViewModel(),
) {
    val make by vm.make.collectAsStateWithLifecycle()
    val model by vm.model.collectAsStateWithLifecycle()
    val records by vm.paginator.items.collectAsStateWithLifecycle()
    val loading by vm.paginator.loading.collectAsStateWithLifecycle()
    val initialized by vm.paginator.initialized.collectAsStateWithLifecycle()
    val endReached by vm.paginator.endReached.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    InfiniteScrollEffect(listState) { vm.loadMore() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("차종 정비노트") }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            BrandPicker(
                selected = make,
                onSelected = { vm.setMake(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            OutlinedTextField(
                value = model,
                onValueChange = { vm.setModel(it) },
                label = { Text("모델 (선택 — 비우면 제조사 전체)") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Button(
                onClick = { vm.search() },
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            ) {
                Text(
                    when {
                        make.isBlank() -> "전체 기록 보기"
                        model.isBlank() -> "이 제조사 전체 보기"
                        else -> "이 차종 기록 보기"
                    },
                )
            }

            Box(Modifier.fillMaxSize()) {
                when {
                    !initialized -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    records.isEmpty() -> Text(
                        "기록이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    )
                    else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(records, key = { it.recordId }) { rec ->
                            RecordCard(rec) { onRecordClick(rec.recordId) }
                        }
                        if (loading && !endReached) item { LoadingFooter() }
                    }
                }
            }
        }
    }
}
