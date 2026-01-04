package com.example.expensemanager.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Month Picker dropdown widget
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPicker(
    selectedMonth: LocalDate,
    onMonthSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val months = remember { generateMonthOptions() }
    
    val formatter = remember {
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("vi-VN"))
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedMonth.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Chọn tháng",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(max = 300.dp)
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = month.format(formatter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = if (month == selectedMonth) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                )
            }
        }
    }
}

private fun generateMonthOptions(): List<LocalDate> {
    val current = LocalDate.now()
    // Cho phép chọn rộng hơn: 10 năm trước đến 10 năm sau
    return (-120..120).map { offset ->
        current.plusMonths(offset.toLong()).withDayOfMonth(1)
    }
}
