package fi.project.petcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fi.project.petcare.model.data.PetResponse
import fi.project.petcare.model.data.PetVitals
import fi.project.petcare.viewmodel.DoctorViewModel
import fi.project.petcare.viewmodel.PatientsUiState
import fi.project.petcare.viewmodel.VitalsUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientDetailScreen(
    petId: String,
    doctorViewModel: DoctorViewModel,
    navController: NavController
) {
    val patientsState by doctorViewModel.patientsUiState.collectAsState()
    val vitalsState by doctorViewModel.vitalsUiState.collectAsState()

    val assignment = if (patientsState is PatientsUiState.Success)
        (patientsState as PatientsUiState.Success).patients.find { it.petId == petId }
    else null
    val pet = assignment?.pets

    LaunchedEffect(petId) { doctorViewModel.loadVitals(petId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "Patient Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item { pet?.let { PetInfoCard(it) } ?: Text("Pet information not available.") }

            item {
                Text(
                    "Live Vitals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            when (vitalsState) {
                is VitalsUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VitalsUiState.Error -> item {
                    Text((vitalsState as VitalsUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                is VitalsUiState.Success -> {
                    val vitals = (vitalsState as VitalsUiState.Success).vitals
                    if (vitals.isEmpty()) {
                        item { Text("No vitals recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        item { LatestVitalsSummary(vitals.first()) }
                        item {
                            Text(
                                "History (${vitals.size} readings)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(vitals) { entry -> VitalsHistoryRow(entry) }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PetInfoCard(pet: PetResponse.Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(pet.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            InfoRow("Species", pet.species)
            InfoRow("Breed", pet.breed)
            InfoRow("Gender", pet.gender)
            InfoRow("Weight", "${pet.weight} kg")
            InfoRow("Age", "${pet.ageMonths / 12}y ${pet.ageMonths % 12}m")
            pet.notes?.let { InfoRow("Notes", it) }
            InfoRow("Microchip ID", pet.microchipId.toString())
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LatestVitalsSummary(vitals: PetVitals) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Latest Reading", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                vitals.temperature?.let { VitalChip(Icons.Outlined.Thermostat, "${it}°C", "Temp") }
                vitals.heartRate?.let { VitalChip(Icons.Outlined.FavoriteBorder, "$it bpm", "Heart Rate") }
                vitals.respiratoryRate?.let { VitalChip(Icons.Outlined.Air, "$it /min", "Resp.") }
                vitals.weight?.let { VitalChip(Icons.Outlined.Scale, "$it kg", "Weight") }
            }
        }
    }
}

@Composable
private fun VitalChip(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, label, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun VitalsHistoryRow(vitals: PetVitals) {
    val formattedTime = try {
        val fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())
        fmt.format(Instant.parse(vitals.recordedAt ?: ""))
    } catch (e: Exception) { vitals.recordedAt ?: "–" }

    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formattedTime, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                vitals.temperature?.let { Text("${it}°C", style = MaterialTheme.typography.bodySmall) }
                vitals.heartRate?.let { Text("$it bpm", style = MaterialTheme.typography.bodySmall) }
                vitals.respiratoryRate?.let { Text("$it/min", style = MaterialTheme.typography.bodySmall) }
                vitals.weight?.let { Text("${it}kg", style = MaterialTheme.typography.bodySmall) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
