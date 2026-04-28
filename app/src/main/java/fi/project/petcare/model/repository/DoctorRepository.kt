package fi.project.petcare.model.repository

import fi.project.petcare.model.data.AssignedPet
import fi.project.petcare.model.data.Doctor
import fi.project.petcare.model.data.PetVitals
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class DoctorRepository(private val client: SupabaseClient) {

    /**
     * Returns the Doctor profile linked to the given auth user ID.
     * Returns null if the user is not a registered doctor.
     */
    suspend fun getDoctorByUserId(userId: String): Result<Doctor?> {
        return try {
            val doctor = client.from("doctors").select {
                filter {
                    Doctor::userId eq userId
                }
            }.decodeList<Doctor>().firstOrNull()
            Result.success(doctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns all pets assigned to the given doctor, with pet details embedded.
     */
    suspend fun getAssignedPets(doctorId: String): Result<List<AssignedPet>> {
        return try {
            val assignments = client.from("pet_doctor_assignments").select(
                columns = io.github.jan.supabase.postgrest.query.Columns.raw("pet_id, doctor_id, notes, pets(*)")
            ) {
                filter {
                    AssignedPet::doctorId eq doctorId
                }
            }.decodeList<AssignedPet>()
            Result.success(assignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns the most recent vitals entries for a given pet (newest first).
     */
    suspend fun getPetVitals(petId: String, limit: Int = 20): Result<List<PetVitals>> {
        return try {
            val vitals = client.from("pet_vitals").select {
                filter {
                    PetVitals::petId eq petId
                }
                order("recorded_at", order = Order.DESCENDING)
                this.limit(limit.toLong())
            }.decodeList<PetVitals>()
            Result.success(vitals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all doctors (used for the HomeScreen list).
     */
    suspend fun getAllDoctors(): Result<List<Doctor>> {
        return try {
            val doctors = client.from("doctors").select().decodeList<Doctor>()
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
