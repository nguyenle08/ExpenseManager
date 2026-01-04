package com.example.expensemanager.feature.categorymanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.data.entity.CategoryEntity
import com.example.expensemanager.data.entity.TransactionType

/**
 * Màn hình quản lý danh mục
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
  onNavigateBack: () -> Unit = {},
  onAddCategoryClick: () -> Unit = {},
  onEditCategoryClick: (Long) -> Unit = {}
) {
  val context = LocalContext.current
  val application = context.applicationContext as? android.app.Application
    ?: throw IllegalStateException("Application context is required")

  val viewModel: CategoryManagementViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
  )
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Danh mục") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
          }
        },
        actions = {
          IconButton(onClick = onAddCategoryClick) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Thêm danh mục",
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Tab selector
      TabRow(
        selectedTabIndex = if (uiState.selectedType == TransactionType.EXPENSE) 0 else 1,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Tab(
          selected = uiState.selectedType == TransactionType.EXPENSE,
          onClick = { viewModel.onTabChanged(TransactionType.EXPENSE) },
          text = { Text("Chi tiêu(${uiState.expenseCount})") }
        )
        Tab(
          selected = uiState.selectedType == TransactionType.INCOME,
          onClick = { viewModel.onTabChanged(TransactionType.INCOME) },
          text = { Text("Thu nhập(${uiState.incomeCount})") }
        )
      }

      // Content
      if (uiState.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else {
        CategoryList(
          categories = uiState.categories,
          onEditClick = { category -> onEditCategoryClick(category.id) },
          onDeleteClick = { viewModel.onDeleteCategory(it) }
        )
      }
    }
  }

  // Dialog thêm/sửa category
  if (uiState.showDialog) {
    AddEditCategoryDialog(
      icon = uiState.dialogIcon,
      color = uiState.dialogColor,
      isEditing = uiState.editingCategory != null,
      availableIcons = viewModel.availableIcons,
      availableColors = viewModel.availableColors,
      initialName = uiState.dialogName,
      onIconChange = { viewModel.onDialogIconChanged(it) },
      onColorChange = { viewModel.onDialogColorChanged(it) },
      onSave = { name -> viewModel.onSaveCategory(name) },
      onDismiss = { viewModel.onDialogDismiss() }
    )
  }

  // Snackbar cho lỗi
  uiState.errorMessage?.let { message ->
    LaunchedEffect(message) {
      kotlinx.coroutines.delay(3000)
      viewModel.onErrorDismiss()
    }
    Snackbar(
      modifier = Modifier.padding(16.dp),
      action = {
        TextButton(onClick = { viewModel.onErrorDismiss() }) {
          Text("Đóng")
        }
      }
    ) {
      Text(message)
    }
  }
}

/**
 * Danh sách categories
 */
@Composable
private fun CategoryList(
  categories: List<CategoryEntity>,
  onEditClick: (CategoryEntity) -> Unit,
  onDeleteClick: (CategoryEntity) -> Unit
) {
  if (categories.isEmpty()) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Chưa có danh mục nào\nNhấn nút + để thêm",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(categories) { category ->
        CategoryItem(
          category = category,
          onEditClick = { onEditClick(category) },
          onDeleteClick = { onDeleteClick(category) }
        )
      }
    }
  }
}

/**
 * Item hiển thị từng category
 */
@Composable
private fun CategoryItem(
  category: CategoryEntity,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon và màu sắc
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(parseColor(category.color)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = category.icon,
          style = MaterialTheme.typography.headlineMedium
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Tên category
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = category.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium
        )
        if (category.isDefault) {
          Text(
            text = "Danh mục mặc định",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Menu 3 chấm: Chỉnh sửa / Xóa
      var menuExpanded by remember { mutableStateOf(false) }

      Box {
        IconButton(onClick = { menuExpanded = true }) {
          Icon(
            Icons.Default.MoreVert,
            contentDescription = "Tùy chọn",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          DropdownMenuItem(
            text = { Text("Chỉnh sửa") },
            onClick = {
              menuExpanded = false
              onEditClick()
            }
          )
          DropdownMenuItem(
            text = { Text("Xóa") },
            enabled = !category.isDefault,
            onClick = {
              menuExpanded = false
              onDeleteClick()
            }
          )
        }
      }
    }
  }
}

/**
 * Dialog để thêm/sửa category
 */
@Composable
private fun AddEditCategoryDialog(
  icon: String,
  color: String,
  isEditing: Boolean,
  availableIcons: List<String>,
  availableColors: List<String>,
  initialName: String = "",
  onIconChange: (String) -> Unit,
  onColorChange: (String) -> Unit,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var nameText by rememberSaveable { mutableStateOf(initialName) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 600.dp),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Text(
          text = if (isEditing) "Chỉnh sửa danh mục" else "Thêm danh mục mới",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tên danh mục
        OutlinedTextField(
          value = nameText,
          onValueChange = { nameText = it },
          label = { Text("Tên danh mục") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chọn icon
        Text(
          text = "Chọn biểu tượng",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(8),
          modifier = Modifier.height(120.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(availableIcons) { iconItem ->
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                  if (iconItem == icon) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surface
                )
                .border(
                  width = if (iconItem == icon) 2.dp else 1.dp,
                  color = if (iconItem == icon) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline,
                  shape = CircleShape
                )
                .clickable { onIconChange(iconItem) },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = iconItem,
                style = MaterialTheme.typography.titleMedium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chọn màu sắc
        Text(
          text = "Chọn màu sắc",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(7),
          modifier = Modifier.height(80.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(availableColors) { colorItem ->
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(parseColor(colorItem))
                .border(
                  width = if (colorItem == color) 3.dp else 0.dp,
                  color = MaterialTheme.colorScheme.primary,
                  shape = CircleShape
                )
                .clickable { onColorChange(colorItem) }
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Hủy")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(onClick = {
            onSave(nameText)
          }) {
            Text(if (isEditing) "Cập nhật" else "Thêm")
          }
        }
      }
    }
  }
}

/**
 * Parse màu từ hex string
 */
private fun parseColor(hexColor: String): Color {
  return try {
    Color(android.graphics.Color.parseColor(hexColor))
  } catch (e: Exception) {
    Color.Gray
  }
}
