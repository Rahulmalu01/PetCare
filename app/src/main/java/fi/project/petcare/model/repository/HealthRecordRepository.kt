package fi.project.petcare.model.repository

import fi.project.petcare.model.data.HealthRecord
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class HealthRecordRepository(private val client: SupabaseClient) {

    suspend fun getHealthRecords(petId: String): Result<List<HealthRecord>> {
        return try {
            val records = client.from("health_records").select {
                filter {
                    HealthRecord::petId eq petId
                }
            }.decodeList<HealthRecord>()
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addHealthRecord(record: HealthRecord): Result<Unit> {
        return try {
            client.from("health_records").insert(record)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
