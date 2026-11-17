package com.yourname.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,  // دخل
    EXPENSE, // مصروف
    DEBT     // دَين
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "main_user", // رمز المستخدم لعزل البيانات Multi-Tenancy
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)