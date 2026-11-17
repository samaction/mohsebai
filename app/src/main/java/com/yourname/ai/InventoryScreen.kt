package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
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
fun InventoryScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val inventoryItems by db.accountingDao().getAllInventoryItems().collectAsState(initial = emptyList())
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
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("جرد وإدارة المخزون", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("الأصناف والكميات وأسعار التكلفة والبيع", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("صنف جديد")
            }
        }

        if (inventoryItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("المخزن فارغ حالياً. اضغط صنف جديد لإضافة بضاعة للجرد", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inventoryItems) { item ->
                    InventoryCardItem(
                        item = item,
                        onDelete = {
                            scope.launch {
                                db.accountingDao().deleteInventoryItem(item)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddInventoryItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, code, category, qty, buyPrice, sellPrice ->
                scope.launch {
                    db.accountingDao().insertInventoryItem(
                        InventoryItemEntity(
                            itemCode = code,
                            itemName = name,
                            category = category,
                            quantityInStock = qty,
                            unitPurchasePrice = buyPrice,
                            unitSellingPrice = sellPrice
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun InventoryCardItem(
    item: InventoryItemEntity,
    onDelete: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = item.itemName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "كود: ${item.itemCode} • المتبقي: ${item.quantityInStock.toInt()} قطعة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "سعر البيع", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = formatCurrency(item.unitSellingPrice), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryItemDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عام") }
    var qtyText by remember { mutableStateOf("") }
    var buyPriceText by remember { mutableStateOf("") }
    var sellPriceText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("إضافة صنف مخزون جديد", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم الصنف / البضاعة") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود الصنف/الباركود") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("الكمية بالمخزن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = buyPriceText,
                    onValueChange = { buyPriceText = it },
                    label = { Text("سعر التكلفة ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellPriceText,
                    onValueChange = { sellPriceText = it },
                    label = { Text("سعر البيع ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    val qty = qtyText.toDoubleOrNull() ?: 0.0
                    val buyPrice = buyPriceText.toDoubleOrNull() ?: 0.0
                    val sellPrice = sellPriceText.toDoubleOrNull() ?: 0.0
                    val generatedCode = if (code.isBlank()) "ITEM-${System.currentTimeMillis().toString().takeLast(4)}" else code
                    if (name.isNotBlank()) {
                        onSave(name, generatedCode, category, qty, buyPrice, sellPrice)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("حفظ الصنف بالمخزون", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}