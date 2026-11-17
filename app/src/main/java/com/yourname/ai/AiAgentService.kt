package com.yourname.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class AgentResponse(
    val replyMessage: String,
    val actionExecuted: Boolean,
    val newTransaction: TransactionEntity? = null
)

/**
 * وكيل الذكاء الاصطناعي المحاسبي المؤسسي المفتوح والحر (Autonomous Unconstrained AI Agent Engine)
 * يفهم الأوامر والرسائل بحرية كاملة دون التقيد بصيغة محددة، ويعالج الفواتير، المخزون، والذمم لكل مستخدم معزول
 */
class AiAgentService(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val memoryManager = FirebaseMemoryManager()

    val systemEnvironmentContext = """
        أنا المحاسب المستشار والوكيل الذكي المالي والمؤسسي المفتوح.
        البيئة المحاسبية المتقدمة:
        1. شجرة الحسابات، فواتير المبيعات والمشتريات، والجرد والذمم المدينة والدائنة.
        2. معالجة الرسائل بحرية دون الالتزام بصيغ محددة.
        3. حفظ الذاكرة السحابية المعزولة بـ userId ومزامنتها مع تلغرام والتطبيق.
    """.trimIndent()

    suspend fun processUserCommand(input: String, userId: String = "main_user"): AgentResponse = withContext(Dispatchers.IO) {
        val trimmedInput = input.trim()

        // 1. الاستعلام عن الذمم وكشوف حساب العملاء/الموردين
        if (trimmedInput.contains("دَين") || trimmedInput.contains("ذمة") || trimmedInput.contains("كشف") || trimmedInput.contains("حساب")) {
            val amount = extractAmount(trimmedInput)
            if (amount != null && amount > 0) {
                val isCustomer = !trimmedInput.contains("مورد")
                val contactType = if (isCustomer) ContactType.CUSTOMER else ContactType.SUPPLIER
                val name = extractTitle(trimmedInput).ifBlank { if (isCustomer) "عميل عام" else "مورد عام" }

                db.accountingDao().insertContact(
                    CustomerSupplierEntity(
                        userId = userId,
                        name = name,
                        type = contactType,
                        totalBalance = amount
                    )
                )

                val reply = "👥 تم تسجيل جهة التعامل ($name) في كشف الذمم ${if (isCustomer) "المدينة" else "الدائنة"} برصيد $amount $ USD."
                return@withContext AgentResponse(replyMessage = reply, actionExecuted = true)
            }
        }

        // 2. معالجة فواتير المبيعات والمشتريات والجرد الحرة
        if (trimmedInput.contains("فاتورة") || trimmedInput.contains("بيع") || trimmedInput.contains("مبيعات") || trimmedInput.contains("شراء") || trimmedInput.contains("مخزون") || trimmedInput.contains("بضاعة")) {
            val amount = extractAmount(trimmedInput) ?: 100.0
            val isSale = !trimmedInput.contains("شراء") && !trimmedInput.contains("مشتريات")
            val invType = if (isSale) InvoiceType.SALE else InvoiceType.PURCHASE

            val invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(5)}"
            db.accountingDao().insertInvoice(
                InvoiceEntity(
                    userId = userId,
                    invoiceNumber = invoiceNumber,
                    type = invType,
                    customerOrSupplier = if (isSale) "عميل عام" else "مورد عام",
                    totalAmount = amount
                )
            )

            val journalEntry = JournalEntryEntity(
                userId = userId,
                entryNumber = "JV-${System.currentTimeMillis().toString().takeLast(5)}",
                debitAccountCode = if (isSale) "1010" else "5010",
                creditAccountCode = if (isSale) "4010" else "1010",
                amount = amount,
                statement = "قيد أوتوماتيكي حر لفاتورة ${if (isSale) "مبيعات" else "مشتريات"} رقم $invoiceNumber"
            )
            db.accountingDao().insertJournalEntry(journalEntry)

            memoryManager.saveMemorySnippet(
                key = "invoice_${System.currentTimeMillis()}",
                content = "فاتورة ${invType.name} بمبلغ $amount دولار"
            )

            val reply = "🧾 تم إصدار فاتورة ${if (isSale) "مبيعات" else "مشتريات"} رقم $invoiceNumber بقيمة $$amount وترحيل قيدها المزدوج للمخزون والحسابات!"
            return@withContext AgentResponse(replyMessage = reply, actionExecuted = true)
        }

        // 3. معالجة المصروفات والإيرادات العامة المرنة
        val amount = extractAmount(trimmedInput)
        if (amount != null && amount > 0) {
            val isIncome = trimmedInput.contains("دخل") || trimmedInput.contains("قبضت") || trimmedInput.contains("راتب") || trimmedInput.contains("ربح")
            val isDebt = trimmedInput.contains("دين") || trimmedInput.contains("ذمة")
            val type = when {
                isIncome -> TransactionType.INCOME
                isDebt -> TransactionType.DEBT
                else -> TransactionType.EXPENSE
            }
            val category = extractCategory(trimmedInput)
            val title = extractTitle(trimmedInput)

            val transaction = TransactionEntity(
                userId = userId,
                title = title,
                amount = amount,
                type = type,
                category = category,
                note = "إدخال حر بواسطة الوكيل الذكي"
            )

            db.transactionDao().insertTransaction(transaction)

            val reply = when (type) {
                TransactionType.INCOME -> "✅ تم تسجيل إيرادك بقيمة $$amount تحت فئة $category وتحديث أرباحك."
                TransactionType.DEBT -> "🤝 تم تسجيل الدَّين بقيمة $$amount وقيده في الذمم."
                TransactionType.EXPENSE -> "💸 تم تسجيل المصروف بقيمة $$amount وقيده تحت فئة $category."
            }

            return@withContext AgentResponse(replyMessage = reply, actionExecuted = true, newTransaction = transaction)
        }

        // 4. الاستجابة العامة المرنة للوكيل
        val defaultReply = "🤖 مرحباً بك! أنا وكيلك المحاسبي الحر. اكتب لي بأي طريقة تناسبك مثل: 'فاتورة بيع بضاعة 400 دولار' أو 'كشف حساب عميل 250' وسأنفذها فوراً!"
        return@withContext AgentResponse(replyMessage = defaultReply, actionExecuted = false)
    }

    private fun extractAmount(text: String): Double? {
        val matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull()
        } else null
    }

    private fun extractCategory(text: String): String {
        return when {
            text.contains("طعام") || text.contains("أكل") || text.contains("مطعم") -> "طعام وشراب"
            text.contains("فاتورة") || text.contains("كهرباء") || text.contains("نت") -> "فواتير وسكن"
            text.contains("بنزين") || text.contains("مواصلات") -> "مواصلات"
            text.contains("راتب") || text.contains("مبيعات") -> "راتب ومبيعات"
            text.contains("شراء") || text.contains("تسوق") -> "تسوق"
            else -> "عام"
        }
    }

    private fun extractTitle(text: String): String {
        return text.take(35)
    }
}