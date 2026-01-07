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
import com.example.expensemanager.feature.report.CategoryReportDetailScreen
import com.example.expensemanager.feature.report.ReportScreen
import com.example.expensemanager.feature.search.SearchScreen
import com.example.expensemanager.feature.settings.SettingsScreen
import com.example.expensemanager.feature.transactiondetail.TransactionDetailScreen
import com.example.expensemanager.feature.transactiondetail.TransactionDetailItemScreen

/**
 * Navigation routes
 */
object Routes {
  const val MAIN = "main"
  const val ADD_TRANSACTION = "add_transaction"
  const val EDIT_TRANSACTION = "edit_transaction"
  const val ADD_EDIT_CATEGORY = "add_edit_category"
  const val TRANSACTION_DETAIL = "transaction_detail"
  const val TRANSACTION_DETAIL_ITEM = "transaction_detail_item"
  const val REPORT = "report"
  const val CATEGORY_REPORT_DETAIL = "category_report_detail"
  const val SETTINGS = "settings"
  const val SEARCH = "search"
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
        },
        onTransactionDetailClick = {
          navController.navigate(Routes.TRANSACTION_DETAIL)
        },
        onTransactionItemClick = { transactionId ->
          navController.navigate("${Routes.TRANSACTION_DETAIL_ITEM}/$transactionId")
        },
        onReportClick = {
          navController.navigate(Routes.REPORT)
        },
        onSettingsClick = {
          navController.navigate(Routes.SETTINGS)
        },
        onSearchClick = {
          navController.navigate(Routes.SEARCH)
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
      route = "${Routes.EDIT_TRANSACTION}/{transactionId}",
      arguments = listOf(
        navArgument("transactionId") {
          type = NavType.LongType
        }
      )
    ) { backStackEntry ->
      val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
      AddTransactionScreen(
        transactionId = transactionId,
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

    composable(Routes.TRANSACTION_DETAIL) {
      TransactionDetailScreen(
        onNavigateBack = {
          navController.popBackStack()
        },
        onTransactionClick = { transactionId ->
          navController.navigate("${Routes.TRANSACTION_DETAIL_ITEM}/$transactionId")
        }
      )
    }

    composable(
      route = "${Routes.TRANSACTION_DETAIL_ITEM}/{transactionId}",
      arguments = listOf(
        navArgument("transactionId") {
          type = NavType.LongType
        }
      )
    ) { backStackEntry ->
      val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
      TransactionDetailItemScreen(
        transactionId = transactionId,
        onNavigateBack = {
          navController.popBackStack()
        },
        onNavigateToEdit = { id ->
          navController.navigate("${Routes.EDIT_TRANSACTION}/$id")
        }
      )
    }

    composable(Routes.REPORT) {
      ReportScreen(
        onNavigateBack = {
          navController.popBackStack()
        },
        onCategoryClick = { categoryId, categoryName, isYearMode, startDate, endDate ->
          navController.navigate(
            "${Routes.CATEGORY_REPORT_DETAIL}/$categoryId/$categoryName/$isYearMode/$startDate/$endDate"
          )
        }
      )
    }
    
    composable(
      route = "${Routes.CATEGORY_REPORT_DETAIL}/{categoryId}/{categoryName}/{isYearMode}/{startDate}/{endDate}",
      arguments = listOf(
        navArgument("categoryId") { type = NavType.LongType },
        navArgument("categoryName") { type = NavType.StringType },
        navArgument("isYearMode") { type = NavType.BoolType },
        navArgument("startDate") { type = NavType.StringType },
        navArgument("endDate") { type = NavType.StringType }
      )
    ) { backStackEntry ->
      val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
      val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
      val isYearMode = backStackEntry.arguments?.getBoolean("isYearMode") ?: false
      val startDate = backStackEntry.arguments?.getString("startDate") ?: ""
      val endDate = backStackEntry.arguments?.getString("endDate") ?: ""
      
      CategoryReportDetailScreen(
        categoryId = categoryId,
        categoryName = categoryName,
        isYearMode = isYearMode,
        startDate = startDate,
        endDate = endDate,
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }
    
    composable(Routes.SETTINGS) {
      SettingsScreen(
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }
    
    composable(Routes.SEARCH) {
      SearchScreen(
        onNavigateBack = {
          navController.popBackStack()
        },
        onTransactionClick = { transactionId ->
          navController.navigate("${Routes.TRANSACTION_DETAIL_ITEM}/$transactionId")
        }
      )
    }
  }
}