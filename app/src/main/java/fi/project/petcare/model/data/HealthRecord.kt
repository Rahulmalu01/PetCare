package fi.project.petcare.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class HealthRecord(
    val type: HealthRecordType,
    val date: String, // ISO string for DB compatibility
    val details: String,
    val id: String? = null,
    @SerialName("pet_id") val petId: String? = null
)

data class HealthRecordState(
    var type: HealthRecordType = HealthRecordType.OPERATION,
    var date: Date = Date(),
    var details: String = "",
    var operation: String = "",
    var veterinarianvisit: String = "",
    var medication: String = "",
    var symptom: String = "",
    var allergy: String = "",
    var exercise: String = "",
    var weight: String = ""
)

enum class HealthRecordType {
    OPERATION,
    VETERINARIAN_VISIT,
    MEDICATION,
    SYMPTOM,
    ALLERGY,
    EXERCISE,
    WEIGHT_MEASUREMENT
}
