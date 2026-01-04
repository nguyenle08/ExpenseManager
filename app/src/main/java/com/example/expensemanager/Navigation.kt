package com.example.expensemanager

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expensemanager.feature.addtransaction.AddTransactionScreen
import com.example.expensemanager.feature.home.HomeScreen

/**
 * Navigation routes
 */
object Routes {
    const val HOME = "home"
    const val ADD_TRANSACTION = "add_transaction"
}

/**
 * Main Navigation Host
 */
@Composable
fun ExpenseManagerNavHost() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddTransactionClick = {
                    navController.navigate(Routes.ADD_TRANSACTION)
                }
            )
        }
        
        composable(Routes.ADD_TRANSACTION) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
