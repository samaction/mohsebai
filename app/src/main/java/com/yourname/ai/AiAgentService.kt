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
 * وكيل الذكاء الاصطناعي المحاسبي والمؤسسي المتقدم (Enterprise AI Accounting Agent Engine)
 * يفهم الدورة المحاسبية الكاملة: شجرة الحسابات، فواتير البيع والشراء، جرد المخزون، والقيود المزدوجة
 */
class AiAgentService(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val memoryManager = FirebaseMemoryManager()

    val systemEnvironmentContext = """
        أنا المحاسب المستشار والوكيل الذكي المالي.
        البيئة المحاسبية المتقدمة:
        1. شجرة الحسابات (أصول، خصوم، حقوق ملكية، إيرادات، مصروفات).
        2. نظام القيود المحاسبية المزدوجة (مدين/دائن).
        3. إدارة المبيعات والمشتريات وفواتير المخزون والجرد المحاسبي.
        4. الميزانية العمومية، قائمة الدخل، والتسويات البنكية.
        5. ذاكرة دائمة سحابية متصلة بتلغرام وFirebase.
    """.trimIndent()

    suspend fun processUserCommand(input: String): AgentResponse = withContext(Dispatchers.IO) {
        val trimmedInput = input.trim()

        // 1. معالجة فواتير البيع والشراء والجرد
        if (trimmedInput.contains("فاتورة") || trimmedInput.contains("بيع") || trimmedInput.contains("شراء") || trimmedInput.contains("مخزون")) {
            val amount = extractAmount(trimmedInput) ?: 100.0
            val isSale = trimmedInput.contains("بيع") || trimmedInput.contains("مبيعات")
            val type = if (isSale) InvoiceType.SALE else InvoiceType.PURCHASE

            val invoice = InvoiceEntity(
                invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(5)}",
                type = type,
                customerOrSupplier = if (isSale) "عميل عام" else "مورد عام",
                totalAmount = amount
            )

            db.accountingDao().insertInvoice(invoice)

            // قيد محاسبي مزدوج تلقائي للفاتورة
            val journalEntry = JournalEntryEntity(
                entryNumber = "JV-${System.currentTimeMillis().toString().takeLast(5)}",
                debitAccountCode = if (isSale) "1010" else "5010", // النقدية أو تكلفة المبيعات
                creditAccountCode = if (isSale) "4010" else "1010", // الإيرادات أو النقدية
                amount = amount,
                statement = "قيد فاتورة ${if (isSale) "مبيعات" else "مشتريات"} رقم ${invoice.invoiceNumber}"
            )
            db.accountingDao().insertJournalEntry(journalEntry)

            memoryManager.saveMemorySnippet(
                key = "invoice_${System.currentTimeMillis()}",
                content = "فاتورة ${invoice.type.name} بقيمة $amount"
            )

            val reply = "🧾 تم تسجيل فاتورة ${if (isSale) "مبيعات" else "مشتريات"} رسمية بقيمة $amount وإنشاء قيدها المزدوج وتحديث حساباتك الماليّة تلقائياً!"
            return@withContext AgentResponse(replyMessage = reply, actionExecuted = true)
        }

        // 2. معالجة المعاملات المالية المباشرة (دخل / مصروف / دَين)
        val amount = extractAmount(trimmedInput)
        val isIncome = trimmedInput.contains("دخل") || trimmedInput.contains("قبضت") || trimmedInput.contains("راتب") || trimmedInput.contains("ربح")
        val isDebt = trimmedInput.contains("دين") || trimmedInput.contains("اقترضت")

        if (amount != null && amount > 0) {
            val type = when {
                isIncome -> TransactionType.INCOME
                isDebt -> TransactionType.DEBT
                else -> TransactionType.EXPENSE
            }
            val category = extractCategory(trimmedInput)
            val title = extractTitle(trimmedInput)

            val transaction = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                note = "تسجيل محاسبي عبر الوكيل الذكي"
            )

            db.transactionDao().insertTransaction(transaction)

            val reply = when (type) {
                TransactionType.INCOME -> "✅ تم تسجيل الإيراد الرسمي بقيمة $amount وتحديث حساب الأرباح."
                TransactionType.DEBT -> "🤝 تم تسجيل الدَّين بقيمة $amount وترحيله للالتزامات."
                TransactionType.EXPENSE -> "💸 تم تسجيل المصروف بقيمة $amount وقيده محاسبياً تحت فئة $category."
            }

            return@withContext AgentResponse(replyMessage = reply, actionExecuted = true, newTransaction = transaction)
        }

        // 3. الاستعلام عن التقارير الماليّة وشجرة الحسابات والميزانية
        if (trimmedInput.contains("شجرة") || trimmedInput.contains("ميزانية") || trimmedInput.contains("تقرير") || trimmedInput.contains("أرباح")) {
            val reply = "📊 يمكنك مشاهدة شجرة الحسابات، ميزان المراجعة، وقائمة الدخل الختامية بدقة من أقسام التطبيق المحاسبية!"
            return@withContext AgentResponse(replyMessage = reply, actionExecuted = false)
        }

        val defaultReply = "🤖 أنا وكيلك المحاسبي الذكي للمؤسسات. يمكنك إملائي بمرونة مثل: 'فاتورة بيع 500' أو 'صرفت 120 صيانة' أو 'جرد مخزون' وسأقوم بتنفيذ القيد المزدوج والترحيل المباشر!"
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
            text.contains("فاتورة") || text.contains("كهرباء") || text.contains("نت") || text.contains("سكن") -> "فواتير وسكن"
            text.contains("بنزين") || text.contains("مواصلات") || text.contains("سيارة") -> "مواصلات"
            text.contains("راتب") || text.contains("مبيعات") || text.contains("مشروع") -> "راتب ومبيعات"
            text.contains("شراء") || text.contains("تسوق") -> "تسوق"
            else -> "عام"
        }
    }

    private fun extractTitle(text: String): String {
        return text.take(35)
    }
}