package com.yourname.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: TransactionType, category: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("عام") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("طعام وشراب", "فواتير وسكن", "مواصلات", "تسوق", "راتب ومبيعات", "أخرى")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "إضافة معاملة مالية جديدة",
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium
            )

            // اختيار نوع المعاملة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("مصروف") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("دخل") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.DEBT,
                    onClick = { selectedType = TransactionType.DEBT },
                    label = { Text("دَين") },
                    modifier = Modifier.weight(1f)
                )
            }

            // حقل عنوان المعاملة
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان المعاملة (مثلاً: فاتورة الكهرباء)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // حقل المبلغ
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("المبلغ (مثلاً: 150)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // اختيار الفئة
            Text("الفئة:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.drop(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            // رسالة خطأ عند الإدخال
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            // زر الحفظ
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMessage = "يرجى كتابة عنوان المعاملة"
                    } else if (amount == null || amount <= 0) {
                        errorMessage = "يرجى كتابة مبلغ صحيح أكبر من 0"
                    } else {
                        onSave(title, amount, selectedType, selectedCategory, note)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ المعاملة", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}