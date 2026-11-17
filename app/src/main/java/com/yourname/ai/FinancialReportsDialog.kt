package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val totalRevenues by db.accountingDao().getTotalRevenues().collectAsState(initial = 0.0)
    val totalExpenses by db.accountingDao().getTotalExpenses().collectAsState(initial = 0.0)
    val totalAssets by db.accountingDao().getTotalAssets().collectAsState(initial = 0.0)
    val totalLiabilities by db.accountingDao().getTotalLiabilities().collectAsState(initial = 0.0)

    val revenues = totalRevenues ?: 0.0
    val expenses = totalExpenses ?: 0.0
    val assets = totalAssets ?: 0.0
    val liabilities = totalLiabilities ?: 0.0
    val netProfit = revenues - expenses

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "التقارير الماليّة والميزانية الختامية",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

            // 1. كارت قائمة الدخل (Income Statement / P&L)
            ReportCard(
                title = "قائمة الدخل والأرباح والخسائر (Income Statement)",
                color = Color(0xFF10B981)
            ) {
                ReportRowItem("إجمالي الإيرادات والمبيعات", revenues, Color(0xFF10B981))
                ReportRowItem("إجمالي المصروفات والتكاليف", expenses, Color(0xFFEF4444))
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                ReportRowItem("صافي الأرباح / الخسائر الختامي", netProfit, if (netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444), isBold = true)
            }

            // 2. كارت الميزانية العمومية الختامية (Balance Sheet)
            ReportCard(
                title = "الميزانية العمومية الختامية (Balance Sheet)",
                color = Color(0xFF6366F1)
            ) {
                ReportRowItem("مجموع الأصول (Assets)", assets, Color(0xFF10B981))
                ReportRowItem("مجموع الالتزامات/الخصوم (Liabilities)", liabilities, Color(0xFFEF4444))
                ReportRowItem("حقوق الملكية والأرباح المبقاة", assets - liabilities, Color(0xFF6366F1), isBold = true)
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ReportRowItem(
    label: String,
    value: Double,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = formatCurrency(value), fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = color)
    }
}