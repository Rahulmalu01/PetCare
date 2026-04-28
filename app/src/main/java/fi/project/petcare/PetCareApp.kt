package fi.project.petcare

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import fi.project.petcare.ui.composables.LoadingIndicator
import fi.project.petcare.ui.nav.NavGraph
import fi.project.petcare.ui.nav.Screen
import fi.project.petcare.ui.screens.WelcomeScreen
import fi.project.petcare.ui.theme.PetCareTheme
import fi.project.petcare.ui.theme.bg_gr
import fi.project.petcare.viewmodel.AuthUiState
import fi.project.petcare.viewmodel.AuthViewModel
import fi.project.petcare.viewmodel.DoctorUiState
import fi.project.petcare.viewmodel.DoctorViewModel
import fi.project.petcare.viewmodel.PetViewModel
import fi.project.petcare.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PetCareApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authUiState.collectAsState()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    PetCareTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (authState) {
                is AuthUiState.Unauthenticated -> {
                    WelcomeScreen(vModel = authViewModel, snackbarHostState = snackbarHostState)
                }

                is AuthUiState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier.aspectRatio(1f),
                        color = bg_gr
                    )
                }

                is AuthUiState.Authenticated -> {
                    val petViewModel: PetViewModel = viewModel()
                    val reminderViewModel: ReminderViewModel = viewModel()
                    val doctorViewModel: DoctorViewModel = viewModel()
                    val doctorState by doctorViewModel.doctorUiState.collectAsState()

                    // Once doctor role is resolved, route accordingly
                    LaunchedEffect(doctorState) {
                        when (doctorState) {
                            is DoctorUiState.DoctorAuthenticated -> {
                                navController.navigate(Screen.DoctorDashboard.route) {
                                    popUpTo(Screen.Dashboard.Home.route) { inclusive = true }
                                }
                            }
                            // NotADoctor or Error → stay on default owner dashboard
                            else -> Unit
                        }
                    }

                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        petViewModel = petViewModel,
                        reminderViewModel = reminderViewModel,
                        doctorViewModel = doctorViewModel
                    )
                }

                is AuthUiState.Error -> {
                    val errorMessage = (authState as AuthUiState.Error).message
                    scope.launch {
                        snackbarHostState.showSnackbar(message = errorMessage)
                    }
                    WelcomeScreen(vModel = authViewModel, snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}
