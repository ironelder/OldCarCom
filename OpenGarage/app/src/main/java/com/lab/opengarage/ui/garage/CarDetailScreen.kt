package com.lab.opengarage.ui.garage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.RecordCard

@Composable
fun CarDetailScreen(
    onBack: () -> Unit,
    onAddRecord: (String) -> Unit,
    onRecordClick: (String) -> Unit,
    vm: CarDetailViewModel = hiltViewModel(),
) {
    val car by vm.car.collectAsStateWithLifecycle()
    val records by vm.records.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            car?.let { c ->
                FloatingActionButton(onClick = { onAddRecord(c.carId) }) {
                    Text("+")
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TextButton(onClick = onBack) { Text("← 뒤로") }
            car?.let { c ->
                Text(
                    text = c.nickname.ifBlank { "${c.make} ${c.model}" },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    text = "${c.make} ${c.model} · ${c.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            if (records.isEmpty()) {
                Text(
                    "기록이 없어요. + 로 정비/주유를 남겨보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(records, key = { it.recordId }) { rec ->
                        RecordCard(rec) { onRecordClick(rec.recordId) }
                    }
                }
            }
        }
    }
}
