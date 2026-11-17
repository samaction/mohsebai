package com.yourname.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PaymentMethod {
    CASH,   // نقداً (الصندوق)
    CREDIT, // ذمة / دَين
    BANK    // بنك
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: TransactionType, category: String, paymentMethod: PaymentMethod, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedCategory by remember { mutableStateOf("مبيعات") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("مبيعات واستثمار", "طعام وشراب", "فواتير وسكن", "مواصلات", "تسوق ومشتريات", "عام")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "إضافة حركة مالية جديدة",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            // 1. اختيار نوع الحركة (إيراد/بيع - مصروف/شراء - دَين)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME; selectedCategory = "مبيعات" },
                    label = { Text("بيع / إيراد (+)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE; selectedCategory = "تسوق ومشتريات" },
                    label = { Text("شراء / مصروف (-)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.DEBT,
                    onClick = { selectedType = TransactionType.DEBT },
                    label = { Text("دَين / ذمة") },
                    modifier = Modifier.weight(1f)
                )
            }

            // 2. اختيار طريقة السداد والتحصيل (نقداً - ذمة - بنك)
            Text("طريقة التحصيل / السداد:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPaymentMethod == PaymentMethod.CASH,
                    onClick = { selectedPaymentMethod = PaymentMethod.CASH },
                    label = { Text("💵 نقداً (الصندوق)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedPaymentMethod == PaymentMethod.CREDIT,
                    onClick = { selectedPaymentMethod = PaymentMethod.CREDIT },
                    label = { Text("📝 ذمة (دَين)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedPaymentMethod == PaymentMethod.BANK,
                    onClick = { selectedPaymentMethod = PaymentMethod.BANK },
                    label = { Text("🏦 بنك (تحويل)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // حقل عنوان العملية
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الحركة (مثلاً: بيع بضاعة / فاتورة هاتف)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // حقل المبلغ بالدولار USD
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("المبلغ بالدولار USD ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // رسالة خطأ عند الإدخال
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            // زر الحفظ وتطبيق القيد المحاسبي
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMessage = "يرجى كتابة عنوان الحركة"
                    } else if (amount == null || amount <= 0) {
                        errorMessage = "يرجى كتابة مبلغ صحيح بالدولار ($)"
                    } else {
                        onSave(title, amount, selectedType, selectedCategory, selectedPaymentMethod, note)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("حفظ وترحيل الحركة المحاسبية", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}