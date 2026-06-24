package com.lab.opengarage.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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

fun formatDate(epoch: Long): String = if (epoch <= 0L) "-" else dateFmt.format(Date(epoch))
fun formatWon(amount: Long): String = "%,d원".format(amount)
fun fuelLabel(type: FuelType?): String = when (type) {
    FuelType.GASOLINE -> "휘발유"
    FuelType.DIESEL -> "경유"
    FuelType.LPG -> "LPG"
    null -> "-"
}

@Composable
private fun Thumb(photoUrl: String?, placeholder: ImageVector) {
    val shape = RoundedCornerShape(14.dp)
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(72.dp).clip(shape),
        )
    } else {
        Box(
            Modifier
                .size(72.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                placeholder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TypeBadge(type: RecordType) {
    val (label, container, onContainer) = if (type == RecordType.FUEL) {
        Triple("주유", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    } else {
        Triple("정비", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Surface(color = container, shape = RoundedCornerShape(8.dp)) {
        Text(
            label,
            color = onContainer,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun CarCard(car: Car, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Thumb(car.photoUrl, Icons.Filled.DirectionsCar)
            Column(Modifier.align(Alignment.CenterVertically)) {
                Text(
                    car.nickname.ifBlank { "${car.make} ${car.model}" },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "${car.make} ${car.model}" + if (car.year > 0) " · ${car.year}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun RecordCard(record: Record, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val placeholder = if (record.type == RecordType.FUEL) Icons.Filled.LocalGasStation else Icons.Filled.Build
            Thumb(record.photoUrls.firstOrNull(), placeholder)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TypeBadge(record.type)
                    Text(
                        record.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    "${record.ownerNickname.ifBlank { "오너" }} · ${"%,d".format(record.mileageKm)}km · ${formatDate(record.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (record.cost > 0) {
                    Text(
                        formatWon(record.cost),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
