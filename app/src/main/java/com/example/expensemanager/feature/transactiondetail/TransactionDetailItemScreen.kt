package com.example.expensemanager.feature.transactiondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Màn hình chi tiết của một giao dịch cụ thể
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailItemScreen(
  transactionId: Long,
  onNavigateBack: () -> Unit,
  onNavigateToEdit: (Long) -> Unit
) {
  val context = LocalContext.current
  val application = context.applicationContext as? android.app.Application
    ?: throw IllegalStateException("Application context is required")

  val viewModel: TransactionDetailItemViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
  )

  val uiState by viewModel.uiState.collectAsState()
  var showDeleteDialog by remember { mutableStateOf(false) }

  LaunchedEffect(transactionId) {
    viewModel.loadTransaction(transactionId)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Chi tiết") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
          }
        },
        actions = {
          IconButton(onClick = { onNavigateToEdit(transactionId) }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa")
          }
          IconButton(onClick = { showDeleteDialog = true }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa")
          }
        }
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when {
        uiState.isLoading -> {
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.error != null -> {
          Text(
            text = uiState.error ?: "Có lỗi xảy ra",
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.error
          )
        }
        uiState.transaction != null -> {
          TransactionDetailContent(
            transaction = uiState.transaction!!,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }

  // Dialog xác nhận xóa
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Xác nhận xóa") },
      text = { Text("Bạn có chắc chắn muốn xóa giao dịch này không?") },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.deleteTransaction(transactionId)
            showDeleteDialog = false
            onNavigateBack()
          }
        ) {
          Text("Xóa")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Hủy")
        }
      }
    )
  }
}

@Composable
private fun TransactionDetailContent(
  transaction: TransactionDetailItemUi,
  modifier: Modifier = Modifier
) {
  val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
  val dateFormatter = remember { DateTimeFormatter.ofPattern("d 'Th.'M, yyyy") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(24.dp))

    // Icon danh mục
    Box(
      modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .background(
          transaction.categoryColor?.let {
            try {
              Color(android.graphics.Color.parseColor(it))
            } catch (e: Exception) {
              Color(0xFFFFCC80)
            }
          } ?: Color(0xFFFFCC80)
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = transaction.categoryIcon ?: transaction.categoryName.firstOrNull()?.toString() ?: "?",
        style = MaterialTheme.typography.displayMedium,
        color = Color.White
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Số tiền
    Text(
      text = (if (transaction.isIncome) "" else "-") + formatter.format(transaction.amount),
      style = MaterialTheme.typography.displayLarge,
      fontWeight = FontWeight.Bold,
      color = if (transaction.isIncome)
        MaterialTheme.colorScheme.primary
      else
        Color(0xFFF44336),
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Card thông tin chi tiết
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Danh mục
        DetailRow(
          icon = "📂",
          label = "Danh mục",
          value = transaction.categoryName
        )

        Divider()

        // Ngày & Giờ
        DetailRow(
          icon = "📅",
          label = "Ngày & Giờ",
          value = transaction.date.format(dateFormatter)
        )

        Divider()

        // Ghi chú
        DetailRow(
          icon = "📝",
          label = "Ghi chú",
          value = transaction.note?.takeIf { it.isNotBlank() } ?: "hhh"
        )
      }
    }
  }
}

@Composable
private fun DetailRow(
  icon: String,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = icon,
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f, fill = false)
    )
  }
}
