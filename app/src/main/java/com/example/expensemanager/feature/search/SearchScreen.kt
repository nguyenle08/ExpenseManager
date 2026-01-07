package com.example.expensemanager.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.ui.composable.rememberSettings
import com.example.expensemanager.util.CurrencyFormatter
import com.example.expensemanager.util.LocaleManager
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//TÌM KIẾM GIAO DỊCH
  //🎯 Chức năng
  // Tìm theo:
    //Tên danh mục
    //Ghi chú
  //Lọc:
    //Tất cả
    //Tháng này
    //Năm nay
fun SearchScreen(
  onNavigateBack: () -> Unit,
  onTransactionClick: (Long) -> Unit = {}
) {
  val context = LocalContext.current
  val application = context.applicationContext as? android.app.Application
    ?: throw IllegalStateException("Application context is required")

  val settings = rememberSettings()
  //SEARCHSCREEN KHỞI TẠO VIEWMODEL
  val viewModel: SearchViewModel = viewModel(
    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
  )
  //collectAsState() bắt đầu lắng nghe uiState
  val uiState by viewModel.uiState.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var filterType by remember { mutableStateOf("all") } // "all", "month", "year"

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          OutlinedTextField(
            value = searchQuery,
            //🔹 Khi gõ text👉 SearchScreen gọi thẳng ViewModel
            onValueChange = { 
              searchQuery = it
              //🔹 Search bar
              viewModel.search(it, filterType)
            },
            placeholder = { 
              Text(LocaleManager.getString(context, "search_placeholder")) 
            },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { 
                  searchQuery = ""
                  viewModel.search("", filterType)
                }) {
                  Icon(Icons.Default.Close, contentDescription = "Clear")
                }
              }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
          )
        },
        navigationIcon = {
          //👉 Khi user bấm nút Back
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Filter chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        //USER ĐỔI FILTER👉 Mỗi thao tác → gọi search()
        FilterChip(
          selected = filterType == "all",
          onClick = { 
            filterType = "all"
            //🔹 Khi đổi filter👉 Mỗi thao tác → gọi search() trong ViewModel
            viewModel.search(searchQuery, "all")
          },
          label = { Text(LocaleManager.getString(context, "all")) }
        )
        FilterChip(
          selected = filterType == "month",
          onClick = { 
            filterType = "month"
            viewModel.search(searchQuery, "month")
          },
          label = { Text(LocaleManager.getString(context, "this_month")) }
        )
        FilterChip(
          selected = filterType == "year",
          onClick = { 
            filterType = "year"
            viewModel.search(searchQuery, "year")
          },
          label = { Text(LocaleManager.getString(context, "this_year")) }
        )
      }

      Divider()

      // Results
      if (uiState.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else if (uiState.searchResults.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = LocaleManager.getString(context, "no_results"),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = LocaleManager.getString(context, "try_different_search"),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        //UI HIỂN THỊ KẾT QUẢ
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(uiState.searchResults) { group ->
            SearchResultDayCard(
              group = group,
              onTransactionClick = onTransactionClick,
              settings = settings
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SearchResultDayCard(
  group: SearchResultGroup,
  onTransactionClick: (Long) -> Unit,
  settings: com.example.expensemanager.ui.composable.SettingsState
) {
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

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
      // Date header
      Text(
        text = group.date.format(dateFormatter),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      // Transactions
      group.transactions.forEach { item ->
        SearchResultItem(
          item = item,
          onClick = { onTransactionClick(item.id) },
          settings = settings
        )
      }
    }
  }
}

@Composable
private fun SearchResultItem(
  item: SearchTransactionItem,
  onClick: () -> Unit,
  settings: com.example.expensemanager.ui.composable.SettingsState
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
      )
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Category icon/circle
    Surface(
      modifier = Modifier.size(40.dp),
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primaryContainer
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = item.categoryName.firstOrNull()?.toString() ?: "?",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Category and note
    Column(
      modifier = Modifier.weight(1f)
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

    // Amount
    Text(
      text = (if (item.isIncome) "+" else "-") + CurrencyFormatter.format(item.amount, settings.context),
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Bold,
      color = if (item.isIncome) 
        MaterialTheme.colorScheme.primary 
      else 
        MaterialTheme.colorScheme.error
    )
  }
}