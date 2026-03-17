package fi.project.petcare.model.repository

import fi.project.petcare.model.data.Reminder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class ReminderRepository(private val client: SupabaseClient) {

    suspend fun getReminders(userId: String): Result<List<Reminder>> {
        return try {
            val reminders = client.from("reminders").select {
                filter {
                    Reminder::ownerId eq userId
                }
            }.decodeList<Reminder>()
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertReminder(reminder: Reminder): Result<Unit> {
        return try {
            client.from("reminders").upsert(reminder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            client.from("reminders").delete {
                filter {
                    Reminder::id eq reminderId
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
