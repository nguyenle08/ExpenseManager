package com.example.expensemanager.feature.categorymanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Màn hình thêm/sửa danh mục
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    categoryId: Long? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as? android.app.Application
        ?: throw IllegalStateException("Application context is required")

    val viewModel: CategoryManagementViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val uiState by viewModel.uiState.collectAsState()

    // State TextFieldValue cục bộ để hỗ trợ gõ tiếng Việt mượt hơn
    var nameField by remember {
        mutableStateOf(TextFieldValue(""))
    }
    
    // Biến để track khi đã init xong
    var isInitialized by remember { mutableStateOf(false) }

    // Load category để edit nếu có categoryId
    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            viewModel.loadCategoryForEdit(categoryId)
        } else {
            viewModel.prepareForNewCategory()
        }
    }

    // Chỉ khởi tạo TextFieldValue MỘT LẦN sau khi ViewModel đã load xong
    LaunchedEffect(uiState.dialogName) {
        if (!isInitialized && uiState.dialogName.isNotEmpty()) {
            nameField = TextFieldValue(
                uiState.dialogName,
                TextRange(uiState.dialogName.length)
            )
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (categoryId != null) "Chỉnh sửa" else "Thêm",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onSaveCategory(nameField.text)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Lưu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // TextField tên danh mục
            OutlinedTextField(
                value = nameField,
                onValueChange = { value ->
                    nameField = value
                },
                label = { Text("Tên danh mục") },
                placeholder = { Text("Tên danh mục") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Hiển thị số ký tự
            Text(
                text = "${nameField.text.length}/15",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Chọn màu
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Chọn màu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.availableColors) { colorItem ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(parseColor(colorItem))
                                .border(
                                    width = if (colorItem == uiState.dialogColor) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) { viewModel.onDialogColorChanged(colorItem) }
                        )
                    }
                }
            }

            // Chọn biểu tượng
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Chọn biểu tượng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.availableIcons) { iconItem ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (iconItem == uiState.dialogIcon)
                                        parseColor(uiState.dialogColor)
                                    else
                                        parseColor(uiState.dialogColor).copy(alpha = 0.3f)
                                )
                                .border(
                                    width = if (iconItem == uiState.dialogIcon) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) { viewModel.onDialogIconChanged(iconItem) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iconItem,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
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
 * Parse màu từ hex string
 */
private fun parseColor(hexColor: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        Color.Gray
    }
}
