package com.yourname.ai

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountingDao {

    // --- شجرة الحسابات (معزولة بـ userId) ---
    @Query("SELECT * FROM chart_of_accounts WHERE userId = :userId ORDER BY accountCode ASC")
    fun getAllAccounts(userId: String = "main_user"): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("UPDATE chart_of_accounts SET currentBalance = currentBalance + :amount WHERE accountCode = :accountCode AND userId = :userId")
    suspend fun updateAccountBalance(accountCode: String, amount: Double, userId: String = "main_user")

    // --- القيود المحاسبية المزدوجة ---
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllJournalEntries(userId: String = "main_user"): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    // --- المخزون والجرد ---
    @Query("SELECT * FROM inventory_items WHERE userId = :userId ORDER BY itemName ASC")
    fun getAllInventoryItems(userId: String = "main_user"): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantityInStock = quantityInStock + :quantityDelta WHERE itemCode = :itemCode AND userId = :userId")
    suspend fun updateStockQuantity(itemCode: String, quantityDelta: Double, userId: String = "main_user")

    // --- الفواتير والتفاصيل ---
    @Query("SELECT * FROM invoices WHERE userId = :userId ORDER BY date DESC")
    fun getAllInvoices(userId: String = "main_user"): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getInvoiceItems(invoiceId: Int): Flow<List<InvoiceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    // --- العملاء والموردين والذمم (AR & AP) ---
    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY name ASC")
    fun getAllContacts(userId: String = "main_user"): Flow<List<CustomerSupplierEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND type = :type ORDER BY name ASC")
    fun getContactsByType(type: ContactType, userId: String = "main_user"): Flow<List<CustomerSupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: CustomerSupplierEntity)

    @Query("UPDATE contacts SET totalBalance = totalBalance + :balanceDelta WHERE id = :contactId AND userId = :userId")
    suspend fun updateContactBalance(contactId: Int, balanceDelta: Double, userId: String = "main_user")

    // --- التقارير الماليّة والميزانية المعزولة ---
    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'REVENUE' AND userId = :userId")
    fun getTotalRevenues(userId: String = "main_user"): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'EXPENSE' AND userId = :userId")
    fun getTotalExpenses(userId: String = "main_user"): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'ASSET' AND userId = :userId")
    fun getTotalAssets(userId: String = "main_user"): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'LIABILITY' AND userId = :userId")
    fun getTotalLiabilities(userId: String = "main_user"): Flow<Double?>
}