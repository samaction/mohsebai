package com.yourname.ai

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountingDao {

    // --- شجرة الحسابات ---
    @Query("SELECT * FROM chart_of_accounts ORDER BY accountCode ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("UPDATE chart_of_accounts SET currentBalance = currentBalance + :amount WHERE accountCode = :accountCode")
    suspend fun updateAccountBalance(accountCode: String, amount: Double)

    // --- القيود المحاسبية المزدوجة ---
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    // --- المخزون والجرد ---
    @Query("SELECT * FROM inventory_items ORDER BY itemName ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantityInStock = quantityInStock + :quantityDelta WHERE itemCode = :itemCode")
    suspend fun updateStockQuantity(itemCode: String, quantityDelta: Double)

    // --- الفواتير ---
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    // --- التقارير الماليّة والميزانية ---
    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'REVENUE'")
    fun getTotalRevenues(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'EXPENSE'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'ASSET'")
    fun getTotalAssets(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM chart_of_accounts WHERE category = 'LIABILITY'")
    fun getTotalLiabilities(): Flow<Double?>
}