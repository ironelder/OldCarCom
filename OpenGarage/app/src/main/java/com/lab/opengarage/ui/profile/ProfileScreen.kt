package com.lab.opengarage.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.RecordCard

@Composable
fun ProfileScreen(
    onRecordClick: (String) -> Unit,
    vm: ProfileViewModel = hiltViewModel(),
) {
    val user by vm.user.collectAsStateWithLifecycle()
    val myRecords by vm.myRecords.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Text(
            text = user?.nickname?.ifBlank { "오너" } ?: "오너",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        OutlinedButton(
            onClick = { vm.signOut() },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Text("로그아웃")
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Text(
            "내 공개 정비기록",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(myRecords, key = { it.recordId }) { rec ->
                RecordCard(rec) { onRecordClick(rec.recordId) }
            }
        }
    }
}
