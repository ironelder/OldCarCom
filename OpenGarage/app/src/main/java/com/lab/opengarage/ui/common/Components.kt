package com.lab.opengarage.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lab.opengarage.model.Car
import com.lab.opengarage.model.FuelType
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.RecordType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)

fun formatDate(epoch: Long): String =
    if (epoch <= 0L) "-" else dateFmt.format(Date(epoch))

fun formatWon(amount: Long): String =
    "%,d원".format(amount)

fun fuelLabel(type: FuelType?): String = when (type) {
    FuelType.GASOLINE -> "휘발유"
    FuelType.DIESEL -> "경유"
    FuelType.LPG -> "LPG"
    null -> "-"
}

@Composable
fun CarCard(car: Car, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (car.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = car.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
            }
            Column {
                Text(
                    text = car.nickname.ifBlank { "${car.make} ${car.model}" },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${car.make} ${car.model} · ${car.year}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun RecordCard(record: Record, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val thumb = record.photoUrls.firstOrNull()
            if (thumb != null) {
                AsyncImage(model = thumb, contentDescription = null, modifier = Modifier.size(64.dp))
            }
            Column(Modifier.weight(1f)) {
                val tag = if (record.type == RecordType.FUEL) "[주유]" else "[정비]"
                Text(
                    text = "$tag ${record.title}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${record.ownerNickname.ifBlank { "오너" }} · ${"%,d".format(record.mileageKm)}km · ${formatDate(record.date)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (record.cost > 0) {
                    Text(text = formatWon(record.cost), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
