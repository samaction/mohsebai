package com.yourname.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.yourname.ai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MohasibTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by remember { mutableStateOf(false) }

                    if (!isLoggedIn) {
                        LoginScreen(onLoginSuccess = { isLoggedIn = true })
                    } else {
                        MainDashboardScreen(onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            isLoggedIn = false
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val transactions by db.transactionDao().getAllTransactions().collectAsState(initial = emptyList())
    val totalIncomeState by db.transactionDao().getTotalIncome().collectAsState(initial = 0.0)
    val totalExpenseState by db.transactionDao().getTotalExpense().collectAsState(initial = 0.0)

    val totalIncome = totalIncomeState ?: 0.0
    val totalExpense = totalExpenseState ?: 0.0
    val netBalance = totalIncome - totalExpense

    var selectedFilter by remember { mutableStateOf("الكل") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAiChat by remember { mutableStateOf(false) }
    var showChartOfAccounts by remember { mutableStateOf(false) }
    var showFinancialReports by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "دخل" -> transactions.filter { it.type == TransactionType.INCOME }
            "مصروف" -> transactions.filter { it.type == TransactionType.EXPENSE }
            "دَين" -> transactions.filter { it.type == TransactionType.DEBT }
            else -> transactions
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "محاسب AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showChartOfAccounts = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "شجرة الحسابات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showFinancialReports = true }) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "الميزانية والتقارير",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showAiChat = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "الوكيل الذكي",
                            tint = Color(0xFF6366F1)
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { showAiChat = true },
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "المساعد الذكي")
                }
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة معاملة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // بنر حالة اتصال وكلاء الذكاء الاصطناعي بالذاكرة وبوت تلغرام
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "وكلاء الذكاء الاصطناعي متصلون بالذاكرة وتلغرام",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }
                    Text(
                        text = "v1.0 Enterprise",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // بنر التحديث التلقائي الذاتي
            AutoUpdateCheckBanner(onUpdateClick = { })

            // كارت الرصيد التدرجي الفاخر (Gradient Hero Card)
            GradientBalanceCard(
                netBalance = netBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )

            // شريط تصفية وتحديد المعاملات السريعة
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل المعاملات",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredTransactions.size} معاملة",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // قائمة المعاملات المحدثة
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد معاملات في هذه الفئة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { item ->
                        ModernTransactionCard(
                            transaction = item,
                            onDelete = {
                                scope.launch {
                                    db.transactionDao().deleteTransaction(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AddTransactionBottomSheet(
            onDismiss = { showBottomSheet = false },
            onSave = { title, amount, type, category, note ->
                scope.launch {
                    db.transactionDao().insertTransaction(
                        TransactionEntity(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            note = note
                        )
                    )
                }
            }
        )
    }

    if (showAiChat) {
        AiChatDialog(onDismiss = { showAiChat = false })
    }

    if (showChartOfAccounts) {
        ChartOfAccountsDialog(onDismiss = { showChartOfAccounts = false })
    }

    if (showFinancialReports) {
        FinancialReportsDialog(onDismiss = { showFinancialReports = false })
    }
}

@Composable
fun GradientBalanceCard(
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F766E))
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إجمالي الصافي المالي",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "محدث الآن", fontSize = 11.sp, color = Color(0xFF34D399))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatCurrency(netBalance),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.White.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStatItem(
                        title = "الدخل",
                        amount = totalIncome,
                        color = Color(0xFF34D399),
                        icon = Icons.Default.ArrowUpward
                    )
                    SummaryStatItem(
                        title = "المصروفات",
                        amount = totalExpense,
                        color = Color(0xFFF87171),
                        icon = Icons.Default.ArrowDownward
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(
    title: String,
    amount: Double,
    color: Color,
    icon: ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.18f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(text = formatCurrency(amount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("الكل", "دخل", "مصروف", "دَين")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ModernTransactionCard(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isDebt = transaction.type == TransactionType.DEBT

    val accentColor = when {
        isIncome -> Color(0xFF10B981)
        isDebt -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val icon = when {
        isIncome -> Icons.Default.TrendingUp
        isDebt -> Icons.Default.Handshake
        else -> Icons.Default.TrendingDown
    }

    val prefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(accentColor.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.category} • ${formatDate(transaction.date)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$prefix${formatCurrency(transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("ar", "SA"))
    return formatter.format(amount)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}