package com.example.expensemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.expensemanager.feature.addtransaction.AddTransactionScreen
import com.example.expensemanager.feature.categorymanagement.AddEditCategoryScreen
import com.example.expensemanager.feature.main.MainScreen

/**
 * Navigation routes
 */
object Routes {
  const val MAIN = "main"
  const val ADD_TRANSACTION = "add_transaction"
  const val ADD_EDIT_CATEGORY = "add_edit_category"
}

/**
 * Main Navigation Host
 */
@Composable
fun ExpenseManagerNavHost() {
  val navController = rememberNavController()
  var selectedTab by remember { mutableStateOf(0) }

  NavHost(
    navController = navController,
    startDestination = Routes.MAIN
  ) {
    composable(Routes.MAIN) {
      MainScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onAddTransactionClick = {
          navController.navigate(Routes.ADD_TRANSACTION)
        },
        onAddCategoryClick = {
          navController.navigate(Routes.ADD_EDIT_CATEGORY)
        },
        onEditCategoryClick = { id ->
          navController.navigate("${Routes.ADD_EDIT_CATEGORY}?categoryId=$id")
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

    composable(
      route = "${Routes.ADD_EDIT_CATEGORY}?categoryId={categoryId}",
      arguments = listOf(
        navArgument("categoryId") {
          type = NavType.LongType
          defaultValue = -1L
        }
      )
    ) { backStackEntry ->
      val categoryIdArg = backStackEntry.arguments?.getLong("categoryId")
      val categoryId = if (categoryIdArg != null && categoryIdArg != -1L) categoryIdArg else null

      AddEditCategoryScreen(
        categoryId = categoryId,
        onNavigateBack = {
          navController.popBackStack()
          selectedTab = 1
        }
      )
    }
  }
}
