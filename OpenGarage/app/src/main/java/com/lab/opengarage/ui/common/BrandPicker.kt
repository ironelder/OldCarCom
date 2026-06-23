package com.lab.opengarage.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lab.opengarage.model.CarBrands

/**
 * 제조사 선택 드롭다운. 자유 입력 대신 [CarBrands.carBrands] 에서 선택만 가능.
 * 입력칸 글자로 목록을 필터링하되, 최종 값은 목록 항목 선택으로만 확정된다.
 *
 * @param selected 현재 선택된 정규 브랜드명("" 이면 미선택)
 * @param onSelected 목록에서 항목을 고르면 호출
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandPicker(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        if (query.isBlank()) CarBrands.carBrands
        else CarBrands.carBrands.filter { it.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = if (expanded) query else selected,
            onValueChange = { query = it; expanded = true },
            readOnly = false,
            label = { Text("제조사") },
            placeholder = { Text("브랜드 선택") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp),
        ) {
            filtered.forEach { brand ->
                DropdownMenuItem(
                    text = { Text(brand) },
                    onClick = {
                        onSelected(brand)
                        query = ""
                        expanded = false
                    },
                )
            }
        }
    }
}
