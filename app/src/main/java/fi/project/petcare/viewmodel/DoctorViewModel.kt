package fi.project.petcare.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fi.project.petcare.model.data.AssignedPet
import fi.project.petcare.model.data.Doctor
import fi.project.petcare.model.data.PetVitals
import fi.project.petcare.model.data.SupabaseClientFactory
import fi.project.petcare.model.repository.DoctorRepository
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DoctorUiState {
    data object Loading : DoctorUiState
    data class DoctorAuthenticated(val doctor: Doctor) : DoctorUiState
    data object NotADoctor : DoctorUiState
    data class Error(val message: String) : DoctorUiState
}

sealed interface PatientsUiState {
    data object Loading : PatientsUiState
    data class Success(val patients: List<AssignedPet>) : PatientsUiState
    data class Error(val message: String) : PatientsUiState
}

sealed interface VitalsUiState {
    data object Loading : VitalsUiState
    data class Success(val vitals: List<PetVitals>) : VitalsUiState
    data class Error(val message: String) : VitalsUiState
}

class DoctorViewModel : ViewModel() {
    private val client = SupabaseClientFactory.getInstance()
    private val repository = DoctorRepository(client)

    private val _doctorUiState = MutableStateFlow<DoctorUiState>(DoctorUiState.Loading)
    val doctorUiState: StateFlow<DoctorUiState> = _doctorUiState

    private val _patientsUiState = MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val patientsUiState: StateFlow<PatientsUiState> = _patientsUiState

    private val _vitalsUiState = MutableStateFlow<VitalsUiState>(VitalsUiState.Loading)
    val vitalsUiState: StateFlow<VitalsUiState> = _vitalsUiState

    init {
        checkDoctorRole()
    }

    /**
     * After authentication, check if the logged-in user has a doctor profile.
     * Sets state to DoctorAuthenticated or NotADoctor accordingly.
     */
    fun checkDoctorRole() {
        viewModelScope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val userId = status.session.user?.id.toString()
                        _doctorUiState.value = DoctorUiState.Loading
                        repository.getDoctorByUserId(userId).fold(
                            onSuccess = { doctor ->
                                if (doctor != null) {
                                    _doctorUiState.value = DoctorUiState.DoctorAuthenticated(doctor)
                                    loadPatients(doctor.id!!)
                                } else {
                                    _doctorUiState.value = DoctorUiState.NotADoctor
                                }
                            },
                            onFailure = { e ->
                                Log.e("DoctorViewModel", "Role check failed", e)
                                _doctorUiState.value = DoctorUiState.Error(
                                    e.message ?: "Could not verify doctor role."
                                )
                            }
                        )
                    }
                    else -> {
                        _doctorUiState.value = DoctorUiState.NotADoctor
                    }
                }
            }
        }
    }

    /**
     * Loads all pets assigned to the doctor.
     */
    fun loadPatients(doctorId: String) {
        viewModelScope.launch {
            _patientsUiState.value = PatientsUiState.Loading
            repository.getAssignedPets(doctorId).fold(
                onSuccess = { patients ->
                    _patientsUiState.value = PatientsUiState.Success(patients)
                },
                onFailure = { e ->
                    Log.e("DoctorViewModel", "Failed to load patients", e)
                    _patientsUiState.value = PatientsUiState.Error(
                        e.message ?: "Could not load patient list."
                    )
                }
            )
        }
    }

    /**
     * Loads vitals history for a specific pet.
     */
    fun loadVitals(petId: String) {
        viewModelScope.launch {
            _vitalsUiState.value = VitalsUiState.Loading
            repository.getPetVitals(petId).fold(
                onSuccess = { vitals ->
                    _vitalsUiState.value = VitalsUiState.Success(vitals)
                },
                onFailure = { e ->
                    Log.e("DoctorViewModel", "Failed to load vitals for pet $petId", e)
                    _vitalsUiState.value = VitalsUiState.Error(
                        e.message ?: "Could not load vitals."
                    )
                }
            )
        }
    }
}
