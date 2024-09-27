package dev.rm.hearmeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.rm.hearmeapp.ui.screens.HomeScreen
import dev.rm.hearmeapp.ui.screens.LocationScreen
import dev.rm.hearmeapp.ui.screens.LoginScreen
import dev.rm.hearmeapp.ui.screens.PasswordRecoveryScreen
import dev.rm.hearmeapp.ui.screens.RegisterScreen
import dev.rm.hearmeapp.vm.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("home") {
            HomeScreen(navController, authViewModel)
        }
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("password_recovery") {
            PasswordRecoveryScreen(navController, authViewModel)
        }

        composable("location") {
            LocationScreen(navController, authViewModel)
        }
    })
}