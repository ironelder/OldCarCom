package com.lab.opengarage.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.model.RecordType
import com.lab.opengarage.ui.common.PhotoPager
import com.lab.opengarage.ui.common.UiState
import com.lab.opengarage.ui.common.formatDate
import com.lab.opengarage.ui.common.formatWon
import com.lab.opengarage.ui.common.fuelLabel

@Composable
fun RecordDetailScreen(
    onBack: () -> Unit,
    onEdit: (recordId: String, carId: String) -> Unit,
    vm: RecordDetailViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val deleted by vm.deleted.collectAsStateWithLifecycle()
    val currentUid by vm.currentUid.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(deleted) { if (deleted) onBack() }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("기록 삭제") },
            text = { Text("이 기록을 삭제할까요?") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete() }) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("취소") } },
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        val s = state
        val mine = s is UiState.Success && s.data.ownerUid == currentUid && currentUid != null
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("← 뒤로") }
            if (mine && s is UiState.Success) {
                Row {
                    TextButton(onClick = { onEdit(s.data.recordId, s.data.carId) }) { Text("수정") }
                    TextButton(onClick = { confirmDelete = true }) { Text("삭제") }
                }
            }
        }

        when (s) {
            is UiState.Loading -> CircularProgressIndicator(Modifier.padding(16.dp))
            is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            is UiState.Success -> {
                val r = s.data
                PhotoPager(r.photoUrls, Modifier.fillMaxWidth())
                Column(Modifier.padding(16.dp)) {
                    val tag = if (r.type == RecordType.FUEL) "[주유]" else "[정비]"
                    Text("$tag ${r.title}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${r.ownerNickname.ifBlank { "오너" }} · ${"%,d".format(r.mileageKm)}km · ${formatDate(r.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    if (r.cost > 0) Text(formatWon(r.cost), modifier = Modifier.padding(top = 4.dp))
                    if (r.type == RecordType.FUEL) {
                        Text("주유량 ${r.liters ?: 0.0}L · ${fuelLabel(r.fuelType)}", modifier = Modifier.padding(top = 4.dp))
                    }
                    if (r.description.isNotBlank()) {
                        Text(r.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }
    }
}
