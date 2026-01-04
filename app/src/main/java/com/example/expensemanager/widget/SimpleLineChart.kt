package com.example.expensemanager.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.feature.home.DayData
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 * Simple Line Chart cho hiển thị thu-chi theo ngày
 */
@Composable
fun SimpleLineChart(
    data: List<DayData>,
    modifier: Modifier = Modifier,
    incomeColor: Color = Color(0xFF4CAF50),
    expenseColor: Color = Color(0xFFF44336)
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có dữ liệu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    val rawMax = data.maxOfOrNull { max(it.income, it.expense) } ?: 0L
    val minValue = 0L
    val maxValue = if (rawMax <= minValue) minValue + 1 else rawMax
    
    Column(modifier = modifier) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = incomeColor, label = "Thu nhập")
            Spacer(modifier = Modifier.width(24.dp))
            LegendItem(color = expenseColor, label = "Chi tiêu")
        }
        
        // Chart + Y axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 8.dp)
        ) {
            val gridLines = 5
            val stepValue = maxValue.toFloat() / gridLines

            // Y-axis value labels
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                for (i in gridLines downTo 0) {
                    val value = (stepValue * i).toLong()
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
            val width = size.width
            val height = size.height
            val padding = 40f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2
            
            // Draw grid lines
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = padding + (chartHeight / gridLines) * i
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
            
            // Prepare data points
            val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
            
            // Draw expense line
            val expensePath = Path()
            data.forEachIndexed { index, dayData ->
                val x = padding + index * stepX
                val normalizedExpense = (dayData.expense - minValue).toFloat() / (maxValue - minValue)
                val y = padding + chartHeight - (normalizedExpense * chartHeight)
                
                if (index == 0) {
                    expensePath.moveTo(x, y)
                } else {
                    expensePath.lineTo(x, y)
                }
            }
            
            drawPath(
                path = expensePath,
                color = expenseColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round
                )
            )
            
            // Draw income line
            val incomePath = Path()
            data.forEachIndexed { index, dayData ->
                val x = padding + index * stepX
                val normalizedIncome = (dayData.income - minValue).toFloat() / (maxValue - minValue)
                val y = padding + chartHeight - (normalizedIncome * chartHeight)
                
                if (index == 0) {
                    incomePath.moveTo(x, y)
                } else {
                    incomePath.lineTo(x, y)
                }
            }
            
            drawPath(
                path = incomePath,
                color = incomeColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round
                )
            )
            
            // Draw data points
            data.forEachIndexed { index, dayData ->
                val x = padding + index * stepX
                
                // Income point
                val normalizedIncome = (dayData.income - minValue).toFloat() / (maxValue - minValue)
                val yIncome = padding + chartHeight - (normalizedIncome * chartHeight)
                drawCircle(
                    color = incomeColor,
                    radius = 4f,
                    center = Offset(x, yIncome)
                )
                
                // Expense point
                val normalizedExpense = (dayData.expense - minValue).toFloat() / (maxValue - minValue)
                val yExpense = padding + chartHeight - (normalizedExpense * chartHeight)
                drawCircle(
                    color = expenseColor,
                    radius = 4f,
                    center = Offset(x, yExpense)
                )
            }
            }
        }
        
        // X-axis labels (show only first, middle, last)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (data.isNotEmpty()) {
                val formatter = DateTimeFormatter.ofPattern("dd/MM")
                Text(
                    text = data.first().date.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (data.size > 2) {
                    Text(
                        text = data[data.size / 2].date.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = data.last().date.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
