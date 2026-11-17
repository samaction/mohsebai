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

// 2. جدول شجرة الحسابات المعزولة حسب المستخدم (Chart of Accounts)
@Entity(tableName = "chart_of_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "main_user",  // رمز المستخدم لعزل البيانات Multi-Tenancy
    val accountCode: String,           // رقم الحساب (مثلاً: 1010)
    val accountName: String,           // اسم الحساب (مثلاً: الصندوق، البنك، مبيعات)
    val category: AccountCategory,      // الفئة الرئيسية
    val currentBalance: Double = 0.0    // الرصيد الحالي
)

// 3. جدول القيود المحاسبية المزدوجة (Double-Entry Journal Entries)
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "main_user",  // رمز المستخدم
    val entryNumber: String,           // رقم القيد
    val date: Long = System.currentTimeMillis(),
    val debitAccountCode: String,      // الحساب المدين
    val creditAccountCode: String,     // الحساب الدائن
    val amount: Double,                // المبلغ بالدولار USD
    val statement: String,             // البيان/الوصف
    val isReconciled: Boolean = false    // التسوية البنكية
)

// 4. جدول إدارة المخزون والجرد الأصناف (Inventory & Stock Control)
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "main_user",  // رمز المستخدم
    val itemCode: String,
    val itemName: String,
    val category: String = "عام",
    val quantityInStock: Double,       // الكمية المتاحة في المخزن
    val unitPurchasePrice: Double,     // سعر التكلفة/الشراء بالدولار
    val unitSellingPrice: Double       // سعر البيع بالدولار
)

// 5. نوع الفاتورة وطبيعة العميل
enum class InvoiceType {
    SALE,    // فاتورة مبيعات
    PURCHASE // فاتورة مشتريات
}

// 6. جدول فواتير المبيعات والمشتريات (Invoices)
@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "main_user",         // رمز المستخدم
    val invoiceNumber: String,
    val type: InvoiceType,
    val customerOrSupplier: String,        // اسم العميل أو المورد
    val paymentMethod: PaymentMethod = PaymentMethod.CASH, // نقداً / ذمة / بنك
    val totalAmount: Double,
    val taxAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)

// 7. جدول بنود الفاتورة التفصيلية (Invoice Items)
@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double
)

// 8. نوع جهة التعامل (عميل أو مورد)
enum class ContactType {
    CUSTOMER, // عميل (ذمم مدينة)
    SUPPLIER  // مورد (ذمم دائنة)
}

// 9. جدول كشوفات حسابات العملاء والموردين (AR & AP Contacts Ledger)
@Entity(tableName = "contacts")
data class CustomerSupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "main_user",  // رمز المستخدم
    val name: String,                  // اسم العميل أو المورد
    val phone: String = "",
    val type: ContactType,             // عميل أو مورد
    val totalBalance: Double = 0.0      // رصيد الذمم المستحقة
)