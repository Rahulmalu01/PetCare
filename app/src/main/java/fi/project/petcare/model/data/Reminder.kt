package fi.project.petcare.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reminder(
    val id: String? = null,
    @SerialName("pet_id") val petId: String,
    val type: String, // e.g., "Feeding", "Grooming", "Medication", "Exercise"
    val title: String,
    val description: String? = null,
    @SerialName("reminder_time") val reminderTime: String, // ISO 8601 string
    @SerialName("is_enabled") val isEnabled: Boolean = true,
    @SerialName("repeat_pattern") val repeatPattern: String? = null, // e.g., "Daily", "Weekly", "None"
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("owner_id") val ownerId: String
)
