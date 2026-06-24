package com.lab.opengarage.ui.garage

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.BrandPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarEditScreen(
    onDone: () -> Unit,
    vm: CarEditViewModel = hiltViewModel(),
) {
    val saved by vm.saved.collectAsStateWithLifecycle()
    val initial by vm.initial.collectAsStateWithLifecycle()
    LaunchedEffect(saved) { if (saved) onDone() }

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    LaunchedEffect(initial) {
        initial?.let {
            make = it.make
            model = it.model
            year = if (it.year > 0) it.year.toString() else ""
            nickname = it.nickname
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (vm.editing) "차 수정" else "차 추가") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
        ) {
            BrandPicker(selected = make, onSelected = { make = it }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(model, { model = it }, label = { Text("모델 (예: 프라이드)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            OutlinedTextField(
                year, { year = it.filter(Char::isDigit) },
                label = { Text("연식 (예: 2005)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(nickname, { nickname = it }, label = { Text("애칭 (예: 은마)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Button(
                onClick = { vm.save(make.trim(), model.trim(), year.toIntOrNull() ?: 0, nickname.trim()) },
                enabled = make.isNotBlank() && model.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("저장")
            }
        }
    }
}
