package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersDebtsScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(ContactType.CUSTOMER) }
    val contacts by db.accountingDao().getContactsByType(selectedTab).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("إدارة الذمم والعملاء والموردين", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("متابعة مستحقات الذمم المدينة والدائنة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("جهة جديدة")
            }
        }

        // التبديل بين العملاء والموردين
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedTab == ContactType.CUSTOMER,
                onClick = { selectedTab = ContactType.CUSTOMER },
                label = { Text("العملاء (الذمم المدينة AR)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedTab == ContactType.SUPPLIER,
                onClick = { selectedTab = ContactType.SUPPLIER },
                label = { Text("الموردون (الذمم الدائنة AP)") },
                modifier = Modifier.weight(1f)
            )
        }

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد سجلات ذمم مسجلة بعد في هذه القائمة", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(contacts) { contact ->
                    ContactCardItem(contact = contact)
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            defaultType = selectedTab,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, type, balance ->
                scope.launch {
                    db.accountingDao().insertContact(
                        CustomerSupplierEntity(
                            name = name,
                            phone = phone,
                            type = type,
                            totalBalance = balance
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun ContactCardItem(contact: CustomerSupplierEntity) {
    val isCustomer = contact.type == ContactType.CUSTOMER
    val accentColor = if (isCustomer) Color(0xFF10B981) else Color(0xFFF59E0B)

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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (contact.phone.isNotBlank()) contact.phone else "بدون رقم هاتف", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "الرصيد المستحق", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = formatCurrency(contact.totalBalance), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    defaultType: ContactType,
    onDismiss: () -> Unit,
    onSave: (String, String, ContactType, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(defaultType) }
    var balanceText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("إضافة عميل أو مورد جديد", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == ContactType.CUSTOMER,
                    onClick = { type = ContactType.CUSTOMER },
                    label = { Text("عميل (ذمة مدينة)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = type == ContactType.SUPPLIER,
                    onClick = { type = ContactType.SUPPLIER },
                    label = { Text("مورد (ذمة دائنة)") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("الرصيد الافتتاحي المستحق ($ USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onSave(name, phone, type, balance)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("حفظ بيانات الجهة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}