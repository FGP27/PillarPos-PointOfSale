package com.pilarkreasi.pillarpos.data.local

import androidx.room.*
import com.pilarkreasi.pillarpos.data.model.TransactionDetailEntity
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionDetails(details: List<TransactionDetailEntity>)

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE idTransaction = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("SELECT * FROM transaction_detail WHERE idTransaction = :transactionId")
    suspend fun getDetailsByTransactionId(transactionId: Int): List<TransactionDetailEntity>

    @Query(
        "SELECT p.name as productName, d.quantity as quantity, d.priceAtTime as priceAtTime " +
                "FROM transaction_detail d " +
                "INNER JOIN product p ON p.idProduct = d.idProduct " +
                "WHERE d.idTransaction = :transactionId"
    )
    suspend fun getDetailsWithProductByTransactionId(
        transactionId: Int
    ): List<com.pilarkreasi.pillarpos.data.model.TransactionDetailWithProduct>


    @Query("SELECT * FROM transactions WHERE idTransaction = :id")
    fun observeTransactionById(id: Int): Flow<TransactionEntity?>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)


    @Query("SELECT * FROM transactions WHERE isSynced = 0 ORDER BY transactionDate ASC")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1, syncedAt = :syncedAt WHERE idTransaction = :id")
    suspend fun markAsSynced(id: Int, syncedAt: Long)

    @Transaction
    suspend fun insertFullTransaction(transaction: TransactionEntity, details: List<TransactionDetailEntity>): Int {
        val id = insertTransaction(transaction)
        val detailsWithId = details.map { it.copy(idTransaction = id.toInt()) }
        insertTransactionDetails(detailsWithId)
        return id.toInt()
    }

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE paymentStatus = 'COMPLETED' AND date(transactionDate/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun getTodayTotalSales(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE paymentStatus = 'COMPLETED' AND date(transactionDate/1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    fun getTodayTransactionCount(): Flow<Int>


    @Query("SELECT * FROM transactions WHERE paymentStatus = 'PENDING_VOID' ORDER BY transactionDate DESC")
    fun getPendingVoidTransactions(): Flow<List<TransactionEntity>>


    @Query(
        "SELECT SUM(totalAmount) as totalSales, COUNT(*) as totalTransactions " +
                "FROM transactions " +
                "WHERE paymentStatus = 'COMPLETED' AND transactionDate >= :startMillis AND transactionDate < :endMillis"
    )
    fun getSalesSummary(startMillis: Long, endMillis: Long): Flow<com.pilarkreasi.pillarpos.data.model.SalesSummary>

    @Query(
        "SELECT date(transactionDate/1000, 'unixepoch', 'localtime') as day, SUM(totalAmount) as total " +
                "FROM transactions " +
                "WHERE paymentStatus = 'COMPLETED' AND transactionDate >= :startMillis AND transactionDate < :endMillis " +
                "GROUP BY day ORDER BY day ASC"
    )
    fun getDailySalesTrend(startMillis: Long, endMillis: Long): Flow<List<com.pilarkreasi.pillarpos.data.model.DailySales>>

    @Query(
        "SELECT p.name as name, SUM(d.quantity) as totalQty " +
                "FROM transaction_detail d " +
                "INNER JOIN transactions t ON d.idTransaction = t.idTransaction " +
                "INNER JOIN product p ON d.idProduct = p.idProduct " +
                "WHERE t.paymentStatus = 'COMPLETED' AND t.transactionDate >= :startMillis AND t.transactionDate < :endMillis " +
                "GROUP BY d.idProduct ORDER BY totalQty DESC LIMIT :limit"
    )
    fun getTopProducts(startMillis: Long, endMillis: Long, limit: Int = 5): Flow<List<com.pilarkreasi.pillarpos.data.model.TopProduct>>
}