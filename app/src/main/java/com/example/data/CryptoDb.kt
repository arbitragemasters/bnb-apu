package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "wallet_assets")
data class WalletAsset(
    @PrimaryKey val coinSymbol: String, // e.g. "USDT", "BTC", "ETH"
    val coinName: String,
    val balance: Double, // Represents total balance across all sub-wallets
    val lockedBalance: Double = 0.0,
    val spotBalance: Double = 0.0,
    val fundingBalance: Double = 0.0,
    val earnBalance: Double = 0.0,
    val futuresBalance: Double = 0.0
) {
    val totalBalance: Double get() = spotBalance + fundingBalance + earnBalance + futuresBalance + lockedBalance
}

@Entity(tableName = "transaction_records")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coinSymbol: String,
    val amount: Double,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "BUY", "SELL"
    val status: String, // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
    val address: String?,
    val network: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val txHash: String = "0x" + (1..40).map { "0123456789abcdef".random() }.joinToString("")
)

// --- DAOs ---

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_assets")
    fun getAllAssetsFlow(): Flow<List<WalletAsset>>

    @Query("SELECT * FROM wallet_assets")
    suspend fun getAllAssets(): List<WalletAsset>

    @Query("SELECT * FROM wallet_assets WHERE coinSymbol = :symbol")
    suspend fun getAssetBySymbol(symbol: String): WalletAsset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: WalletAsset)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<WalletAsset>)

    @Update
    suspend fun updateAsset(asset: WalletAsset)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_records ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    @Query("SELECT * FROM transaction_records WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionRecord?
}

// --- AppDatabase ---

@Database(entities = [WalletAsset::class, TransactionRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
}
