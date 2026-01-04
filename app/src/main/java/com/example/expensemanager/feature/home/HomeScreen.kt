package com.example.expensemanager.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.widget.SimpleLineChart
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

/**
 * Home Screen - Trang chủ ứng dụng Quản Lý Chi Tiêu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransactionClick: () -> Unit = {},
    onCategoryManagementClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as? android.app.Application 
        ?: throw IllegalStateException("Application context is required")
    
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            HomeTopBar(
                onSettingsClick = onCategoryManagementClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm giao dịch"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                HomeContent(
                    uiState = uiState,
                    onMonthChanged = { viewModel.onMonthChanged(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearSelector(
    selectedMonth: LocalDate,
    onMonthSelected: (LocalDate) -> Unit
) {
    val formatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("vi-VN"))
    }
    var showSheet by remember { mutableStateOf(false) }

    Surface(
        onClick = { showSheet = true },
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Tháng",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Text(
                text = selectedMonth.format(formatter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
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

/**
 * Top App Bar với Month Picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onSettingsClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = "Trang chủ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Quản lý danh mục",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Nội dung chính của trang chủ
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onMonthChanged: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHeaderCard(
            selectedMonth = uiState.selectedMonth,
            balance = uiState.balance,
            totalIncome = uiState.totalIncome,
            totalExpense = uiState.totalExpense,
            onMonthChanged = onMonthChanged
        )
        
        DailyOverviewCard(
            selectedMonth = uiState.selectedMonth,
            chartData = uiState.chartData
        )
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeaderCard(
    selectedMonth: LocalDate,
    balance: Long,
    totalIncome: Long,
    totalExpense: Long,
    onMonthChanged: (LocalDate) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onMonthChanged(selectedMonth.minusMonths(1)) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tháng trước"
                        )
                    }

                    MonthYearSelector(
                        selectedMonth = selectedMonth,
                        onMonthSelected = onMonthChanged
                    )

                    IconButton(
                        onClick = { onMonthChanged(selectedMonth.plusMonths(1)) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Tháng sau"
                        )
                    }
                }

                IconButton(
                    onClick = { /* TODO: mở màn hình cài đặt */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt"
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Số dư tháng này",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Text(
                    text = formatCurrency(balance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Thu nhập",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(totalIncome),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB2FF59)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Chi tiêu",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(totalExpense),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFCDD2)
                        )
                    }
                }
            }
        }
    }
}

private enum class ChartMode { EXPENSE, INCOME }

@Composable
private fun DailyOverviewCard(
    selectedMonth: LocalDate,
    chartData: List<DayData>
) {
    var chartMode by remember { mutableStateOf(ChartMode.EXPENSE) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hàng ngày",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Theo dõi thu/chi mỗi ngày",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = chartMode == ChartMode.EXPENSE,
                        onClick = { chartMode = ChartMode.EXPENSE },
                        label = { Text("Chi tiêu") }
                    )
                    FilterChip(
                        selected = chartMode == ChartMode.INCOME,
                        onClick = { chartMode = ChartMode.INCOME },
                        label = { Text("Thu nhập") }
                    )
                }
            }

            val primary = MaterialTheme.colorScheme.primary
            val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            val incomeColor = if (chartMode == ChartMode.INCOME) primary else muted
            val expenseColor = if (chartMode == ChartMode.EXPENSE) primary else muted

            SimpleLineChart(
                data = chartData,
                modifier = Modifier.fillMaxWidth(),
                incomeColor = incomeColor,
                expenseColor = expenseColor
            )

            DailyStatsTable(
                selectedMonth = selectedMonth,
                data = chartData
            )
        }
    }
}

@Composable
private fun DailyStatsTable(
    selectedMonth: LocalDate,
    data: List<DayData>
) {
    if (data.isEmpty()) {
        Text(
            text = "Chưa có dữ liệu thống kê",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    val nonEmptyDays = remember(data) {
        data.filter { it.income != 0L || it.expense != 0L }
            .sortedBy { it.date }
    }

    if (nonEmptyDays.isEmpty()) {
        Text(
            text = "Chưa có dữ liệu thống kê",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    val totalIncome = nonEmptyDays.sumOf { it.income }
    val totalExpense = nonEmptyDays.sumOf { it.expense }
    val daysInMonth = nonEmptyDays.size.coerceAtLeast(1)
    val avgIncome = totalIncome / daysInMonth
    val avgExpense = totalExpense / daysInMonth

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Tổng quan
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Tổng cộng",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatCurrency(totalIncome),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                text = formatCurrency(totalExpense),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                text = formatCurrency(totalIncome - totalExpense),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TB/ngày",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatCurrency(avgIncome),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                text = formatCurrency(avgExpense),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                text = formatCurrency(avgIncome - avgExpense),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Ngày",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Thu nhập",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Chi tiêu",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Số dư",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        var runningBalance = 0L

        nonEmptyDays.forEach { day ->
            runningBalance += day.income - day.expense

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = day.date.dayOfMonth.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (day.income == 0L) "-" else formatCurrency(day.income),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (day.expense == 0L) "-" else formatCurrency(day.expense),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatCurrency(runningBalance),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Card hiển thị thông tin tóm tắt với animation
 */
@Composable
private fun AnimatedSummaryCard(
    title: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    var animatedAmount by remember { mutableStateOf(0L) }
    
    LaunchedEffect(amount) {
        val duration = 800
        val steps = 30
        val increment = amount / steps
        
        repeat(steps) { step ->
            animatedAmount = (increment * (step + 1)).coerceAtMost(amount)
            kotlinx.coroutines.delay(duration.toLong() / steps)
        }
        animatedAmount = amount
    }
    
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatCurrency(animatedAmount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
/**
 * Format số tiền theo định dạng VND
 */
private fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    return formatter.format(amount)
}

/**
 * Preview với mock data
 */
@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        Surface {
            HomeScreen(
                onAddTransactionClick = {}
            )
        }
    }
}
