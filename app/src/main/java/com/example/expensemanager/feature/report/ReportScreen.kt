package com.example.expensemanager.feature.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit,
    onCategoryClick: (categoryId: Long, categoryName: String, isYearMode: Boolean, startDate: String, endDate: String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val application = context.applicationContext as? android.app.Application
        ?: throw IllegalStateException("Application context is required")

    val viewModel: ReportViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var periodType by remember { mutableStateOf("month") } // "month" or "year"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biểu đồ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
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
            // Tab Tháng/Năm
            var selectedPeriodTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedPeriodTab) {
                listOf("Tháng", "Năm").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedPeriodTab == index,
                        onClick = { 
                            selectedPeriodTab = index
                            periodType = if (index == 0) "month" else "year"
                            if (index == 1) {
                                viewModel.onYearChanged(uiState.selectedMonth.year)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Tháng/Năm hiển thị - Click để mở picker
            val monthFormatter = remember { DateTimeFormatter.ofPattern("'Th'M yyyy") }
            val yearFormatter = remember { DateTimeFormatter.ofPattern("yyyy") }
            val dateRangeFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd") }
            
            TextButton(
                onClick = { 
                    if (periodType == "month") {
                        showMonthPicker = true
                    } else {
                        showYearPicker = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (periodType == "month") 
                            uiState.selectedMonth.format(monthFormatter)
                        else
                            uiState.selectedMonth.format(yearFormatter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val startDate = if (periodType == "month") 
                        uiState.selectedMonth.withDayOfMonth(1)
                    else
                        LocalDate.of(uiState.selectedMonth.year, 1, 1)
                    
                    val endDate = if (periodType == "month")
                        uiState.selectedMonth.withDayOfMonth(uiState.selectedMonth.lengthOfMonth())
                    else
                        LocalDate.of(uiState.selectedMonth.year, 12, 31)
                    
                    Text(
                        text = "${startDate.format(dateRangeFormatter)} - ${endDate.format(dateRangeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab Chi tiêu/Thu nhập
            var selectedTypeTab by remember { mutableStateOf(0) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thống kê danh mục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeTab == 0,
                        onClick = { 
                            selectedTypeTab = 0
                            viewModel.onTypeChanged(false)
                        },
                        label = { Text("Chi tiêu") }
                    )
                    FilterChip(
                        selected = selectedTypeTab == 1,
                        onClick = { 
                            selectedTypeTab = 1
                            viewModel.onTypeChanged(true)
                        },
                        label = { Text("Thu nhập") }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Biểu đồ tròn
                    item {
                        DonutChart(
                            data = uiState.categoryStats,
                            total = uiState.total,
                            isIncome = selectedTypeTab == 1
                        )
                    }

                    // Danh sách danh mục
                    items(uiState.categoryStats) { stat ->
                        val dateRangeFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
                        val startDateStr: String
                        val endDateStr: String
                        
                        if (uiState.isYearMode) {
                            val year = uiState.selectedMonth.year
                            startDateStr = LocalDate.of(year, 1, 1).format(dateRangeFormatter)
                            endDateStr = LocalDate.of(year, 12, 31).format(dateRangeFormatter)
                        } else {
                            val month = uiState.selectedMonth
                            startDateStr = month.withDayOfMonth(1).format(dateRangeFormatter)
                            endDateStr = month.withDayOfMonth(month.lengthOfMonth()).format(dateRangeFormatter)
                        }
                        
                        CategoryStatItem(
                            stat = stat,
                            isIncome = selectedTypeTab == 1,
                            onClick = {
                                onCategoryClick(
                                    stat.categoryId,
                                    stat.name,
                                    uiState.isYearMode,
                                    startDateStr,
                                    endDateStr
                                )
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Month Picker Dialog
    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = uiState.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month ->
                viewModel.onMonthChanged(month)
                showMonthPicker = false
            }
        )
    }
    
    // Year Picker Dialog
    if (showYearPicker) {
        YearPickerDialog(
            currentYear = uiState.selectedMonth.year,
            onDismiss = { showYearPicker = false },
            onYearSelected = { year ->
                viewModel.onYearChanged(year)
                showYearPicker = false
            }
        )
    }
}

@Composable
private fun DonutChart(
    data: List<CategoryStatUi>,
    total: Long,
    isIncome: Boolean
) {
    val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentAlignment = Alignment.Center
    ) {
        if (data.isEmpty() || total == 0L) {
            Text(
                text = "Chưa có dữ liệu",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Biểu đồ với labels
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(220.dp)
                ) {
                    val strokeWidth = 40.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    var startAngle = -90f

                    data.forEach { stat ->
                        val sweepAngle = (stat.amount.toFloat() / total.toFloat()) * 360f
                        
                        drawArc(
                            color = stat.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(
                                centerX - radius,
                                centerY - radius
                            ),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        
                        startAngle += sweepAngle
                    }
                }
                
                // Số tiền ở giữa
                Text(
                    text = (if (isIncome) "" else "-") + formatter.format(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Labels xung quanh biểu đồ - hiển thị cho tất cả segments
                var currentAngle = -90f
                data.forEach { stat ->
                    val sweepAngle = (stat.amount.toFloat() / total.toFloat()) * 360f
                    val middleAngle = currentAngle + sweepAngle / 2
                    
                    // Chỉ hiển thị label nếu segment đủ lớn (> 5%)
                    if (stat.percentage > 5f) {
                        val radius = 150.dp
                        val angleRad = Math.toRadians(middleAngle.toDouble())
                        val x = (radius.value * kotlin.math.cos(angleRad)).dp
                        val y = (radius.value * kotlin.math.sin(angleRad)).dp
                        
                        Box(
                            modifier = Modifier.offset(x = x, y = y)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${stat.percentage.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stat.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    
                    currentAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
private fun CategoryStatItem(
    stat: CategoryStatUi,
    isIncome: Boolean,
    onClick: () -> Unit = {}
) {
    val formatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }
    val interactionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(stat.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stat.icon ?: stat.name.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            
            // Tên danh mục
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                // Progress bar
                LinearProgressIndicator(
                    progress = { stat.percentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = stat.color,
                    trackColor = stat.color.copy(alpha = 0.2f),
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Phần trăm và số tiền
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${stat.percentage}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = (if (isIncome) "" else "-") + formatter.format(stat.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
            )
        }
        
        Text(
            text = stat.count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun MonthYearPickerDialog(
    currentMonth: LocalDate,
    onDismiss: () -> Unit,
    onMonthSelected: (LocalDate) -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentMonth.year) }
    var selectedMonth by remember { mutableStateOf(currentMonth.monthValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Hủy", color = MaterialTheme.colorScheme.primary)
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
                
                TextButton(
                    onClick = {
                        onMonthSelected(LocalDate.of(selectedYear, selectedMonth, 1))
                    }
                ) {
                    Text("Xác nhận", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Grid tháng
                repeat(4) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { col ->
                            val month = row * 3 + col + 1
                            val isSelected = month == selectedMonth
                            
                            Surface(
                                onClick = { selectedMonth = month },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Th$month",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun YearPickerDialog(
    currentYear: Int,
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentYear) }
    val years = remember { (2020..2030).toList() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Hủy", color = MaterialTheme.colorScheme.primary)
                }
                
                Text(
                    text = "Chọn năm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                TextButton(
                    onClick = {
                        onYearSelected(selectedYear)
                    }
                ) {
                    Text("Xác nhận", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(years.chunked(3)) { rowYears ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowYears.forEach { year ->
                            val isSelected = year == selectedYear
                            
                            Surface(
                                onClick = { selectedYear = year },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = year.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        // Fill empty spaces if row has less than 3 items
                        repeat(3 - rowYears.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}