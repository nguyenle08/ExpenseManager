package com.example.expensemanager.feature.addtransaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.expensemanager.data.entity.TransactionType
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Add Transaction Screen - Màn hình thêm giao dịch mới
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
  transactionId: Long? = null,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val application = context.applicationContext as? android.app.Application
    ?: throw IllegalStateException("Application context is required")

  val viewModel: AddTransactionViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
  )
  val uiState by viewModel.uiState.collectAsState()

  // Load transaction data khi edit
  LaunchedEffect(transactionId) {
    if (transactionId != null && transactionId > 0) {
      viewModel.loadTransaction(transactionId)
    }
  }

  var amountField by remember(uiState.amountText) {
    mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(
      uiState.amountText,
      androidx.compose.ui.text.TextRange(uiState.amountText.length)
    ))
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (transactionId != null && transactionId > 0) "Sửa giao dịch" else "Thêm giao dịch") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, "Quay lại")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      // Error message
      uiState.error?.let { error ->
        AssistChip(
          onClick = {},
          label = {
            Text(text = error)
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error
            )
          },
          colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Transaction Type Toggle
      TransactionTypeToggle(
        selectedType = uiState.type,
        onTypeSelected = { viewModel.onTransactionTypeChanged(it) }
      )

      // Amount Input
      OutlinedTextField(
        value = amountField,
        onValueChange = { newValue ->
          amountField = newValue
          viewModel.onAmountChanged(newValue.text)
        },
        label = { Text("Số tiền") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text("đ") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
      )

      // Date Picker
      DateSelector(
        date = uiState.date,
        onDateChange = { viewModel.onDateChanged(it) }
      )

      // Category Selection
      Text(
        text = "Chọn danh mục",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(300.dp)
      ) {
        items(uiState.categories) { category ->
          CategoryItem(
            category = category,
            isSelected = category.id == uiState.selectedCategory?.id,
            onClick = { viewModel.onCategorySelected(category) }
          )
        }
      }

      // Note Input
      OutlinedTextField(
        value = uiState.note,
        onValueChange = { viewModel.onNoteChanged(it) },
        label = { Text("Ghi chú (không bắt buộc)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3
      )

      Button(
        onClick = { viewModel.saveTransaction(onSuccess = onNavigateBack) },
        enabled = !uiState.isLoading,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
      ) {
        if (uiState.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
        } else {
          Text("Lưu giao dịch")
        }
      }
    }
  }
}

@Composable
private fun TransactionTypeToggle(
  selectedType: TransactionType,
  onTypeSelected: (TransactionType) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    FilterChip(
      selected = selectedType == TransactionType.EXPENSE,
      onClick = { onTypeSelected(TransactionType.EXPENSE) },
      label = {
        Text(
          text = "Chi tiêu",
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center
        )
      },
      modifier = Modifier.weight(1f),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Color(0xFFF44336),
        selectedLabelColor = Color.White
      )
    )

    FilterChip(
      selected = selectedType == TransactionType.INCOME,
      onClick = { onTypeSelected(TransactionType.INCOME) },
      label = {
        Text(
          text = "Thu nhập",
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center
        )
      },
      modifier = Modifier.weight(1f),
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Color(0xFF4CAF50),
        selectedLabelColor = Color.White
      )
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
  date: java.time.LocalDate,
  onDateChange: (java.time.LocalDate) -> Unit
) {
  val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
  var showPicker by remember { mutableStateOf(false) }

  if (showPicker) {
    val dateMillis = remember(date) {
      date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    DatePickerDialog(
      onDismissRequest = { showPicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            val millis = datePickerState.selectedDateMillis
            if (millis != null) {
              val newDate = java.time.Instant.ofEpochMilli(millis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
              onDateChange(newDate)
            }
            showPicker = false
          }
        ) {
          Text("Xác nhận")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPicker = false }) {
          Text("Hủy")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  OutlinedCard(
    onClick = { showPicker = true },
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Ngày giao dịch",
        style = MaterialTheme.typography.bodyLarge
      )
      Text(
        text = date.format(formatter),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}

@Composable
private fun CategoryItem(
  category: com.example.expensemanager.data.entity.CategoryEntity,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val backgroundColor = if (isSelected) {
    try {
      Color(category.color.toColorInt())
    } catch (e: Exception) {
      MaterialTheme.colorScheme.primary
    }
  } else {
    MaterialTheme.colorScheme.surfaceVariant
  }

  val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

  Surface(
    onClick = onClick,
    modifier = Modifier
      .aspectRatio(1f)
      .fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = backgroundColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = category.icon,
        fontSize = 32.sp,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = category.name,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        color = contentColor,
        maxLines = 1
      )
    }
  }
}
