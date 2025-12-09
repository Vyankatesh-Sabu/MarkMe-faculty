package com.vrsabu.markme

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.vrsabu.markme.data.repository.AuthRepository
import com.vrsabu.markme.navigation.Screen
import com.vrsabu.markme.ui.screens.home.MarkMeHomeScreen
import com.vrsabu.markme.ui.screens.login.AuthViewModel
import com.vrsabu.markme.ui.screens.login.LoginPage
import com.vrsabu.markme.ui.screens.profile.ProfileScreen
import com.vrsabu.markme.ui.screens.schedule.ScheduleScreen
import com.vrsabu.markme.ui.theme.MarkMeTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrsabu.markme.ui.screens.home.HomeViewModel
import com.vrsabu.markme.ui.screens.attendanceScreen.AttendanceScreen
import com.vrsabu.markme.ui.screens.attendanceScreen.StudentStatisticsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            // Get the AuthRepository from the Application instance

            val app = application as MarkMeApp
            MarkMeTheme {
                AppNavHost(navController = navController, app)
            }
        }
    }
}

/**
 * Simple ViewModelProvider.Factory to construct AuthViewModel with a custom repository.
 */
class AuthViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory for HomeViewModel so we don't construct it directly inside a Composable
class HomeViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AppNavHost(navController: NavHostController, app: MarkMeApp) {

    val authRepo: AuthRepository = app.authRepository

    // Create a ViewModelFactory that provides the AuthRepository
    val factory = AuthViewModelFactory(authRepo)
    // Obtain the ViewModel using Compose's viewModel(...) which integrates with lifecycle
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    // Obtain HomeViewModel via a factory instead of constructing it directly
    val homeFactory = HomeViewModelFactory(authRepo)
    val homeViewModel: HomeViewModel = viewModel(factory = homeFactory)

    NavHost(navController = navController, startDestination = if(authViewModel.isLoggedIn()) Screen.Home.route else Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginPage(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.Home.route) {
            MarkMeHomeScreen(navController, homeViewModel)
        }

        // Attendance route with courseId argument (Long)
        composable(
            route = "attendance/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseIdLong = backStackEntry.arguments?.getLong("courseId") ?: 0L
            AttendanceScreen(courseId = courseIdLong, navController = navController)
        }

        composable(
            route = "attendance/{courseId}/students",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseIdLong = backStackEntry.arguments?.getLong("courseId") ?: 0L
            StudentStatisticsScreen(courseId = courseIdLong, navController = navController)
        }

        composable(
            route = "attendance/{courseId}/take",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseIdLong = backStackEntry.arguments?.getLong("courseId") ?: 0L
            com.vrsabu.markme.ui.screens.attendanceSelection.AttendanceSelectionScreen(navController = navController, courseId = courseIdLong, authRepo)
        }

        composable(
            route = "attendance/session/{courseId}/{dateIso}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("dateIso") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseIdLong = backStackEntry.arguments?.getLong("courseId") ?: 0L
            val dateIso = backStackEntry.arguments?.getString("dateIso") ?: ""
            com.vrsabu.markme.ui.screens.attendanceScreen.StudentListScreen(courseId = courseIdLong, dateIso = dateIso, navController = navController)
        }

         composable(Screen.Profile.route) {
             ProfileScreen(onLogout = {
                 // Ensure ViewModel and repository clear auth data first
                 try {
                     authViewModel.logout()
                 } catch (_: Exception) {
                     // ignore
                 }

                 // Clear back stack and navigate to login
                 navController.navigate(Screen.Login.route) {
                     popUpTo(Screen.Home.route) { inclusive = true }
                 }
             })
         }

        composable(Screen.Schedule.route) {
            ScheduleScreen(navController = navController)
        }


     }
 }

@Composable
fun AskNotificationPermission() {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

