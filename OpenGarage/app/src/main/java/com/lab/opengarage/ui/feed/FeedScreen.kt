package com.lab.opengarage.ui.feed

import androidx.compose.foundation.layout.Column
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
import com.lab.opengarage.ui.common.BrandPicker
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
        BrandPicker(
            selected = make,
            onSelected = { make = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("모델 (선택 — 비우면 제조사 전체)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Button(
            onClick = { vm.search(make, model) },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            enabled = make.isNotBlank(),
        ) {
            Text(if (model.isBlank()) "이 제조사 전체 기록 보기" else "이 차종 기록 보기")
        }

        if (records.isEmpty()) {
            Text(
                "제조사를 골라 정비기록을 찾아보세요. (모델은 선택)",
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
