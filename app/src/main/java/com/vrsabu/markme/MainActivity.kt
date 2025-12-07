package com.vrsabu.markme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vrsabu.markme.data.repository.AuthRepository
import com.vrsabu.markme.navigation.Screen
import com.vrsabu.markme.ui.screens.home.MarkMeHomeScreen
import com.vrsabu.markme.ui.screens.login.AuthViewModel
import com.vrsabu.markme.ui.screens.login.LoginPage
import com.vrsabu.markme.ui.screens.profile.ProfileScreen
import com.vrsabu.markme.ui.theme.MarkMeTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vrsabu.markme.ui.screens.home.HomeViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
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

        composable(Screen.Profile.route) {
            ProfileScreen(onLogout = {
                // Clear back stack and navigate to login
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            })
        }
    }
}
