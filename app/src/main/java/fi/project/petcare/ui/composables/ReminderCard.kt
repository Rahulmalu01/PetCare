package fi.project.petcare.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.project.petcare.model.data.Reminder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun reminderIcon(type: String): ImageVector = when (type.lowercase()) {
    "feeding" -> Icons.Outlined.Restaurant
    "medication" -> Icons.Outlined.LocalHospital
    "exercise" -> Icons.Outlined.FitnessCenter
    "grooming" -> Icons.Outlined.Spa
    else -> Icons.Outlined.Alarm
}

private fun formatReminderTime(isoString: String): String {
    return try {
        val formatter = DateTimeFormatter
            .ofPattern("MMM d, HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.parse(isoString))
    } catch (e: Exception) {
        isoString
    }
}

@Composable
fun ReminderCard(reminder: Reminder, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = reminderIcon(reminder.type),
                contentDescription = reminder.type,
                modifier = Modifier.size(36.dp),
                tint = if (reminder.isEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                reminder.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = formatReminderTime(reminder.reminderTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    reminder.repeatPattern?.let {
                        Text(
                            text = "· $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = "Pet",
                modifier = Modifier.size(20.dp),
                tint = if (reminder.isEnabled)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}
