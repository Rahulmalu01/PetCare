package fi.project.petcare.ui.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fi.project.petcare.model.data.User
import fi.project.petcare.ui.composables.Dashboard
import fi.project.petcare.ui.screens.HomeScreen
import fi.project.petcare.ui.screens.PetListScreen
import fi.project.petcare.ui.screens.ProfileScreen
import fi.project.petcare.ui.screens.SettingsScreen
import fi.project.petcare.viewmodel.AuthUiState
import fi.project.petcare.viewmodel.AuthViewModel
import fi.project.petcare.viewmodel.PetViewModel
import fi.project.petcare.viewmodel.ReminderViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    petViewModel: PetViewModel,
    reminderViewModel: ReminderViewModel
) {
    val userState by authViewModel.authUiState.collectAsState()
    val petState by petViewModel.petUiState.collectAsState()
    val user =
        if (userState is AuthUiState.Authenticated)
            (userState as AuthUiState.Authenticated).user
        else User(id = "demo-user", name = "John Doe", email = "johndoe@email.com")

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.Home.route
    ) {
        composable(Screen.Dashboard.Home.route) {
            Dashboard(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                navController = navController,
            ) {
                HomeScreen(
                    user = user
                )
            }
        }
        composable(Screen.Dashboard.Pets.route) {
            var showModal by remember { mutableStateOf(false) }
            val toggleShowModal = { showModal = !showModal }
            Dashboard(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                navController = navController,
                onAddPetClick = { toggleShowModal() }
            ) {
                PetListScreen(
                    petState = petState,
                    petViewModel = petViewModel,
                    toggleShowModal = toggleShowModal,
                    showModal = showModal,
                    userId = user.id
                )
            }
        }
        composable(Screen.PetProfile.route) {
            ProfileScreen(petName = "Fluffy", navController = navController)
        }
        composable(Screen.Dashboard.Community.route) {
            val reminderState by reminderViewModel.uiState.collectAsState()
            Dashboard(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                navController = navController
            ) {
                Column {
                    Text(
                        text = "Reminders & Community",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "This section is coming soon! You will be able to manage daily care reminders and connect with the pet owner community here.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        composable(
            Screen.Settings.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }

        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    authViewModel.onSignOut()
                    navController.navigate(Screen.Dashboard.Home.route) {
                        popUpTo(Screen.Dashboard.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
