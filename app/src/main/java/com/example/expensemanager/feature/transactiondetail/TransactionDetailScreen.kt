package com.example.expensemanager.feature.transactiondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val application = context.applicationContext as? android.app.Application
    ?: throw IllegalStateException("Application context is required")

  val viewModel: TransactionDetailViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
  )
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          DetailMonthYearSelector(
            selectedMonth = uiState.selectedMonth,
            onMonthSelected = { viewModel.onMonthChanged(it) }
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
      if (uiState.isLoading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      } else {
        Column(
          modifier = Modifier
            .fillMaxSize()
        ) {
          SummaryHeader(
            balance = uiState.balance,
            totalIncome = uiState.totalIncome,
            totalExpense = uiState.totalExpense
          )

          Text(
            text = "Thiết lập ngân sách",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
          )

          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(uiState.dailyGroups) { group ->
              DayTransactionCard(group = group)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailMonthYearSelector(
  selectedMonth: LocalDate,
  onMonthSelected: (LocalDate) -> Unit
) {
  val formatter = remember {
    DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("vi-VN"))
  }
  var showSheet by remember { mutableStateOf(false) }

  Surface(
    onClick = { showSheet = true },
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    shape = MaterialTheme.shapes.medium
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Text(
        text = "Tháng",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = selectedMonth.format(formatter),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  if (showSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tempYear by remember { mutableStateOf(selectedMonth.year) }
    var tempMonth by remember { mutableStateOf(selectedMonth.monthValue) }

    ModalBottomSheet(
      onDismissRequest = { showSheet = false },
      sheetState = sheetState
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = { showSheet = false }) {
            Text("Hủy")
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            IconButton(onClick = { tempYear-- }) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Năm trước"
              )
            }
            Text(
              text = tempYear.toString(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { tempYear++ }) {
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Năm sau"
              )
            }
          }

          TextButton(
            onClick = {
              onMonthSelected(LocalDate.of(tempYear, tempMonth, 1))
              showSheet = false
            }
          ) {
            Text("Xác nhận")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
        ) {
          items(12) { index ->
            val month = index + 1
            val isSelected = month == tempMonth
            Surface(
              onClick = { tempMonth = month },
              shape = MaterialTheme.shapes.medium,
              color = if (isSelected)
                MaterialTheme.colorScheme.primary
              else
                MaterialTheme.colorScheme.surfaceVariant
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Th$month",
                  style = MaterialTheme.typography.bodyMedium,
                  color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                  else
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun SummaryHeader(
  balance: Long,
  totalIncome: Long,
  totalExpense: Long
) {
  val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.primary)
      .padding(horizontal = 16.dp, vertical = 20.dp)
  ) {
    Text(
      text = "Số dư",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onPrimary
    )
    Text(
      text = formatter.format(balance),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onPrimary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "Chi tiêu",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
          text = "-${formatter.format(totalExpense)}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFFFCDD2)
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "Thu nhập",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
          text = formatter.format(totalIncome),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFC8E6C9)
        )
      }
    }
  }
}

@Composable
private fun DayTransactionCard(
  group: DayTransactionGroup
) {
  val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val dayText = group.date.dayOfMonth.toString().padStart(2, '0')
        val monthText = group.date.monthValue
        Text(
          text = "Th$monthText $dayText",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Chi tiêu:${if (group.totalExpense != 0L) "-${formatter.format(group.totalExpense)}" else "0"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Thu nhập:${formatter.format(group.totalIncome)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      group.transactions.forEach { item ->
        TransactionRow(item = item)
      }
    }
  }
}

@Composable
private fun TransactionRow(item: TransactionItemUi) {
  val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color(0xFFFFCC80)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = item.categoryName.firstOrNull()?.toString() ?: "?",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 12.dp)
    ) {
      Text(
        text = item.categoryName,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (!item.note.isNullOrBlank()) {
        Text(
          text = item.note,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Text(
      text = (if (item.isIncome) "" else "-") + formatter.format(item.amount),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = if (item.isIncome) MaterialTheme.colorScheme.primary else Color(0xFFF44336),
      textAlign = TextAlign.End
    )
  }
}
