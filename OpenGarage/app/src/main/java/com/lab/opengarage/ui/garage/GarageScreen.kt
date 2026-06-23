package com.lab.opengarage.ui.garage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.opengarage.ui.common.CarCard

@Composable
fun GarageScreen(
    onAddCar: () -> Unit,
    onCarClick: (String) -> Unit,
    vm: GarageViewModel = hiltViewModel(),
) {
    val cars by vm.cars.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        if (cars.isEmpty()) {
            Text(
                "아직 등록한 차가 없어요. + 로 추가하세요.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
            )
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(cars, key = { it.carId }) { car ->
                    CarCard(car) { onCarClick(car.carId) }
                }
            }
        }
        FloatingActionButton(
            onClick = onAddCar,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Text("+")
        }
    }
}
