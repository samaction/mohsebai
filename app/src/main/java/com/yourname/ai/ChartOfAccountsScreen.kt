package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartOfAccountsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val accounts by db.accountingDao().getAllAccounts().collectAsState(initial = emptyList())

    // إذا كانت الشجرة فارغة، نقوم بتعبئتها بشجرة الحسابات المحاسبية المعتمدة
    LaunchedEffect(accounts) {
        if (accounts.isEmpty()) {
            scope.launch {
                db.accountingDao().insertAccount(AccountEntity(accountCode = "1010", accountName = "الصندوق والنقدية", category = AccountCategory.ASSET, currentBalance = 0.0))
                db.accountingDao().insertAccount(AccountEntity(accountCode = "1020", accountName = "حساب البنك", category = AccountCategory.ASSET, currentBalance = 0.0))
                db.accountingDao().insertAccount(AccountEntity(accountCode = "1030", accountName = "بضاعة المخزون", category = AccountCategory.ASSET, currentBalance = 0.0))
                db.accountingDao().insertAccount(AccountEntity(accountCode = "2010", accountName = "حسابات الموردين (التزامات)", category = AccountCategory.LIABILITY, currentBalance = 0.0))
                db.accountingDao().insertAccount(AccountEntity(accountCode = "3010", accountName = "رأس المال (حقوق الملكية)", category = AccountCategory.EQUITY, currentBalance = 0.0))
                db.accountingDao().insertAccount(AccountEntity(accountCode = "4010", accountName = "إيرادات المبيعات", category = AccountCategory.REVENUE, currentBalance = 0.0))
                db.accountingDao().insertAccount(AccountEntity(accountCode = "5010", accountName = "مصروفات تشغيلية وعامة", category = AccountCategory.EXPENSE, currentBalance = 0.0))
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "شجرة الحسابات المحاسبية الرسمية",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(accounts) { acc ->
                    AccountCardItem(account = acc)
                }
            }
        }
    }
}

@Composable
fun AccountCardItem(account: AccountEntity) {
    val categoryColor = when (account.category) {
        AccountCategory.ASSET -> Color(0xFF10B981)
        AccountCategory.LIABILITY -> Color(0xFFEF4444)
        AccountCategory.EQUITY -> Color(0xFF6366F1)
        AccountCategory.REVENUE -> Color(0xFF059669)
        AccountCategory.EXPENSE -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(categoryColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.accountCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = categoryColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = account.accountName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = account.category.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(text = formatCurrency(account.currentBalance), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = categoryColor)
        }
    }
}