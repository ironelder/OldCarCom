package com.lab.opengarage.ui.record

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.model.FuelType
import com.lab.opengarage.model.RecordType
import com.lab.opengarage.ui.common.fuelLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditScreen(
    carId: String,
    onDone: () -> Unit,
    vm: RecordEditViewModel = hiltViewModel(),
) {
    val saved by vm.saved.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()
    val initial by vm.initial.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    var type by remember { mutableStateOf(RecordType.MAINTENANCE) }
    var title by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var liters by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf(FuelType.GASOLINE) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    LaunchedEffect(initial) {
        initial?.let {
            type = it.type
            title = it.title
            mileage = if (it.mileageKm > 0) it.mileageKm.toString() else ""
            cost = if (it.cost > 0) it.cost.toString() else ""
            description = it.description
            isPublic = it.shared
            liters = it.liters?.toString() ?: ""
            fuelType = it.fuelType ?: FuelType.GASOLINE
        }
    }
    val effectiveCarId = initial?.carId?.takeIf { it.isNotBlank() } ?: carId

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5),
    ) { uris -> photoUris = uris }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (vm.editing) "기록 수정" else "기록 작성") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == RecordType.MAINTENANCE,
                        onClick = { type = RecordType.MAINTENANCE },
                        label = { Text("정비") },
                    )
                    FilterChip(
                        selected = type == RecordType.FUEL,
                        onClick = { type = RecordType.FUEL },
                        label = { Text("주유") },
                    )
                }

                OutlinedTextField(title, { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(
                    mileage, { mileage = it.filter(Char::isDigit) }, label = { Text("주행거리(km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                OutlinedTextField(
                    cost, { cost = it.filter(Char::isDigit) }, label = { Text("금액(원)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                OutlinedTextField(description, { description = it }, label = { Text("내용/노하우") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

                if (type == RecordType.FUEL) {
                    OutlinedTextField(
                        liters, { liters = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("주유량(L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FuelType.entries.forEach { ft ->
                            FilterChip(selected = fuelType == ft, onClick = { fuelType = ft }, label = { Text(fuelLabel(ft)) })
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("공개 (차종 피드 노출)", modifier = Modifier.weight(1f))
                        Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                    }
                }

                OutlinedButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(if (photoUris.isEmpty()) "사진 첨부" else "사진 ${photoUris.size}장 선택됨")
                }

                Button(
                    onClick = {
                        vm.save(
                            RecordForm(
                                carId = effectiveCarId,
                                type = type,
                                title = title.trim(),
                                mileageKm = mileage.toIntOrNull() ?: 0,
                                description = description.trim(),
                                cost = cost.toLongOrNull() ?: 0L,
                                liters = if (type == RecordType.FUEL) liters.toDoubleOrNull() else null,
                                fuelType = if (type == RecordType.FUEL) fuelType else null,
                                isPublic = isPublic,
                            ),
                            photoUris,
                        )
                    },
                    enabled = title.isNotBlank() && effectiveCarId.isNotBlank() && !saving,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) {
                    Text("저장")
                }
            }

            if (saving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = true) {},
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
