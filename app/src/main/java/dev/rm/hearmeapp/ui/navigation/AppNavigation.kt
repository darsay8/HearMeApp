package dev.rm.hearmeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.rm.hearmeapp.ui.screens.HomeScreen
import dev.rm.hearmeapp.ui.screens.LoginScreen
import dev.rm.hearmeapp.ui.screens.PasswordRecoveryScreen
import dev.rm.hearmeapp.ui.screens.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login"){
        composable("home") { HomeScreen(navController = navController) }
        composable("login") { LoginScreen(navController = navController) }
        composable("register") { RegisterScreen(navController = navController) }
        composable("password_recovery") { PasswordRecoveryScreen(navController = navController)  }
    }
}