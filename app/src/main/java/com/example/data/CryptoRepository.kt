package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Locale

class CryptoRepository(private val context: Context) {

    // --- Local Database Setup ---
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "crypto_replica_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val walletDao = database.walletDao()
    private val transactionDao = database.transactionDao()

    // --- Retrofit Setup for Binance API ---
    private val binanceApi: BinanceApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BinanceApi::class.java)
    }

    // --- Base Asset Prices for Fallback ---
    private val basePrices = mapOf(
        "BTCUSDT" to 63741.00,
        "ETHUSDT" to 1792.00,
        "SOLUSDT" to 145.0,
        "BNBUSDT" to 577.80,
        "ADAUSDT" to 0.45,
        "DOGEUSDT" to 0.11
    )

    // Cached prices to support realistic step-fluctuations when fallback is active
    private val priceCache = mutableMapOf<String, Double>().apply {
        putAll(basePrices)
    }

    init {
        // Initialize user wallet balance on first app launch
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialWalletIfEmpty()
        }
    }

    // --- Database Initialization Seeding ---
    private suspend fun seedInitialWalletIfEmpty() {
        val existingAssets = walletDao.getAllAssets()
        if (existingAssets.isEmpty()) {
            val initialAssets = listOf(
                WalletAsset("USDT", "Tether", 0.0, 0.0, spotBalance = 0.0, fundingBalance = 0.0, earnBalance = 0.0, futuresBalance = 0.0),
                WalletAsset("BTC", "Bitcoin", 0.00000001, 0.0, spotBalance = 0.00000001, fundingBalance = 0.0, earnBalance = 0.00000002),
                WalletAsset("ACT", "Act I : The AI Prophecy", 0.04, 0.0, spotBalance = 0.04, fundingBalance = 0.0, earnBalance = 0.0),
                WalletAsset("ETH", "Ethereum", 0.00000002, 0.0, spotBalance = 0.00000002, fundingBalance = 0.0, earnBalance = 0.0),
                WalletAsset("SOL", "Solana", 0.0, 0.0, spotBalance = 0.0, fundingBalance = 0.0, earnBalance = 0.0),
                WalletAsset("BNB", "BNB", 0.0, 0.0, spotBalance = 0.0, fundingBalance = 0.0, earnBalance = 0.0),
                WalletAsset("ADA", "Cardano", 0.0, 0.0, spotBalance = 0.0, fundingBalance = 0.0, earnBalance = 0.0),
                WalletAsset("DOGE", "Dogecoin", 0.0, 0.0, spotBalance = 0.0, fundingBalance = 0.0, earnBalance = 0.0)
            )
            walletDao.insertAssets(initialAssets)
        }
    }

    // --- Ticker and Chart APIs ---

    suspend fun fetchTicker(pair: String): BinanceTicker = withContext(Dispatchers.IO) {
        try {
            binanceApi.getTicker24hr(pair)
        } catch (e: Exception) {
            // High-fidelity fallback simulated ticker
            val base = basePrices[pair] ?: 100.0
            // Micro-fluctuation (-0.05% to +0.05%) to simulate active ticking without wild jumps
            val pctChange = (-5..5).random() / 100.0 // e.g. 0.03%
            val newPrice = base * (1 + pctChange / 100)
            priceCache[pair] = newPrice

            val high = newPrice * 1.002
            val low = newPrice * 0.998
            val volume = 12000.0 + (0..50000).random()

            BinanceTicker(
                symbol = pair,
                priceChange = String.format(Locale.US, "%.4f", newPrice - (base * 0.998)),
                priceChangePercent = String.format(Locale.US, "%.2f", pctChange),
                lastPrice = String.format(Locale.US, "%.4f", newPrice),
                highPrice = String.format(Locale.US, "%.4f", high),
                lowPrice = String.format(Locale.US, "%.4f", low),
                volume = String.format(Locale.US, "%.2f", volume),
                openPrice = String.format(Locale.US, "%.4f", base * 0.998)
            )
        }
    }

    suspend fun fetchKlines(pair: String, interval: String): List<Candle> = withContext(Dispatchers.IO) {
        try {
            val rawKlines = binanceApi.getKlines(pair, interval, limit = 100)
            rawKlines.map { kline ->
                Candle(
                    openTime = (kline[0] as Double).toLong(),
                    open = (kline[1] as String).toFloat(),
                    high = (kline[2] as String).toFloat(),
                    low = (kline[3] as String).toFloat(),
                    close = (kline[4] as String).toFloat(),
                    volume = (kline[5] as String).toFloat()
                )
            }
        } catch (e: Exception) {
            // Generate simulated candles based on the base price
            val base = priceCache[pair] ?: 100.0
            val list = mutableListOf<Candle>()
            var lastClose = base * 0.95
            var time = System.currentTimeMillis() - (100 * 3600 * 1000) // 100 hours ago
            
            val intervalMs = when (interval) {
                "1m" -> 60 * 1000L
                "15m" -> 15 * 60 * 1000L
                "1h" -> 3600 * 1000L
                "4h" -> 4 * 3600 * 1000L
                else -> 24 * 3600 * 1000L
            }

            for (i in 1..100) {
                val open = lastClose
                val fluctuation = open * ((-8..8).random() / 200.0) // up to 4% fluctuation
                val close = open + fluctuation
                val high = maxOf(open, close) + (open * ((0..10).random() / 500.0))
                val low = minOf(open, close) - (open * ((0..10).random() / 500.0))
                val volume = 100f + (0..1000).random().toFloat()
                
                list.add(
                    Candle(
                        openTime = time,
                        open = open.toFloat(),
                        high = high.toFloat(),
                        low = low.toFloat(),
                        close = close.toFloat(),
                        volume = volume
                    )
                )
                lastClose = close
                time += intervalMs
            }
            list
        }
    }

    // --- Wallet Balances ---

    fun getWalletAssetsFlow(): Flow<List<WalletAsset>> = walletDao.getAllAssetsFlow()

    suspend fun getWalletAsset(symbol: String): WalletAsset? = withContext(Dispatchers.IO) {
        walletDao.getAssetBySymbol(symbol)
    }

    suspend fun depositFundsSimulated(symbol: String, amount: Double) = withContext(Dispatchers.IO) {
        val existing = walletDao.getAssetBySymbol(symbol)
        if (existing != null) {
            walletDao.updateAsset(existing.copy(
                spotBalance = existing.spotBalance + amount,
                balance = existing.balance + amount
            ))
        } else {
            walletDao.insertAsset(WalletAsset(symbol, symbol, amount, 0.0, spotBalance = amount, fundingBalance = 0.0))
        }
        transactionDao.insertTransaction(
            TransactionRecord(
                coinSymbol = symbol,
                amount = amount,
                type = "DEPOSIT",
                status = "COMPLETED",
                address = "0xSimulationDepositWalletAddress",
                network = "Mainnet"
            )
        )
    }

    // --- Buy/Sell Trading Execution ---

    suspend fun executeTrade(
        pair: String, // e.g. "BTCUSDT"
        isBuy: Boolean,
        tradePrice: Double,
        tradeAmount: Double // in base crypto amount, e.g. 0.05 BTC
    ): String = withContext(Dispatchers.IO) {
        val baseSymbol = if (pair.length >= 4) pair.substring(0, pair.length - 4) else pair
        val quoteSymbol = if (pair.length >= 4) pair.substring(pair.length - 4) else "USDT"

        val totalQuoteCost = tradePrice * tradeAmount

        val baseAsset = walletDao.getAssetBySymbol(baseSymbol) ?: WalletAsset(baseSymbol, baseSymbol, 0.0)
        val quoteAsset = walletDao.getAssetBySymbol(quoteSymbol) ?: WalletAsset(quoteSymbol, quoteSymbol, 0.0)

        if (isBuy) {
            if (quoteAsset.spotBalance < totalQuoteCost) {
                return@withContext "Insufficient ${quoteSymbol} Spot balance. Required: ${String.format(Locale.US, "%.2f", totalQuoteCost)}, Available: ${String.format(Locale.US, "%.2f", quoteAsset.spotBalance)}"
            }
            // Update USDT and Crypto Balances on Spot
            val updatedQuoteSpot = quoteAsset.spotBalance - totalQuoteCost
            val updatedQuoteTotal = quoteAsset.balance - totalQuoteCost
            walletDao.updateAsset(quoteAsset.copy(spotBalance = updatedQuoteSpot, balance = updatedQuoteTotal))

            val updatedBaseSpot = baseAsset.spotBalance + tradeAmount
            val updatedBaseTotal = baseAsset.balance + tradeAmount
            walletDao.updateAsset(baseAsset.copy(spotBalance = updatedBaseSpot, balance = updatedBaseTotal))

            // Save records
            transactionDao.insertTransaction(
                TransactionRecord(
                    coinSymbol = baseSymbol,
                    amount = tradeAmount,
                    type = "BUY",
                    status = "COMPLETED",
                    address = null,
                    network = null
                )
            )
            return@withContext "SUCCESS: Bought ${String.format(Locale.US, "%.4f", tradeAmount)} $baseSymbol at ${String.format(Locale.US, "%.2f", tradePrice)} $quoteSymbol"
        } else {
            if (baseAsset.spotBalance < tradeAmount) {
                return@withContext "Insufficient ${baseSymbol} Spot balance. Required: ${String.format(Locale.US, "%.4f", tradeAmount)}, Available: ${String.format(Locale.US, "%.4f", baseAsset.spotBalance)}"
            }
            // Update USDT and Crypto Balances on Spot
            val updatedBaseSpot = baseAsset.spotBalance - tradeAmount
            val updatedBaseTotal = baseAsset.balance - tradeAmount
            walletDao.updateAsset(baseAsset.copy(spotBalance = updatedBaseSpot, balance = updatedBaseTotal))

            val updatedQuoteSpot = quoteAsset.spotBalance + totalQuoteCost
            val updatedQuoteTotal = quoteAsset.balance + totalQuoteCost
            walletDao.updateAsset(quoteAsset.copy(spotBalance = updatedQuoteSpot, balance = updatedQuoteTotal))

            // Save records
            transactionDao.insertTransaction(
                TransactionRecord(
                    coinSymbol = baseSymbol,
                    amount = tradeAmount,
                    type = "SELL",
                    status = "COMPLETED",
                    address = null,
                    network = null
                )
            )
            return@withContext "SUCCESS: Sold ${String.format(Locale.US, "%.4f", tradeAmount)} $baseSymbol at ${String.format(Locale.US, "%.2f", tradePrice)} $quoteSymbol"
        }
    }

    // --- Withdrawal Flow & Pipeline ---

    fun getTransactionHistoryFlow(): Flow<List<TransactionRecord>> = transactionDao.getAllTransactionsFlow()

    suspend fun initiateWithdrawal(
        symbol: String,
        amount: Double,
        address: String,
        network: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        val asset = walletDao.getAssetBySymbol(symbol)
            ?: return@withContext Result.failure(Exception("Asset not found"))

        if (asset.spotBalance < amount) {
            return@withContext Result.failure(Exception("Insufficient ${symbol} Spot balance. Available: ${asset.spotBalance}"))
        }

        // Lock the balance (moves active Spot balance to locked, prevents double withdrawal)
        val updatedAsset = asset.copy(
            spotBalance = asset.spotBalance - amount,
            balance = asset.balance - amount,
            lockedBalance = asset.lockedBalance + amount
        )
        walletDao.updateAsset(updatedAsset)

        // Insert transaction with "PENDING" status
        val transaction = TransactionRecord(
            coinSymbol = symbol,
            amount = amount,
            type = "WITHDRAWAL",
            status = "PENDING",
            address = address,
            network = network
        )
        val txId = transactionDao.insertTransaction(transaction).toInt()

        // Start background process to simulate network confirmation stages!
        // This simulates a live blockchain confirmation process
        simulateBlockchainConfirmations(txId, symbol, amount)

        return@withContext Result.success(txId)
    }

    suspend fun transferSubWallet(
        symbol: String,
        amount: Double,
        fromAccount: String, // "Spot", "Funding", "Earn", "Futures"
        toAccount: String   // "Spot", "Funding", "Earn", "Futures"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val asset = walletDao.getAssetBySymbol(symbol) 
            ?: return@withContext Result.failure(Exception("Asset not found"))
        
        val fromBalance = when (fromAccount) {
            "Spot" -> asset.spotBalance
            "Funding" -> asset.fundingBalance
            "Earn" -> asset.earnBalance
            "Futures" -> asset.futuresBalance
            else -> 0.0
        }
        
        if (fromBalance < amount) {
            return@withContext Result.failure(Exception("Insufficient balance in $fromAccount wallet!"))
        }
        
        var newSpot = asset.spotBalance
        var newFunding = asset.fundingBalance
        var newEarn = asset.earnBalance
        var newFutures = asset.futuresBalance
        
        when (fromAccount) {
            "Spot" -> newSpot -= amount
            "Funding" -> newFunding -= amount
            "Earn" -> newEarn -= amount
            "Futures" -> newFutures -= amount
        }
        
        when (toAccount) {
            "Spot" -> newSpot += amount
            "Funding" -> newFunding += amount
            "Earn" -> newEarn += amount
            "Futures" -> newFutures += amount
        }
        
        val updatedAsset = asset.copy(
            spotBalance = newSpot,
            fundingBalance = newFunding,
            earnBalance = newEarn,
            futuresBalance = newFutures,
            balance = newSpot + newFunding + newEarn + newFutures
        )
        
        walletDao.updateAsset(updatedAsset)
        
        transactionDao.insertTransaction(
            TransactionRecord(
                coinSymbol = symbol,
                amount = amount,
                type = "TRANSFER ($fromAccount -> $toAccount)",
                status = "COMPLETED",
                address = "Sub-wallet Transfer",
                network = "Internal Network"
            )
        )
        
        return@withContext Result.success(Unit)
    }

    private fun simulateBlockchainConfirmations(txId: Int, symbol: String, amount: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            // Stage 1: PENDING to PROCESSING
            delay(5000)
            val record1 = transactionDao.getTransactionById(txId)
            if (record1 != null) {
                transactionDao.updateTransaction(record1.copy(status = "PROCESSING"))
            }

            // Stage 2: PROCESSING to COMPLETED
            delay(12000)
            val record2 = transactionDao.getTransactionById(txId)
            if (record2 != null) {
                transactionDao.updateTransaction(record2.copy(status = "COMPLETED"))

                // Once complete, release/deduct the locked amount permanently!
                val asset = walletDao.getAssetBySymbol(symbol)
                if (asset != null) {
                    val finalLocked = maxOf(0.0, asset.lockedBalance - amount)
                    walletDao.updateAsset(asset.copy(lockedBalance = finalLocked))
                }
            }
        }
    }
}
