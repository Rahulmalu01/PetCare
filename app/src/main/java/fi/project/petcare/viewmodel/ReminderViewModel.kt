package fi.project.petcare.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.project.petcare.model.data.Reminder
import fi.project.petcare.model.data.SupabaseClientFactory
import fi.project.petcare.model.repository.ReminderRepository
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ReminderUiState {
    data object Loading : ReminderUiState
    data class Success(val reminders: List<Reminder>) : ReminderUiState
    data class Error(val message: String) : ReminderUiState
}

class ReminderViewModel : ViewModel() {
    private val client = SupabaseClientFactory.getInstance()
    private val repository = ReminderRepository(client)

    private val _uiState = MutableStateFlow<ReminderUiState>(ReminderUiState.Loading)
    val uiState: StateFlow<ReminderUiState> = _uiState

    init {
        getReminders()
    }

    fun getReminders() {
        viewModelScope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val userId = status.session.user?.id.toString()
                        val result = repository.getReminders(userId)
                        result.fold(
                            onSuccess = { _uiState.value = ReminderUiState.Success(it) },
                            onFailure = { _uiState.value = ReminderUiState.Error(it.message ?: "Unknown error") }
                        )
                    }
                    else -> _uiState.value = ReminderUiState.Error("Please sign in to see reminders")
                }
            }
        }
    }

    fun upsertReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.upsertReminder(reminder).fold(
                onSuccess = { getReminders() },
                onFailure = { Log.e("ReminderViewModel", "Error upserting reminder", it) }
            )
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId).fold(
                onSuccess = { getReminders() },
                onFailure = { Log.e("ReminderViewModel", "Error deleting reminder", it) }
            )
        }
    }
}
