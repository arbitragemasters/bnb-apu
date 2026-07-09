package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BinanceTicker
import com.example.data.Candle
import com.example.ui.CryptoViewModel
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CryptoViewModel
) {
    val tickers by viewModel.tickers.collectAsState()
    val selectedPair by viewModel.selectedPair.collectAsState()
    val interval by viewModel.chartInterval.collectAsState()
    val candles by viewModel.chartCandles.collectAsState()
    val loadingChart by viewModel.chartLoading.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val walletAssets by viewModel.walletAssets.collectAsState()
    val tradeMessage by viewModel.tradeStatusMessage.collectAsState()

    val context = LocalContext.current

    // Trading Panel States
    var isBuyTab by remember { mutableStateOf(true) }
    var orderType by remember { mutableStateOf("Limit") } // "Limit", "Market"
    var tradePriceInput by remember { mutableStateOf("") }
    var tradeAmountInput by remember { mutableStateOf("") }

    val currentTicker = tickers[selectedPair]
    val basePrice = currentTicker?.lastPrice?.toDoubleOrNull() ?: 100.0

    // Whenever pair or ticker price changes, prefill input if empty or limit order is selected
    LaunchedEffect(selectedPair, currentTicker) {
        if (currentTicker != null && (tradePriceInput.isEmpty() || orderType == "Market")) {
            tradePriceInput = currentTicker.lastPrice
        }
    }

    val showTradingView by viewModel.showTradingView.collectAsState()
    val isHideBalances by viewModel.isHideBalances.collectAsState()

    if (showTradingView) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BinanceDarkBg)
        ) {
        // Top Header of Selected Pair
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceDarkSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val base = if (selectedPair.length >= 4) selectedPair.substring(0, selectedPair.length - 4) else selectedPair
                CryptoIcon(symbol = base, size = 28.dp)
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${base}/USDT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = BinanceTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Pair",
                            tint = BinanceTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Binance Futures Live",
                        fontSize = 10.sp,
                        color = BinanceGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Add to favorites toggle
                Icon(
                    imageVector = if (favorites.contains(selectedPair)) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (favorites.contains(selectedPair)) BinanceGold else BinanceTextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.toggleFavorite(selectedPair) }
                )
            }

            // Stats view
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentTicker != null) {
                    val change = currentTicker.priceChangePercent.toDoubleOrNull() ?: 0.0
                    val changeColor = if (change >= 0) BinanceGreen else BinanceRed
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currentTicker.lastPrice,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                        Text(
                            text = String.format(Locale.US, "%+.2f%%", change),
                            fontSize = 12.sp,
                            color = changeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Ticker Quick Selector Slider (horizontally scrollable pairs)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceDarkSurface)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.cryptoPairs) { pair ->
                val ticker = tickers[pair]
                val isSelected = pair == selectedPair
                val baseSym = if (pair.length >= 4) pair.substring(0, pair.length - 4) else pair

                Card(
                    modifier = Modifier
                        .width(115.dp)
                        .clickable { viewModel.selectPair(pair) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BinanceCardBg else BinanceDarkBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (isSelected) StrokeBorder(BinanceGold, 1.dp) else null
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CryptoIcon(baseSym, size = 16.dp)
                            Text(
                                text = "$baseSym/USDT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextPrimary
                            )
                        }
                        if (ticker != null) {
                            val changePct = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0
                            val col = if (changePct >= 0) BinanceGreen else BinanceRed
                            Text(
                                text = ticker.lastPrice,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BinanceTextPrimary
                            )
                            Text(
                                text = String.format(Locale.US, "%+.2f%%", changePct),
                                fontSize = 10.sp,
                                color = col,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("Loading...", fontSize = 11.sp, color = BinanceTextSecondary)
                        }
                    }
                }
            }
        }

        // Main Layout: Chart & trading split
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            item {
                // Interactive Candlestick Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(BinanceDarkBg)
                ) {
                    if (loadingChart) {
                        CircularProgressIndicator(
                            color = BinanceGold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (candles.isNotEmpty()) {
                        CandlestickChart(
                            candles = candles,
                            pair = selectedPair,
                            ticker = currentTicker,
                            interval = interval,
                            onIntervalChange = { viewModel.setChartInterval(it) }
                        )
                    } else {
                        Text(
                            text = "Error loading trading chart",
                            color = BinanceRed,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            item {
                HorizontalDivider(color = BinanceDivider, thickness = 1.dp)
            }

            // Real-time Order Book and Trade Execution split
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Order Book (live generated orders)
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Order Book",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Price(USDT)", color = BinanceTextSecondary, fontSize = 9.sp)
                            Text("Qty(Base)", color = BinanceTextSecondary, fontSize = 9.sp)
                        }

                        // Red Asks (Selling orders - top)
                        val asks = remember(selectedPair, basePrice) {
                            List(6) { idx ->
                                val offset = (6 - idx) * (basePrice * 0.0003)
                                Pair(basePrice + offset, 0.01 + (0..100).random() / 15.0)
                            }
                        }

                        asks.forEach { (price, qty) ->
                            OrderBookRow(price = price, qty = qty, isBuy = false, basePrice = basePrice)
                        }

                        // Current Spread Price
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            val changePct = currentTicker?.priceChangePercent?.toDoubleOrNull() ?: 0.0
                            val color = if (changePct >= 0) BinanceGreen else BinanceRed
                            Column {
                                Text(
                                    text = String.format(Locale.US, "%.4f", basePrice),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    text = "≈$${String.format(Locale.US, "%.2f", basePrice)}",
                                    fontSize = 10.sp,
                                    color = BinanceTextSecondary
                                )
                            }
                        }

                        // Green Bids (Buying orders - bottom)
                        val bids = remember(selectedPair, basePrice) {
                            List(6) { idx ->
                                val offset = (idx + 1) * (basePrice * 0.0003)
                                Pair(basePrice - offset, 0.01 + (0..100).random() / 15.0)
                            }
                        }

                        bids.forEach { (price, qty) ->
                            OrderBookRow(price = price, qty = qty, isBuy = true, basePrice = basePrice)
                        }
                    }

                    // Right Column: Trade Panel Form
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Buy/Sell tab switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BinancePillBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (isBuyTab) BinanceGreen else Color.Transparent)
                                    .clickable { isBuyTab = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BUY", color = if (isBuyTab) Color.Black else BinanceTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (!isBuyTab) BinanceRed else Color.Transparent)
                                    .clickable { isBuyTab = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SELL", color = if (!isBuyTab) Color.White else BinanceTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Order Type (Limit/Market selector)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Limit", "Market").forEach { type ->
                                val active = orderType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (active) BinanceCardBg else Color.Transparent)
                                        .clickable { orderType = type }
                                        .border(
                                            width = if (active) 1.dp else 0.dp,
                                            color = if (active) BinanceGold else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(type, color = if (active) BinanceGold else BinanceTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Price Input Field (Limit Order only)
                        if (orderType == "Limit") {
                            TextField(
                                value = tradePriceInput,
                                onValueChange = { tradePriceInput = it },
                                label = { Text("Price (USDT)", color = BinanceTextSecondary, fontSize = 10.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = BinancePillBg,
                                    unfocusedContainerColor = BinancePillBg,
                                    focusedTextColor = BinanceTextPrimary,
                                    unfocusedTextColor = BinanceTextPrimary,
                                    focusedIndicatorColor = BinanceGold,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(6.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Market price display placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BinancePillBg)
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Text("Market Price", color = BinanceTextSecondary, fontSize = 13.sp)
                            }
                        }

                        // Amount Input Field
                        val baseSymbol = if (selectedPair.length >= 4) selectedPair.substring(0, selectedPair.length - 4) else selectedPair
                        TextField(
                            value = tradeAmountInput,
                            onValueChange = { tradeAmountInput = it },
                            label = { Text("Amount ($baseSymbol)", color = BinanceTextSecondary, fontSize = 10.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BinancePillBg,
                                unfocusedContainerColor = BinancePillBg,
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary,
                                focusedIndicatorColor = BinanceGold,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Wallet Balance display
                        val balanceAsset = walletAssets.find { it.coinSymbol == if (isBuyTab) "USDT" else baseSymbol }
                        val balDouble = balanceAsset?.balance ?: 0.0
                        Text(
                            text = if (isBuyTab) {
                                "Available: ${formatWithCommas(balDouble, maxDecimals = 8, minDecimals = 2)} USDT"
                            } else {
                                "Available: ${formatWithCommas(balDouble, maxDecimals = 8, minDecimals = 4)} $baseSymbol"
                            },
                            color = BinanceTextSecondary,
                            fontSize = 11.sp
                        )

                        // Percentage Quick Slider Buttons (25%, 50%, 75%, 100%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(0.25, 0.50, 0.75, 1.0).forEach { pct ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BinancePillBg)
                                        .clickable {
                                            if (isBuyTab) {
                                                val price = tradePriceInput.toDoubleOrNull() ?: basePrice
                                                if (price > 0) {
                                                    val amount = (balDouble * pct) / price
                                                    tradeAmountInput = String.format(Locale.US, "%.4f", amount)
                                                }
                                            } else {
                                                tradeAmountInput = String.format(Locale.US, "%.4f", balDouble * pct)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${(pct * 100).toInt()}%", color = BinanceTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Big Execution Action Button
                        Button(
                            onClick = {
                                val price = if (orderType == "Market") basePrice else (tradePriceInput.toDoubleOrNull() ?: 0.0)
                                val amount = tradeAmountInput.toDoubleOrNull() ?: 0.0
                                if (price <= 0 || amount <= 0) {
                                    Toast.makeText(context, "Please enter a valid price and amount!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.executeTrade(isBuyTab, price, amount)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBuyTab) BinanceGreen else BinanceRed,
                                contentColor = if (isBuyTab) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isBuyTab) "Buy $baseSymbol" else "Sell $baseSymbol",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Success notification overlay inline
                        AnimatedVisibility(
                            visible = tradeMessage != null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            tradeMessage?.let { msg ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (msg.startsWith("SUCCESS")) BinanceGreen.copy(alpha = 0.15f) else BinanceRed.copy(alpha = 0.15f)
                                    ),
                                    border = StrokeBorder(if (msg.startsWith("SUCCESS")) BinanceGreen else BinanceRed, 1.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        color = if (msg.startsWith("SUCCESS")) BinanceGreen else BinanceRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    } else {
        HomeLandingView(
            viewModel = viewModel,
            tickers = tickers,
            walletAssets = walletAssets,
            isHideBalances = isHideBalances
        )
    }
}

// Custom function to generate StrokeBorder safely in modern Compose
private fun StrokeBorder(color: Color, width: androidx.compose.ui.unit.Dp) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun OrderBookRow(price: Double, qty: Double, isBuy: Boolean, basePrice: Double) {
    val barColor = if (isBuy) BinanceGreen.copy(alpha = 0.12f) else BinanceRed.copy(alpha = 0.12f)
    // Fill percent based on quantity size for visual depth
    val fillPercent = (qty / 15.0).coerceIn(0.1, 1.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        // Transparent depth bar in background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fillPercent.toFloat())
                .align(Alignment.CenterEnd)
                .background(barColor)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(Locale.US, "%.4f", price),
                color = if (isBuy) BinanceGreen else BinanceRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format(Locale.US, "%.4f", qty),
                color = BinanceTextPrimary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    pair: String,
    ticker: BinanceTicker?,
    interval: String,
    onIntervalChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Ticker details inside chart header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1m", "15m", "1h", "4h", "1d").forEach { item ->
                    val isSel = interval == item
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) BinanceCardBg else Color.Transparent)
                            .border(width = if (isSel) 1.dp else 0.dp, color = if (isSel) BinanceGold else Color.Transparent, shape = RoundedCornerShape(4.dp))
                            .clickable { onIntervalChange(item) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item,
                            color = if (isSel) BinanceGold else BinanceTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (ticker != null) {
                Text(
                    text = "High: ${ticker.highPrice}  Low: ${ticker.lowPrice}",
                    color = BinanceTextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas element drawing high performance candlesticks
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height

            if (candles.isEmpty()) return@Canvas

            // Calculate Min and Max prices in set to fit perfectly on canvas
            val maxPrice = candles.maxOf { it.high }
            val minPrice = candles.minOf { it.low }
            val priceRange = maxPrice - minPrice

            // Pad the range slightly (10%)
            val paddedMax = maxPrice + (priceRange * 0.05f)
            val paddedMin = (minPrice - (priceRange * 0.05f)).coerceAtLeast(0.0001f)
            val finalRange = paddedMax - paddedMin

            val candleCount = candles.size
            val candleWidth = width / candleCount
            val spacing = candleWidth * 0.2f // 20% gap

            // Draw clean vertical grid lines
            val gridColor = BinanceDivider.copy(alpha = 0.4f)
            for (i in 1..4) {
                val gridX = (width / 5) * i
                drawLine(
                    color = gridColor,
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, height),
                    strokeWidth = 1f
                )
            }

            // Draw horizontal price lines
            for (i in 1..3) {
                val gridY = (height / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f
                )
            }

            candles.forEachIndexed { index, candle ->
                val x = (index * candleWidth) + (spacing / 2)
                val isBullish = candle.close >= candle.open
                val candleColor = if (isBullish) BinanceGreen else BinanceRed

                // Convert prices to Y coordinates (inverted because 0 is at top)
                val yHigh = height - ((candle.high - paddedMin) / finalRange * height)
                val yLow = height - ((candle.low - paddedMin) / finalRange * height)
                val yOpen = height - ((candle.open - paddedMin) / finalRange * height)
                val yClose = height - ((candle.close - paddedMin) / finalRange * height)

                // Draw central wick shadow line
                drawLine(
                    color = candleColor,
                    start = Offset(x + (candleWidth - spacing) / 2, yHigh),
                    end = Offset(x + (candleWidth - spacing) / 2, yLow),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Draw body rectangle
                val rectTop = minOf(yOpen, yClose)
                val rectHeight = (yOpen - yClose).absoluteValue.coerceAtLeast(1.dp.toPx())
                
                drawRect(
                    color = candleColor,
                    topLeft = Offset(x, rectTop),
                    size = Size(candleWidth - spacing, rectHeight)
                )
            }
        }
    }
}

@Composable
fun SparklineChart(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val minX = 0f
        val maxX = (points.size - 1).toFloat()
        val minY = points.minOrNull() ?: 0f
        val maxY = points.maxOrNull() ?: 1f
        val rangeY = (maxY - minY).coerceAtLeast(1f)

        val path = Path()
        points.forEachIndexed { idx, value ->
            val x = (idx / maxX) * width
            val y = height - ((value - minY) / rangeY) * height
            if (idx == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLandingView(
    viewModel: CryptoViewModel,
    tickers: Map<String, BinanceTicker>,
    walletAssets: List<com.example.data.WalletAsset>,
    isHideBalances: Boolean
) {
    val context = LocalContext.current
    var activeExchangeTab by remember { mutableStateOf("Exchange") } // Exchange vs Wallet
    var bannerSlideIndex by remember { mutableStateOf(0) }
    
    // Dynamic portfolio balance calculation
    val totalBalanceUsdt = remember(walletAssets, tickers) {
        walletAssets.fold(0.0) { sum, asset ->
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            val totalAssetBalance = asset.spotBalance + asset.fundingBalance + asset.earnBalance + asset.futuresBalance
            sum + totalAssetBalance * price
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        // 1. Top Header Row (Hamburger, Profile, Switcher, Scan, Alert)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                Toast.makeText(context, "Menu opened", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BinanceMenuIcon(color = BinanceTextPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                Toast.makeText(context, "Profile settings opened", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BinanceProfileIcon(color = BinanceTextSecondary)
                    }
                }

                // Center Switcher "Exchange" vs "Wallet"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BinancePillBg)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Exchange", "Wallet").forEach { tab ->
                        val active = activeExchangeTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) BinanceDarkSurface else Color.Transparent)
                                .clickable { activeExchangeTab = tab }
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (active) BinanceTextPrimary else BinanceTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Right Icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                Toast.makeText(context, "Starting scanner...", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BinanceScannerIcon(
                            color = BinanceTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                Toast.makeText(context, "Notifications opened", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(24.dp)) {
                            BinanceMessageIcon(
                                color = BinanceTextPrimary,
                                modifier = Modifier
                                    .size(19.dp)
                                    .align(Alignment.Center)
                            )
                            // Notification Badge "99+" snuggled perfectly to the top-right corner in a clean round circle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 3.dp, y = (-3).dp)
                                    .size(13.dp)
                                    .background(color = BinanceGold, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "99+",
                                    color = Color(0xFF181A20),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = @Suppress("DEPRECATION") androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Search Bar
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(BinancePillBg)
                    .clickable { Toast.makeText(context, "Searching...", Toast.LENGTH_SHORT).show() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔥", fontSize = 12.sp)
                    Text("POL hot search", color = BinanceTextSecondary, fontSize = 12.sp)
                }
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search icon",
                    tint = BinanceTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 3. Est. Total Value Card (Balance Card)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.clickable { viewModel.toggleHideBalances() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Est. Total Value(USDT)", color = BinanceTextSecondary, fontSize = 12.sp)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropUp,
                            contentDescription = "Up",
                            tint = BinanceTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHideBalances) "******" else {
                            if (totalBalanceUsdt < 1.0) {
                                formatWithCommas(totalBalanceUsdt, maxDecimals = 8, minDecimals = 7)
                            } else {
                                formatWithCommas(totalBalanceUsdt, maxDecimals = 2, minDecimals = 2)
                            }
                        },
                        color = BinanceTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isHideBalances) "******" else {
                            if (totalBalanceUsdt < 1.0) {
                                "≈$${formatWithCommas(totalBalanceUsdt, maxDecimals = 8, minDecimals = 7)}"
                            } else {
                                "≈$${formatWithCommas(totalBalanceUsdt, maxDecimals = 2, minDecimals = 2)}"
                            }
                        },
                        color = BinanceTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Today's PNL", color = BinanceTextSecondary, fontSize = 11.sp)
                        Text(
                            text = "-$0.00002142 (-2.05%)",
                            color = BinanceRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = BinanceRed,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Add Funds Button
                Button(
                    onClick = {
                        Toast.makeText(context, "Opening Deposit portal...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BinanceGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Add Funds", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 4. Promotional Banner (bStocks Trading Carnival / Trading Countdown Carousel)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BinanceCardBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (bannerSlideIndex == 0) {
                        // Slide 0: Trading Countdown (WENUSDT Perp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trading Countdown",
                                color = BinanceTextSecondary,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = BinanceTextSecondary,
                                modifier = Modifier.size(14.dp).clickable {
                                    Toast.makeText(context, "Banner dismissed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                WendysLogo(modifier = Modifier.size(36.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "WENUSDT",
                                            color = BinanceTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFF2B3139))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "Perp",
                                                color = BinanceTextSecondary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Starts in 0H : 1M",
                                        color = BinanceTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.selectPair("WENUSDT")
                                    viewModel.showTradingView.value = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3139), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Trade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Slide 1: bStocks Trading Carnival
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "bStocks Trading Carnival",
                                color = BinanceTextSecondary,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = BinanceTextSecondary,
                                modifier = Modifier.size(14.dp).clickable {
                                    Toast.makeText(context, "Banner dismissed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(BinanceGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("M", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text(
                                    text = "Share Up to 100,000 USDT in MUB",
                                    color = BinanceTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Button(
                                onClick = { Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = BinancePillBg, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 9 beautiful pager indicator dots with active capsule matching the screenshot exactly!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (0..8).forEach { index ->
                            // Use index % 2 to alternate active slide
                            val isActive = (bannerSlideIndex == 0 && index == 3) || (bannerSlideIndex == 1 && index == 4)
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .width(12.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(BinanceTextSecondary.copy(alpha = 0.35f))
                                        .clickable {
                                            bannerSlideIndex = if (index < 4) 0 else 1
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Grid Action Cards (UAH Card vs BNB Sparkline Card)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Card: UAH Card
                Card(
                    modifier = Modifier.weight(1f).aspectRatio(0.98f),
                    colors = CardDefaults.cardColors(containerColor = BinanceCardBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CryptoIcon("UAH", size = 18.dp)
                                Text("UAH", color = BinanceTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(BinanceTextSecondary.copy(alpha = 0.5f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BinanceGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Deposit", color = BinanceGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Text("Card (Fiat Trade)", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FiatScannerIcon(modifier = Modifier.size(16.dp), color = BinanceTextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.width(8.dp).height(3.dp).clip(RoundedCornerShape(1.5.dp)).background(Color.White))
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(BinanceTextSecondary.copy(alpha = 0.4f)))
                            }
                        }
                    }
                }

                // Right Card: BNB Sparkline Card
                val bnbPrice = tickers["BNBUSDT"]?.lastPrice ?: "571.68"
                val bnbChange = tickers["BNBUSDT"]?.priceChangePercent ?: "+1.04"
                val isBnbPositive = !bnbChange.startsWith("-")
                val bnbColor = if (isBnbPositive) BinanceGreen else BinanceRed
                val bnbSparklinePoints by viewModel.bnbSparklinePoints.collectAsState()
                
                val sparklinePoints = remember(bnbChange, bnbSparklinePoints) {
                    if (bnbSparklinePoints.isNotEmpty()) {
                        bnbSparklinePoints
                    } else {
                        if (isBnbPositive) {
                            listOf(10f, 15f, 12f, 18f, 22f, 20f, 25f, 28f, 32f)
                        } else {
                            listOf(35f, 32f, 30f, 22f, 25f, 18f, 20f, 12f, 10f)
                        }
                    }
                }

                val bnbChangeText = remember(bnbChange) {
                    val cleanVal = bnbChange.replace("-", "").replace("+", "").trim()
                    if (isBnbPositive) "▲ $cleanVal%" else "▼ $cleanVal%"
                }

                Card(
                    modifier = Modifier.weight(1f).aspectRatio(0.98f),
                    colors = CardDefaults.cardColors(containerColor = BinanceCardBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CryptoIcon("BNB", size = 18.dp)
                            Text("BNB", color = BinanceTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Column {
                            Text(bnbPrice, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(bnbChangeText, color = bnbColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        SparklineChart(
                            points = sparklinePoints,
                            color = bnbColor,
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        )
                    }
                }
            }
        }

        // 6. Tabs, Header & Coin Market List directly on screen background
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tabs row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("New", "Gainers", "Losers", "24h Vol", "Market Cap").forEach { tabName ->
                        val selected = tabName == "Market Cap"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "$tabName selected", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = tabName,
                                color = if (selected) BinanceTextPrimary else BinanceTextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            if (selected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(BinanceGold)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Crypto", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Name", color = BinanceTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                    Text("Last Price", color = BinanceTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    Text("Cap/ Vol", color = BinanceTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Market Rows
                val cryptos = listOf(
                    Triple("BTC", "$1.26T", "$27.61B"),
                    Triple("ETH", "$210.53B", "$8.70B"),
                    Triple("BNB", "$76.88B", "$1.07B"),
                    Triple("SOL", "$87.41B", "$4.12B"),
                    Triple("DOGE", "$23.10B", "$1.84B")
                )

                cryptos.forEachIndexed { index, (symbol, cap, vol) ->
                    val ticker = tickers[symbol + "USDT"]
                    val lastPrice = ticker?.lastPrice ?: when (symbol) {
                        "BTC" -> "63,082.66"
                        "ETH" -> "1,748.66"
                        "BNB" -> "571.68"
                        "SOL" -> "195.00"
                        "DOGE" -> "0.1624"
                        else -> "1.00"
                    }
                    val changePercent = ticker?.priceChangePercent ?: when (symbol) {
                        "BTC" -> "+1.79"
                        "ETH" -> "+0.64"
                        "BNB" -> "+1.04"
                        "SOL" -> "+1.25"
                        "DOGE" -> "+3.40"
                        else -> "0.00"
                    }
                    val isPositive = !changePercent.startsWith("-")
                    val percentColor = if (isPositive) BinanceGreen else BinanceRed
                    val percentText = if (isPositive && !changePercent.startsWith("+")) "+$changePercent" else changePercent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectPair(symbol + "USDT")
                                viewModel.showTradingView.value = true
                            }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Name
                        Row(
                            modifier = Modifier.weight(1.2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CryptoIcon(symbol, size = 26.dp)
                            Text(symbol, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // Last Price
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            val displayPrice = if (lastPrice.startsWith("$")) lastPrice else "$$lastPrice"
                            Text(displayPrice, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$percentText%", color = percentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Cap / Vol
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(cap, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(vol, color = BinanceTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 9. Discover Sub-tab bar
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Discover", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(BinanceGold))
                    }
                    Text("Following", color = BinanceTextSecondary, fontSize = 13.sp)
                    Text("Campaign", color = BinanceTextSecondary, fontSize = 13.sp)
                    Text("Smart Money", color = BinanceTextSecondary, fontSize = 13.sp)
                }
                Icon(Icons.Filled.KeyboardArrowDown, "More", tint = BinanceTextSecondary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun BinanceMenuIcon(
    modifier: Modifier = Modifier,
    color: Color = BinanceTextPrimary
) {
    Canvas(modifier = modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        val padding = strokeWidth * 0.5f
        val startX = padding
        val endX = w - padding
        
        // Top line
        drawLine(
            color = color,
            start = Offset(startX, h * 0.25f),
            end = Offset(endX, h * 0.25f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Middle line
        drawLine(
            color = color,
            start = Offset(startX, h * 0.5f),
            end = Offset(endX, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Bottom line
        drawLine(
            color = color,
            start = Offset(startX, h * 0.75f),
            end = Offset(endX, h * 0.75f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun BinanceProfileIcon(
    modifier: Modifier = Modifier,
    color: Color = BinanceTextSecondary
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()
        
        // Coin circle centered at 0.62w, 0.35h
        val coinCenter = Offset(w * 0.62f, h * 0.35f)
        val coinRadius = w * 0.16f
        drawCircle(
            color = color,
            radius = coinRadius,
            center = coinCenter,
            style = Stroke(width = strokeWidth)
        )
        
        // Diamond inside the coin
        val diamondPath = Path().apply {
            val cx = coinCenter.x
            val cy = coinCenter.y
            val r = coinRadius * 0.5f
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        drawPath(
            path = diamondPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        // Hand outline path
        val handPath = Path().apply {
            // Bottom wrist segment
            moveTo(w * 0.35f, h * 0.82f)
            lineTo(w * 0.52f, h * 0.82f)
            
            // Back of hand & finger on the right
            moveTo(w * 0.52f, h * 0.82f)
            quadraticTo(w * 0.80f, h * 0.82f, w * 0.80f, h * 0.62f)
            quadraticTo(w * 0.80f, h * 0.45f, w * 0.58f, h * 0.45f)
            
            // Thumb & palm on the left
            moveTo(w * 0.35f, h * 0.82f)
            quadraticTo(w * 0.20f, h * 0.82f, w * 0.20f, h * 0.68f)
            quadraticTo(w * 0.20f, h * 0.52f, w * 0.32f, h * 0.52f)
            quadraticTo(w * 0.45f, h * 0.52f, w * 0.48f, h * 0.62f)
        }
        
        drawPath(
            path = handPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun BinanceScannerIcon(
    modifier: Modifier = Modifier,
    color: Color = BinanceTextSecondary
) {
    Canvas(modifier = modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        val padding = strokeWidth * 0.5f
        val left = padding
        val right = w - padding
        val top = padding
        val bottom = h - padding
        val bracketLength = (right - left) * 0.25f
        
        // Top-Left bracket
        val tlPath = Path().apply {
            moveTo(left, top + bracketLength)
            lineTo(left, top)
            lineTo(left + bracketLength, top)
        }
        drawPath(tlPath, color, style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Miter))
        
        // Top-Right bracket
        val trPath = Path().apply {
            moveTo(right - bracketLength, top)
            lineTo(right, top)
            lineTo(right, top + bracketLength)
        }
        drawPath(trPath, color, style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Miter))
        
        // Bottom-Left bracket
        val blPath = Path().apply {
            moveTo(left, bottom - bracketLength)
            lineTo(left, bottom)
            lineTo(left + bracketLength, bottom)
        }
        drawPath(blPath, color, style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Miter))
        
        // Bottom-Right bracket
        val brPath = Path().apply {
            moveTo(right - bracketLength, bottom)
            lineTo(right, bottom)
            lineTo(right, bottom - bracketLength)
        }
        drawPath(brPath, color, style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Miter))
        
        // Middle horizontal line (minus)
        val midY = h / 2f
        val lineStart = left + (right - left) * 0.22f
        val lineEnd = left + (right - left) * 0.78f
        drawLine(
            color = color,
            start = Offset(lineStart, midY),
            end = Offset(lineEnd, midY),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun BinanceMessageIcon(
    modifier: Modifier = Modifier,
    color: Color = BinanceTextSecondary
) {
    Canvas(modifier = modifier.size(19.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        val padding = strokeWidth * 0.5f
        val left = padding
        val right = w - padding
        val top = padding
        val bottomBox = h * 0.75f
        val bottomTotal = h - padding
        val cornerRadius = 2.5.dp.toPx()
        
        val rectWidth = right - left
        val rectHeight = bottomBox - top
        
        // Outer path for speech bubble (mathematically perfect, no overlap, rounded corners)
        val bubblePath = Path().apply {
            // Start at top-left
            moveTo(left + cornerRadius, top)
            lineTo(right - cornerRadius, top)
            quadraticTo(right, top, right, top + cornerRadius)
            
            // Right edge
            lineTo(right, bottomBox - cornerRadius)
            quadraticTo(right, bottomBox, right - cornerRadius, bottomBox)
            
            // Bottom edge to start of tail
            lineTo(left + rectWidth * 0.35f, bottomBox)
            
            // Tail tip
            lineTo(left + rectWidth * 0.10f, bottomTotal)
            
            // Back to bottom-left corner
            lineTo(left, bottomBox)
            
            // Left edge
            lineTo(left, top + cornerRadius)
            quadraticTo(left, top, left + cornerRadius, top)
            close()
        }
        
        drawPath(
            path = bubblePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
        
        // 2 horizontal lines inside the bubble (uniform thickness, matching stroke)
        val startX = left + rectWidth * 0.25f
        val endX1 = left + rectWidth * 0.75f
        val endX2 = left + rectWidth * 0.60f
        
        // Line 1 (top, longer)
        drawLine(
            color = color,
            start = Offset(startX, top + rectHeight * 0.35f),
            end = Offset(endX1, top + rectHeight * 0.35f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Line 2 (bottom, shorter)
        drawLine(
            color = color,
            start = Offset(startX, top + rectHeight * 0.65f),
            end = Offset(endX2, top + rectHeight * 0.65f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun WendysLogo(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFE22127)), // Wendy's red
        contentAlignment = Alignment.Center
    ) {
        Text("W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun FiatScannerIcon(
    modifier: Modifier = Modifier,
    color: Color = BinanceTextSecondary
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()
        
        // Card rounded rectangle frame
        val rectPath = Path().apply {
            val left = strokeWidth * 0.5f
            val top = h * 0.15f
            val right = w - strokeWidth * 0.5f
            val bottom = h * 0.85f
            val corner = 2.dp.toPx()
            
            moveTo(left + corner, top)
            lineTo(right - corner, top)
            quadraticTo(right, top, right, top + corner)
            lineTo(right, bottom - corner)
            quadraticTo(right, bottom, right - corner, bottom)
            lineTo(left + corner, bottom)
            quadraticTo(left, bottom, left, bottom - corner)
            lineTo(left, top + corner)
            quadraticTo(left, top, left + corner, top)
            close()
        }
        
        drawPath(
            path = rectPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )
        
        // Credit card magnetic stripe
        drawLine(
            color = color,
            start = Offset(0f, h * 0.40f),
            end = Offset(w, h * 0.40f),
            strokeWidth = strokeWidth
        )
        
        // Small card chip on bottom left
        val chipSize = w * 0.22f
        drawRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.55f),
            size = androidx.compose.ui.geometry.Size(chipSize, chipSize)
        )
    }
}

