package com.yourname.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. أنواع الحسابات المحاسبية الرئيسية (Account Types)
enum class AccountCategory {
    ASSET,       // أصول (نقدية، بنك، مخزون، ذمم مدينة)
    LIABILITY,   // خصوم/التزامات (ذمم دائنة، قروض)
    EQUITY,      // حقوق ملكية (رأس المال، أرباح مبقاة)
    REVENUE,     // إيرادات/مبيعات
    EXPENSE      // مصروفات/تكاليف
}

// 2. جدول شجرة الحسابات (Chart of Accounts)
@Entity(tableName = "chart_of_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountCode: String,      // رقم الحساب (مثلاً: 1010)
    val accountName: String,      // اسم الحساب (مثلاً: الصندوق، البنك، مبيعات)
    val category: AccountCategory, // الفئة الرئيسية
    val currentBalance: Double = 0.0 // الرصيد الحالي
)

// 3. جدول القيود المحاسبية المزدوجة (Double-Entry Journal Entries)
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entryNumber: String,        // رقم القيد
    val date: Long = System.currentTimeMillis(),
    val debitAccountCode: String,   // الحساب المدين
    val creditAccountCode: String,  // الحساب الدائن
    val amount: Double,             // المبلغ
    val statement: String,          // البيان/الوصف
    val isReconciled: Boolean = false // التسوية البنكية
)

// 4. جدول إدار المخزون والجرد (Inventory & Stock Management)
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemCode: String,
    val itemName: String,
    val quantityInStock: Double,  // الكمية بالمخزون
    val unitPurchasePrice: Double,// سعر الشراء
    val unitSellingPrice: Double  // سعر البيع
)

// 5. نوع الفاتورة
enum class InvoiceType {
    SALE,    // بيع
    PURCHASE // شراء
}

// 6. جدول فواتير المبيعات والمشتريات (Invoices)
@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String,
    val type: InvoiceType,
    val customerOrSupplier: String, // اسم العملاء/الموردين
    val totalAmount: Double,
    val date: Long = System.currentTimeMillis()
)