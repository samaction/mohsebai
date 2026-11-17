package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
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
fun OwnerDashboardDialog(
    ownerEmail: String = "putincenterfg12@gmail.com",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val transactions by db.transactionDao().getAllTransactions().collectAsState(initial = emptyList())
    val totalIncomeState by db.transactionDao().getTotalIncome().collectAsState(initial = 0.0)
    val totalExpenseState by db.transactionDao().getTotalExpense().collectAsState(initial = 0.0)

    val totalVolume = (totalIncomeState ?: 0.0) + (totalExpenseState ?: 0.0)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFF59E0B), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "لوحة تحكم مالك التطبيق",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المالك المعتمد: $ownerEmail",
                            fontSize = 12.sp,
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OwnerMetricCard(
                        title = "نظرة عامة على نشاط التطبيق للرئيس",
                        color = Color(0xFF10B981)
                    ) {
                        MetricRow("المالك الرسمي للتطبيق", ownerEmail)
                        MetricRow("حساب المبيعات والدفع الرئيسي", "USD ($) - الدولار الأمريكي")
                        MetricRow("إجمالي عدد المعاملات", "${transactions.size} معاملة")
                        MetricRow("حجم المعاملات المالي الفعلي", formatCurrency(totalVolume))
                        MetricRow("حالة قاعدة البيانات", "نشطة (Room & Firestore Active)")
                    }
                }

                item {
                    OwnerMetricCard(
                        title = "حالة اتصال الذكاء الاصطناعي وتلغرام بالمالك",
                        color = Color(0xFF6366F1)
                    ) {
                        MetricRow("الوكيل المحاسبي المربوط", "🟢 متصل بحساب $ownerEmail")
                        MetricRow("بوت تلغرام المربوط", "🟢 نشط (Token: 9274013467...)")
                        MetricRow("الذاكرة الدائمة السحابية", "🟢 متصلة بـ Firestore Memory")
                        MetricRow("طرق السداد المدعومة", "نقداً (Cash) / بنك / ذمة (Debt)")
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "أدوات تحكم المالك الفورية",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مزامنة السحابة", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("فحص الذاكرة", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerMetricCard(
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
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}