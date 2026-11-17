package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SalesInvoicesScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val invoices by db.accountingDao().getAllInvoices().collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("فواتير المبيعات والمشتريات", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("إصدار ومعاينة الفواتير الرسمية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("فاتورة جديدة")
            }
        }

        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد فواتير صادرة بعد", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(invoices) { inv ->
                    InvoiceCardItem(invoice = inv)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateInvoiceDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { invType, customerName, paymentMethod, amount ->
                scope.launch {
                    val invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(5)}"
                    db.accountingDao().insertInvoice(
                        InvoiceEntity(
                            invoiceNumber = invoiceNumber,
                            type = invType,
                            customerOrSupplier = customerName,
                            paymentMethod = paymentMethod,
                            totalAmount = amount
                        )
                    )
                    // قيد محاسبي مزدوج تلقائي
                    val isSale = invType == InvoiceType.SALE
                    val journalEntry = JournalEntryEntity(
                        entryNumber = "JV-${System.currentTimeMillis().toString().takeLast(5)}",
                        debitAccountCode = if (paymentMethod == PaymentMethod.CASH) "1010" else (if (paymentMethod == PaymentMethod.BANK) "1020" else "2010"),
                        creditAccountCode = if (isSale) "4010" else "1010",
                        amount = amount,
                        statement = "قيد فاتورة ${if (isSale) "مبيعات" else "مشتريات"} رقم $invoiceNumber ($customerName)"
                    )
                    db.accountingDao().insertJournalEntry(journalEntry)
                }
            }
        )
    }
}

@Composable
fun InvoiceCardItem(invoice: InvoiceEntity) {
    val isSale = invoice.type == InvoiceType.SALE
    val accentColor = if (isSale) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = invoice.invoiceNumber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${invoice.customerOrSupplier} • ${invoice.paymentMethod.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(text = formatCurrency(invoice.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onSave: (InvoiceType, String, PaymentMethod, Double) -> Unit
) {
    var invType by remember { mutableStateOf(InvoiceType.SALE) }
    var customerName by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("إصدار فاتورة جديدة", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = invType == InvoiceType.SALE,
                    onClick = { invType = InvoiceType.SALE },
                    label = { Text("فاتورة مبيعات") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = invType == InvoiceType.PURCHASE,
                    onClick = { invType = InvoiceType.PURCHASE },
                    label = { Text("فاتورة مشتريات") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text(if (invType == InvoiceType.SALE) "اسم العميل" else "اسم المورد") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("طريقة السداد:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = paymentMethod == PaymentMethod.CASH,
                    onClick = { paymentMethod = PaymentMethod.CASH },
                    label = { Text("نقداً") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = paymentMethod == PaymentMethod.CREDIT,
                    onClick = { paymentMethod = PaymentMethod.CREDIT },
                    label = { Text("آجل/ذمة") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = paymentMethod == PaymentMethod.BANK,
                    onClick = { paymentMethod = PaymentMethod.BANK },
                    label = { Text("بنك") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("إجمالي الفاتورة ($ USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (customerName.isNotBlank() && amount > 0) {
                        onSave(invType, customerName, paymentMethod, amount)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("حفظ وإصدار الفاتورة الرسميّة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}