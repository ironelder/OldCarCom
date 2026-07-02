package com.lab.opengarage.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.model.RecordType
import com.lab.opengarage.ui.common.OverflowMenu
import com.lab.opengarage.ui.common.PhotoPager
import com.lab.opengarage.ui.common.UiState
import com.lab.opengarage.ui.common.formatDate
import com.lab.opengarage.ui.common.formatWon
import com.lab.opengarage.ui.common.fuelLabel

@OptIn(ExperimentalMaterial3Api::class)
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

    val s = state
    val mine = s is UiState.Success && s.data.ownerUid == currentUid && currentUid != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("기록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (mine && s is UiState.Success) {
                        OverflowMenu(
                            onEdit = { onEdit(s.data.recordId, s.data.carId) },
                            onDelete = { confirmDelete = true },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            when (s) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.padding(16.dp))
                is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                is UiState.Success -> {
                    val r = s.data
                    PhotoPager(r.photoUrls, Modifier.fillMaxWidth())
                    Column(Modifier.padding(16.dp)) {
                        val tag = if (r.type == RecordType.FUEL) "[주유]" else "[정비]"
                        Text("$tag ${r.title}", style = MaterialTheme.typography.titleLarge)
                        if (r.years.isNotBlank()) {
                            Text(
                                "${r.make} · ${r.years}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Text(
                            "${r.ownerNickname.ifBlank { "오너" }} · ${"%,d".format(r.mileageKm)}km · ${formatDate(r.date)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (r.cost > 0) {
                            Text(
                                formatWon(r.cost),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
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
}
