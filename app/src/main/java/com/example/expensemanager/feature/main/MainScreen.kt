package com.example.expensemanager.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.expensemanager.feature.categorymanagement.CategoryManagementScreen
import com.example.expensemanager.feature.home.HomeScreen

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
    onEditCategoryClick: (Long) -> Unit = {}
) {

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
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
                    onCategoryManagementClick = { onTabSelected(1) }
                )
                1 -> CategoryManagementScreen(
                    onNavigateBack = { onTabSelected(0) },
                    onAddCategoryClick = onAddCategoryClick,
                    onEditCategoryClick = onEditCategoryClick
                )
                2 -> SearchScreen()
                3 -> ProfileScreen()
            }
        }
    }
}

/**
 * Màn hình Tìm kiếm (placeholder)
 */
@Composable
private fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tìm kiếm giao dịch",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Chức năng đang phát triển",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Màn hình Cá nhân (placeholder)
 */
@Composable
private fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Thông tin cá nhân",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Chức năng đang phát triển",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Danh sách các item trong Bottom Navigation
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("Trang chủ", Icons.Default.Home),
    BottomNavItem("Danh mục", Icons.Default.List),
    BottomNavItem("Tìm kiếm", Icons.Default.Search),
    BottomNavItem("Cá nhân", Icons.Default.Person)
)
