package com.lab.opengarage.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.model.Car
import com.lab.opengarage.ui.common.RecordCard

@Composable
fun FeedScreen(
    onRecordClick: (String) -> Unit,
    vm: FeedViewModel = hiltViewModel(),
) {
    val records by vm.records.collectAsStateWithLifecycle()
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Text(
            "차종 정비노트",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("제조사") },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("모델") },
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = { vm.setModelKey(Car.makeModelKey(make, model)) },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            enabled = make.isNotBlank() && model.isNotBlank(),
        ) {
            Text("이 차종 기록 보기")
        }

        if (records.isEmpty()) {
            Text(
                "차종을 입력해 정비기록을 찾아보세요.",
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
