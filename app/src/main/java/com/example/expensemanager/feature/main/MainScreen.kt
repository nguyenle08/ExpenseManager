package com.example.expensemanager.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.feature.categorymanagement.CategoryManagementScreen
import com.example.expensemanager.feature.home.HomeScreen
import com.example.expensemanager.feature.transactiondetail.TransactionDetailScreen

/**
 * Màn hình chính với Bottom Navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  onAddTransactionClick: () -> Unit = {},
  onAddCategoryClick: () -> Unit = {},
  onEditCategoryClick: (Long) -> Unit = {},
  onTransactionDetailClick: () -> Unit = {},
  onTransactionItemClick: (Long) -> Unit = {},
  onReportClick: () -> Unit = {},
  onSettingsClick: () -> Unit = {},
  onSearchClick: () -> Unit = {}
) {

  val context = LocalContext.current
  
  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        bottomNavItems.forEachIndexed { index, item ->
          val label = com.example.expensemanager.util.LocaleManager.getString(context, item.labelKey)
          NavigationBarItem(
            icon = {
              Icon(
                imageVector = item.icon,
                contentDescription = label
              )
            },
            label = { Text(label) },
            selected = selectedTab == index,
            onClick = { onTabSelected(index) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }
  ) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
      when (selectedTab) {
        0 -> HomeScreen(
          onAddTransactionClick = onAddTransactionClick,
          onCategoryManagementClick = { onTabSelected(1) },
          onTransactionDetailClick = onTransactionDetailClick,
          onReportClick = onReportClick,
          onSettingsClick = onSettingsClick,
          onSearchClick = onSearchClick
        )
        1 -> CategoryManagementScreen(
          onNavigateBack = { onTabSelected(0) },
          onAddCategoryClick = onAddCategoryClick,
          onEditCategoryClick = onEditCategoryClick
        )
        2 -> {
          // Trigger add transaction and return to home
          LaunchedEffect(Unit) {
            onAddTransactionClick()
            onTabSelected(0)
          }
          // Show home screen while navigating
          HomeScreen(
            onAddTransactionClick = onAddTransactionClick,
            onCategoryManagementClick = { onTabSelected(1) },
            onTransactionDetailClick = onTransactionDetailClick,
            onReportClick = onReportClick,
            onSettingsClick = onSettingsClick
          )
        }
        3 -> TransactionDetailScreen(
          onNavigateBack = { onTabSelected(0) },
          onTransactionClick = onTransactionItemClick
        )
        4 -> {
          // Trigger report and return to home
          LaunchedEffect(Unit) {
            onReportClick()
            onTabSelected(0)
          }
          // Show home screen while navigating
          HomeScreen(
            onAddTransactionClick = onAddTransactionClick,
            onCategoryManagementClick = { onTabSelected(1) },
            onTransactionDetailClick = onTransactionDetailClick,
            onReportClick = onReportClick,
            onSettingsClick = onSettingsClick
          )
        }
      }
    }
  }
}

/**
 * Danh sách các item trong Bottom Navigation
 */
data class BottomNavItem(
  val labelKey: String,
  val icon: ImageVector
)

private val bottomNavItems = listOf(
  BottomNavItem("home", Icons.Default.Home),
  BottomNavItem("categories", Icons.Default.List),
  BottomNavItem("add_transaction", Icons.Default.Add),
  BottomNavItem("transactions", Icons.Default.Menu),
  BottomNavItem("report", Icons.Default.Info)
)