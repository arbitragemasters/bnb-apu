package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CryptoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CryptoRepository(application)

    // --- Wallet and Transaction States (Room Flow) ---
    val walletAssets: StateFlow<List<WalletAsset>> = repository.getWalletAssetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionHistory: StateFlow<List<TransactionRecord>> = repository.getTransactionHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Market Ticker States ---
    private val _tickers = MutableStateFlow<Map<String, BinanceTicker>>(emptyMap())
    val tickers: StateFlow<Map<String, BinanceTicker>> = _tickers.asStateFlow()

    private val _selectedPair = MutableStateFlow("BTCUSDT")
    val selectedPair: StateFlow<String> = _selectedPair.asStateFlow()

    private val _chartInterval = MutableStateFlow("1h")
    val chartInterval: StateFlow<String> = _chartInterval.asStateFlow()

    private val _chartCandles = MutableStateFlow<List<Candle>>(emptyList())
    val chartCandles: StateFlow<List<Candle>> = _chartCandles.asStateFlow()

    private val _chartLoading = MutableStateFlow(false)
    val chartLoading: StateFlow<Boolean> = _chartLoading.asStateFlow()

    // Supported Pairs
    val cryptoPairs = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "ADAUSDT", "DOGEUSDT")

    // --- Trade Execution States ---
    private val _tradeStatusMessage = MutableStateFlow<String?>(null)
    val tradeStatusMessage: StateFlow<String?> = _tradeStatusMessage.asStateFlow()

    // --- Watchlist State ---
    private val _favorites = MutableStateFlow<Set<String>>(setOf("BTCUSDT", "ETHUSDT", "SOLUSDT"))
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // --- Balance Hide Toggle State ---
    private val _isHideBalances = MutableStateFlow(false)
    val isHideBalances: StateFlow<Boolean> = _isHideBalances.asStateFlow()

    val showTradingView = MutableStateFlow(false)

    fun toggleHideBalances() {
        _isHideBalances.value = !_isHideBalances.value
    }

    // --- Withdrawal Flow States ---
    private val _withdrawalState = MutableStateFlow<WithdrawalUiState>(WithdrawalUiState.Input)
    val withdrawalState: StateFlow<WithdrawalUiState> = _withdrawalState.asStateFlow()

    // Timer state for dynamic simulation
    private val _emailTimer = MutableStateFlow(0)
    val emailTimer: StateFlow<Int> = _emailTimer.asStateFlow()

    private val _bnbSparklinePoints = MutableStateFlow<List<Float>>(emptyList())
    val bnbSparklinePoints: StateFlow<List<Float>> = _bnbSparklinePoints.asStateFlow()

    private var pricePollingJob: Job? = null
    private var chartPollingJob: Job? = null
    private var emailTimerJob: Job? = null
    private var bnbSparklineJob: Job? = null

    init {
        startPricePolling()
        startChartPolling()
        startBnbSparklinePolling()
    }

    private fun startBnbSparklinePolling() {
        bnbSparklineJob?.cancel()
        bnbSparklineJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val klines = repository.fetchKlines("BNBUSDT", "1h")
                    val points = klines.takeLast(15).map { it.close }
                    if (points.isNotEmpty()) {
                        _bnbSparklinePoints.value = points
                    }
                } catch (e: Exception) {
                    // Suppress
                }
                delay(15000) // update every 15 seconds
            }
        }
    }

    // --- Market Data Fetching/Polling ---

    private fun startPricePolling() {
        pricePollingJob?.cancel()
        pricePollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val updated = mutableMapOf<String, BinanceTicker>()
                cryptoPairs.forEach { pair ->
                    try {
                        val ticker = repository.fetchTicker(pair)
                        updated[pair] = ticker
                    } catch (e: Exception) {
                        // Suppress, repository will provide fallback
                    }
                }
                _tickers.value = updated
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    private fun startChartPolling() {
        chartPollingJob?.cancel()
        chartPollingJob = viewModelScope.launch {
            combine(selectedPair, chartInterval) { pair, interval -> Pair(pair, interval) }
                .collectLatest { (pair, interval) ->
                    _chartLoading.value = true
                    try {
                        val candles = repository.fetchKlines(pair, interval)
                        _chartCandles.value = candles
                    } catch (e: Exception) {
                        _chartCandles.value = emptyList()
                    } finally {
                        _chartLoading.value = false
                    }
                }
        }
    }

    fun selectPair(pair: String) {
        _selectedPair.value = pair
    }

    fun setChartInterval(interval: String) {
        _chartInterval.value = interval
    }

    fun toggleFavorite(pair: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(pair)) {
            current.remove(pair)
        } else {
            current.add(pair)
        }
        _favorites.value = current
    }

    // --- Trade Actions ---

    fun executeTrade(isBuy: Boolean, price: Double, amount: Double) {
        viewModelScope.launch {
            _tradeStatusMessage.value = "Executing trade..."
            val result = repository.executeTrade(
                pair = _selectedPair.value,
                isBuy = isBuy,
                tradePrice = price,
                tradeAmount = amount
            )
            _tradeStatusMessage.value = result
            delay(4000)
            _tradeStatusMessage.value = null
        }
    }

    // --- Deposit simulated funds ---
    fun depositSimulatedFunds(symbol: String, amount: Double) {
        viewModelScope.launch {
            repository.depositFundsSimulated(symbol, amount)
        }
    }

    // --- Transfer simulated sub-wallet funds ---
    fun transferSubWallet(
        symbol: String,
        amount: Double,
        fromAccount: String,
        toAccount: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.transferSubWallet(symbol, amount, fromAccount, toAccount)
            onResult(result)
        }
    }

    // --- Withdrawal Flow Pipeline ---

    fun resetWithdrawalFlow() {
        _withdrawalState.value = WithdrawalUiState.Input
        _emailTimer.value = 0
        emailTimerJob?.cancel()
    }

    fun startEmailTimer() {
        emailTimerJob?.cancel()
        _emailTimer.value = 60
        emailTimerJob = viewModelScope.launch {
            while (_emailTimer.value > 0) {
                delay(1000)
                _emailTimer.value -= 1
            }
        }
    }

    fun submitWithdrawalInput(coin: String, amount: Double, address: String, network: String) {
        if (address.trim().length < 10) {
            _withdrawalState.value = WithdrawalUiState.InputError("Invalid address. Must be at least 10 characters.")
            return
        }
        if (amount <= 0.0) {
            _withdrawalState.value = WithdrawalUiState.InputError("Amount must be greater than zero.")
            return
        }

        viewModelScope.launch {
            val asset = repository.getWalletAsset(coin)
            if (asset == null || asset.balance < amount) {
                _withdrawalState.value = WithdrawalUiState.InputError("Insufficient ${coin} balance.")
                return@launch
            }

            // Validations pass, move to MFA Authentication state
            _withdrawalState.value = WithdrawalUiState.MfaVerification(
                coin = coin,
                amount = amount,
                address = address,
                network = network,
                generatedEmailCode = (100000..999999).random().toString(),
                generatedGfaCode = "482091" // Mock standard Google authenticator rotating code
            )
            // Send automatic verification code triggered
            startEmailTimer()
        }
    }

    fun verifyWithdrawalMfa(enteredEmailCode: String, enteredGfaCode: String) {
        val currentMfa = _withdrawalState.value as? WithdrawalUiState.MfaVerification ?: return

        // Validate verification codes (Google Authenticator accepts '482091' or any 6 digits for friendly simulation, Email code must match generated code)
        if (enteredEmailCode != currentMfa.generatedEmailCode) {
            _withdrawalState.value = currentMfa.copy(errorMessage = "Incorrect Email verification code!")
            return
        }
        if (enteredGfaCode.length != 6) {
            _withdrawalState.value = currentMfa.copy(errorMessage = "Google Authenticator code must be 6 digits!")
            return
        }

        // Codes match! Execute actual withdrawal transaction in repository
        _withdrawalState.value = WithdrawalUiState.Submitting
        viewModelScope.launch {
            delay(1500) // Elegant submitting spinner delay
            val result = repository.initiateWithdrawal(
                symbol = currentMfa.coin,
                amount = currentMfa.amount,
                address = currentMfa.address,
                network = currentMfa.network
            )

            result.onSuccess { txId ->
                _withdrawalState.value = WithdrawalUiState.Success(
                    txId = txId,
                    coin = currentMfa.coin,
                    amount = currentMfa.amount,
                    address = currentMfa.address,
                    network = currentMfa.network
                )
            }.onFailure { exception ->
                _withdrawalState.value = WithdrawalUiState.InputError(exception.message ?: "Withdrawal failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pricePollingJob?.cancel()
        chartPollingJob?.cancel()
        emailTimerJob?.cancel()
        bnbSparklineJob?.cancel()
    }
}

// --- Withdrawal Flow UI States ---
sealed class WithdrawalUiState {
    object Input : WithdrawalUiState()
    data class InputError(val message: String) : WithdrawalUiState()
    data class MfaVerification(
        val coin: String,
        val amount: Double,
        val address: String,
        val network: String,
        val generatedEmailCode: String,
        val generatedGfaCode: String,
        val errorMessage: String? = null
    ) : WithdrawalUiState()
    object Submitting : WithdrawalUiState()
    data class Success(
        val txId: Int,
        val coin: String,
        val amount: Double,
        val address: String,
        val network: String
    ) : WithdrawalUiState()
}
