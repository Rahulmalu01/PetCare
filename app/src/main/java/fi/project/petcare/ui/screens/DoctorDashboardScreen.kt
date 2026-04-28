package fi.project.petcare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fi.project.petcare.model.data.AssignedPet
import fi.project.petcare.model.data.Doctor
import fi.project.petcare.ui.nav.Screen
import fi.project.petcare.viewmodel.DoctorUiState
import fi.project.petcare.viewmodel.DoctorViewModel
import fi.project.petcare.viewmodel.PatientsUiState

@Composable
fun DoctorDashboardScreen(
    doctorViewModel: DoctorViewModel,
    navController: NavController
) {
    val doctorState by doctorViewModel.doctorUiState.collectAsState()
    val patientsState by doctorViewModel.patientsUiState.collectAsState()

    when (doctorState) {
        is DoctorUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DoctorUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = (doctorState as DoctorUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is DoctorUiState.NotADoctor -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your account is not registered as a veterinarian.")
            }
        }
        is DoctorUiState.DoctorAuthenticated -> {
            val doctor = (doctorState as DoctorUiState.DoctorAuthenticated).doctor
            DoctorDashboardContent(
                doctor = doctor,
                patientsState = patientsState,
                onPatientClick = { petId ->
                    navController.navigate(Screen.DoctorPatientDetail.route + "/$petId")
                }
            )
        }
    }
}

@Composable
private fun DoctorDashboardContent(
    doctor: Doctor,
    patientsState: PatientsUiState,
    onPatientClick: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // ── Welcome card ──────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            DoctorWelcomeCard(doctor = doctor)
        }

        // ── Stats row ─────────────────────────────────────────────────────────
        item {
            val count = if (patientsState is PatientsUiState.Success)
                patientsState.patients.size else 0
            DoctorStatsRow(patientCount = count)
        }

        // ── Patients header ───────────────────────────────────────────────────
        item {
            Text(
                text = "My Patients",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ── Patient list ──────────────────────────────────────────────────────
        when (patientsState) {
            is PatientsUiState.Loading -> {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is PatientsUiState.Error -> {
                item {
                    Text(
                        text = patientsState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is PatientsUiState.Success -> {
                if (patientsState.patients.isEmpty()) {
                    item {
                        Text(
                            text = "No patients assigned yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(patientsState.patients) { assignment ->
                        PatientCard(
                            assignment = assignment,
                            onClick = { assignment.petId.let(onPatientClick) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DoctorWelcomeCard(doctor: Doctor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Dr. ${doctor.fullName.removePrefix("Dr. ")}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                doctor.specialization?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                doctor.clinicName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%.1f", doctor.rating),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorStatsRow(patientCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatCard(
            label = "Total Patients",
            value = "$patientCount",
            icon = { Icon(Icons.Outlined.Pets, contentDescription = null, modifier = Modifier.size(28.dp)) },
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Specialization",
            value = "Vet",
            icon = { Icon(Icons.Outlined.Healing, contentDescription = null, modifier = Modifier.size(28.dp)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            icon()
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientCard(
    assignment: AssignedPet,
    onClick: () -> Unit
) {
    val pet = assignment.pets
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet?.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${pet?.breed ?: "–"} · ${pet?.gender ?: "–"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(pet?.ageMonths ?: 0) / 12}y ${(pet?.ageMonths ?: 0) % 12}m · ${pet?.weight ?: "–"} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                assignment.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
