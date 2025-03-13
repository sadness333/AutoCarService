package com.example.autocarservice.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autocarservice.ui.screens.auth.LoginScreen
import com.example.autocarservice.ui.screens.auth.RegisterScreen
import com.example.autocarservice.ui.screens.client.ClientHomeScreen
import com.example.autocarservice.ui.screens.client.CreateServiceRequestScreen
import com.example.autocarservice.ui.screens.employee.EmployeeHomeScreen
import com.example.autocarservice.ui.screens.shared.ProfileScreen
import com.example.autocarservice.ui.screens.shared.ServiceRequestDetailScreen
import com.example.autocarservice.ui.screens.shared.SplashScreen

/**
 * Navigation routes for the application
 */
sealed class Screen(val route: String) {
    // Auth screens
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Client screens
    object ClientHome : Screen("client_home")
    object CreateServiceRequest : Screen("create_service_request")
    
    // Employee screens
    object EmployeeHome : Screen("employee_home")
    
    // Shared screens
    object ServiceRequestDetail : Screen("service_request_detail/{serviceRequestId}") {
        fun createRoute(serviceRequestId: String) = "service_request_detail/$serviceRequestId"
    }
    object Profile : Screen("profile")
}

/**
 * Main navigation component for the application
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val actions = remember(navController) { NavigationActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Splash.route) {
            SplashScreen(
                navigateToLogin = actions.navigateToLogin,
                navigateToClientHome = actions.navigateToClientHome,
                navigateToEmployeeHome = actions.navigateToEmployeeHome
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                navigateToRegister = actions.navigateToRegister,
                navigateToClientHome = actions.navigateToClientHome,
                navigateToEmployeeHome = actions.navigateToEmployeeHome
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                navigateToLogin = actions.navigateToLogin,
                navigateToClientHome = actions.navigateToClientHome,
                navigateToEmployeeHome = actions.navigateToEmployeeHome
            )
        }
        
        // Client screens
        composable(Screen.ClientHome.route) {
            ClientHomeScreen(
                navigateToCreateServiceRequest = actions.navigateToCreateServiceRequest,
                navigateToServiceRequestDetail = actions.navigateToServiceRequestDetail,
                navigateToProfile = actions.navigateToProfile,
                navigateToLogin = actions.navigateToLogin
            )
        }
        
        composable(Screen.CreateServiceRequest.route) {
            CreateServiceRequestScreen(
                navigateBack = actions.navigateBack,
                navigateToServiceRequestDetail = actions.navigateToServiceRequestDetail
            )
        }
        
        // Employee screens
        composable(Screen.EmployeeHome.route) {
            EmployeeHomeScreen(
                navigateToServiceRequestDetail = actions.navigateToServiceRequestDetail,
                navigateToProfile = actions.navigateToProfile,
                navigateToLogin = actions.navigateToLogin
            )
        }
        
        // Shared screens
        composable(
            route = Screen.ServiceRequestDetail.route,
            arguments = listOf(
                navArgument("serviceRequestId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val serviceRequestId = backStackEntry.arguments?.getString("serviceRequestId") ?: ""
            ServiceRequestDetailScreen(
                serviceRequestId = serviceRequestId,
                navigateBack = actions.navigateBack
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                navigateBack = actions.navigateBack
            )
        }
    }
}

/**
 * Navigation actions for the application
 */
class NavigationActions(private val navController: NavHostController) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToRegister: () -> Unit = {
        navController.navigate(Screen.Register.route)
    }
    
    val navigateToClientHome: () -> Unit = {
        navController.navigate(Screen.ClientHome.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToEmployeeHome: () -> Unit = {
        navController.navigate(Screen.EmployeeHome.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToCreateServiceRequest: () -> Unit = {
        navController.navigate(Screen.CreateServiceRequest.route)
    }
    
    val navigateToServiceRequestDetail: (String) -> Unit = { serviceRequestId ->
        navController.navigate(Screen.ServiceRequestDetail.createRoute(serviceRequestId))
    }
    
    val navigateToProfile: () -> Unit = {
        navController.navigate(Screen.Profile.route)
    }
    
    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}
