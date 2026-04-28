package fi.project.petcare.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Doctor(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("full_name") val fullName: String,
    val specialization: String? = null,
    @SerialName("clinic_name") val clinicName: String? = null,
    @SerialName("clinic_location") val clinicLocation: String? = null,
    val phone: String? = null,
    @SerialName("license_number") val licenseNumber: String? = null,
    val rating: Double = 0.0,
    @SerialName("img_url") val imgUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PetDoctorAssignment(
    val id: String? = null,
    @SerialName("pet_id") val petId: String,
    @SerialName("doctor_id") val doctorId: String,
    @SerialName("assigned_at") val assignedAt: String? = null,
    val notes: String? = null
)

/**
 * Joined view: assignment + embedded pet info.
 * Used by the doctor to see their patient list.
 */
@Serializable
data class AssignedPet(
    @SerialName("pet_id") val petId: String,
    @SerialName("doctor_id") val doctorId: String,
    val notes: String? = null,
    val pets: PetResponse.Pet? = null
)

@Serializable
data class PetVitals(
    val id: String? = null,
    @SerialName("pet_id") val petId: String? = null,
    val temperature: Double? = null,
    @SerialName("heart_rate") val heartRate: Int? = null,
    @SerialName("respiratory_rate") val respiratoryRate: Int? = null,
    val weight: Double? = null,
    val notes: String? = null,
    @SerialName("recorded_at") val recordedAt: String? = null
)
