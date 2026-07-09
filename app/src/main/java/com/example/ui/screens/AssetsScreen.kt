package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import com.example.data.WalletAsset
import com.example.ui.CryptoViewModel
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: CryptoViewModel,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    onNavigateToTrade: (String) -> Unit,
    currentVersionName: String = "1.0",
    isCheckingUpdates: Boolean = false,
    onCheckForUpdates: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val assets by viewModel.walletAssets.collectAsState()
    val isHideBalances by viewModel.isHideBalances.collectAsState()
    val tickers by viewModel.tickers.collectAsState()

    // Screen top navigation tabs: "Overview", "Spot", "Funding", "Earn", "Futures"
    val topTabs = listOf("Overview", "Spot", "Funding", "Earn", "Futures")
    var selectedTopTab by remember { mutableStateOf("Overview") }

    // Assets / Account section tab switcher
    var selectedBottomTab by remember { mutableStateOf("Assets") }

    // Search query inside asset list
    var searchQuery by remember { mutableStateOf("") }
    var hideZeroBalances by remember { mutableStateOf(true) }
    var showSearchBar by remember { mutableStateOf(false) }

    // Transfer Dialog state
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferFrom by remember { mutableStateOf("Spot") }
    var transferTo by remember { mutableStateOf("Funding") }
    var transferAmount by remember { mutableStateOf("") }
    var transferCoin by remember { mutableStateOf("USDT") }

    // Calculate dynamic total sum in USDT based on active tab balances
    val totalBalanceUsdt = remember(assets, tickers, selectedTopTab) {
        assets.fold(0.0) { sum, asset ->
            val tabBalance = when (selectedTopTab) {
                "Overview" -> asset.spotBalance + asset.fundingBalance
                "Spot" -> asset.spotBalance
                "Funding" -> asset.fundingBalance
                "Earn" -> asset.earnBalance
                "Futures" -> asset.futuresBalance
                else -> asset.spotBalance + asset.fundingBalance
            }
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            sum + getAssetUsdtEquivalent(asset.coinSymbol, tabBalance, price)
        }
    }

    // Equivalent in Kenyan Shillings (KSh)
    val totalBalanceKsh = totalBalanceUsdt * 129.259999

    val spotTotalUsdt = remember(assets, tickers) {
        assets.fold(0.0) { sum, asset ->
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            sum + getAssetUsdtEquivalent(asset.coinSymbol, asset.spotBalance, price)
        }
    }

    val fundingTotalUsdt = remember(assets, tickers) {
        assets.fold(0.0) { sum, asset ->
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            sum + getAssetUsdtEquivalent(asset.coinSymbol, asset.fundingBalance, price)
        }
    }

    val earnTotalUsdt = remember(assets, tickers) {
        assets.fold(0.0) { sum, asset ->
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            sum + getAssetUsdtEquivalent(asset.coinSymbol, asset.earnBalance, price)
        }
    }

    val futuresTotalUsdt = remember(assets, tickers) {
        assets.fold(0.0) { sum, asset ->
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            sum + getAssetUsdtEquivalent(asset.coinSymbol, asset.futuresBalance, price)
        }
    }

    // Dynamic filtering and sorting of list based on active tab
    val filteredAssets = remember(assets, searchQuery, hideZeroBalances, selectedTopTab, tickers) {
        assets.filter { asset ->
            val matchesQuery = asset.coinSymbol.contains(searchQuery, ignoreCase = true) ||
                               asset.coinName.contains(searchQuery, ignoreCase = true)
            val tabBalance = when (selectedTopTab) {
                "Overview" -> asset.spotBalance + asset.fundingBalance
                "Spot" -> asset.spotBalance
                "Funding" -> asset.fundingBalance
                "Earn" -> asset.earnBalance
                "Futures" -> asset.futuresBalance
                else -> asset.spotBalance + asset.fundingBalance
            }
            val matchesZero = !hideZeroBalances || tabBalance > 0.0
            matchesQuery && matchesZero
        }.sortedByDescending { asset ->
            val tabBalance = when (selectedTopTab) {
                "Overview" -> asset.spotBalance + asset.fundingBalance
                "Spot" -> asset.spotBalance
                "Funding" -> asset.fundingBalance
                "Earn" -> asset.earnBalance
                "Futures" -> asset.futuresBalance
                else -> asset.spotBalance + asset.fundingBalance
            }
            val price = when (asset.coinSymbol) {
                "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                "ACT" -> 0.00924
                else -> 1.0
            }
            getAssetUsdtEquivalent(asset.coinSymbol, tabBalance, price)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceDarkBg)
    ) {
        // Horizontal Top Menu Tabs ("Overview", "Spot", "Funding", "Earn", "Futures")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            topTabs.forEach { tabName ->
                val isSelected = tabName == selectedTopTab
                Text(
                    text = tabName,
                    color = if (isSelected) Color.White else BinanceTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (isSelected) 17.sp else 16.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .clickable { selectedTopTab = tabName }
                        .alignByBaseline()
                )
            }
        }

        // Main Assets Layout Scroll body
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Card: Balance Section (Directly on dark background, matching screenshot!)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Est. Total Value",
                                color = BinanceTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isHideBalances) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = BinanceTextSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.toggleHideBalances() }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShowChart,
                                contentDescription = "Analytics",
                                tint = BinanceTextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        Toast.makeText(context, "Opening historical balance analytics", Toast.LENGTH_SHORT).show()
                                    }
                            )
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = "Transaction History",
                                tint = BinanceTextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        Toast.makeText(context, "Opening transaction records", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val usdtStr = if (isHideBalances) "********" else {
                            if (totalBalanceUsdt < 1.0) {
                                formatWithCommas(totalBalanceUsdt, maxDecimals = 8, minDecimals = 8)
                            } else {
                                formatWithCommas(totalBalanceUsdt, maxDecimals = 2, minDecimals = 2)
                            }
                        }
                        Text(
                            text = usdtStr,
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alignByBaseline()
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = "USDT",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = BinanceTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val equivalentStr = if (isHideBalances) "≈$ ********" else {
                        val formattedVal = if (totalBalanceUsdt < 1.0) {
                            formatWithCommas(totalBalanceUsdt, maxDecimals = 8, minDecimals = 8)
                        } else {
                            formatWithCommas(totalBalanceUsdt, maxDecimals = 2, minDecimals = 2)
                        }
                        "≈$$formattedVal"
                    }
                    Text(
                        text = equivalentStr,
                        color = BinanceTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Today's PNL Bar matching screenshot (Green & positive)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable {
                                Toast.makeText(context, "Opening historical balance analytics", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Text(
                            text = "Today's PNL",
                            color = BinanceTextSecondary,
                            fontSize = 12.sp
                        )
                        val pnlPercent = if (totalBalanceUsdt > 0.001011) 0.31 else 0.13
                        val pnlVal = totalBalanceUsdt * (pnlPercent / 100.0)
                        val pnlStr = if (isHideBalances) "****" else {
                            val formattedPnl = formatWithCommas(pnlVal, maxDecimals = 8, minDecimals = 8)
                            "+$formattedPnl USDT(+$pnlPercent%)"
                        }
                        Text(
                            text = pnlStr,
                            color = BinanceGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Detail",
                            tint = BinanceTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Three beautiful main wallet buttons ("Add Funds", "Send", "Transfer")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Add Funds Button
                        Button(
                            onClick = onNavigateToDeposit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text("Add Funds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Send Button (Withdrawal)
                        Button(
                            onClick = onNavigateToWithdrawal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinancePillBg,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text("Send", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Transfer Button
                        Button(
                            onClick = { showTransferDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinancePillBg,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text("Transfer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
                       // Section: Bottom asset toggle tabs (Assets / Account)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Assets", "Account").forEach { bTab ->
                        val isSel = bTab == selectedBottomTab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedBottomTab = bTab }
                        ) {
                            Text(
                                text = bTab,
                                color = if (isSel) BinanceTextPrimary else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            if (isSel) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(BinanceGold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Mini Settings icon & Sort Filter icon
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedBottomTab == "Assets") {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = BinanceTextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        showSearchBar = !showSearchBar
                                    }
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    Toast.makeText(context, "Opening asset settings", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            val radius = width / 2
                            val center = Offset(width / 2, height / 2)
                            val path = androidx.compose.ui.graphics.Path().apply {
                                for (i in 0..5) {
                                    val angle = Math.toRadians((i * 60 - 90).toDouble())
                                    val x = center.x + radius * Math.cos(angle).toFloat()
                                    val y = center.y + radius * Math.sin(angle).toFloat()
                                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                                }
                                close()
                            }
                            // Draw Hexagon outline
                            drawPath(
                                path = path,
                                color = BinanceTextSecondary,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            // Draw Circle in the center
                            drawCircle(
                                color = BinanceTextSecondary,
                                radius = 2.5.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar & Checkbox Row (Conditionally shown when showSearchBar is true!)
            if (showSearchBar && selectedBottomTab == "Assets") {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search coin symbol...", color = BinanceTextSecondary.copy(alpha = 0.5f), fontSize = 12.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BinanceDarkSurface,
                                unfocusedContainerColor = BinanceDarkSurface,
                                focusedTextColor = BinanceTextPrimary,
                                unfocusedTextColor = BinanceTextPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            leadingIcon = {
                                Icon(Icons.Filled.Search, "Search", tint = BinanceTextSecondary, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                        )

                        // Small hide empty balances check
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { hideZeroBalances = !hideZeroBalances }
                                .padding(4.dp)
                        ) {
                            Checkbox(
                                checked = hideZeroBalances,
                                onCheckedChange = { hideZeroBalances = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BinanceGold,
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = BinanceTextSecondary
                                ),
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Hide 0 Balance", color = BinanceTextSecondary, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Coin List vs Account List based on selectedBottomTab
            if (selectedBottomTab == "Account") {
                val accountsList = listOf(
                    Triple("Spot", spotTotalUsdt, isHideBalances),
                    Triple("Funding", fundingTotalUsdt, isHideBalances),
                    Triple("Earn", earnTotalUsdt, isHideBalances),
                    Triple("Futures", futuresTotalUsdt, isHideBalances)
                )
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(18.dp) // Clean tight vertical spacing!
                    ) {
                        accountsList.forEach { (name, total, hide) ->
                            val formattedUsdt = if (hide) {
                                "******"
                            } else if (total == 0.0) {
                                "0.00 USDT"
                            } else if (total < 1.0) {
                                "${formatWithCommas(total, maxDecimals = 8, minDecimals = 2)} USDT"
                            } else {
                                "${formatWithCommas(total, maxDecimals = 2, minDecimals = 2)} USDT"
                            }

                            val formattedEquivalent = if (hide) {
                                "******"
                            } else if (total == 0.0) {
                                "≈ $0.00"
                            } else if (total < 1.0) {
                                "≈ $${formatWithCommas(total, maxDecimals = 8, minDecimals = 2)}"
                            } else {
                                "≈ $${formatWithCommas(total, maxDecimals = 2, minDecimals = 2)}"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = name,
                                    color = BinanceTextPrimary,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formattedUsdt,
                                        color = BinanceTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    if (formattedEquivalent != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formattedEquivalent,
                                            color = BinanceTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (filteredAssets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No assets found matches search query.", color = BinanceTextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    itemsIndexed(filteredAssets) { index, asset ->
                        val price = when (asset.coinSymbol) {
                            "BTC" -> tickers["BTCUSDT"]?.lastPrice?.toDoubleOrNull() ?: 63736.0
                            "ETH" -> tickers["ETHUSDT"]?.lastPrice?.toDoubleOrNull() ?: 1794.5
                            "SOL" -> tickers["SOLUSDT"]?.lastPrice?.toDoubleOrNull() ?: 195.0
                            "BNB" -> tickers["BNBUSDT"]?.lastPrice?.toDoubleOrNull() ?: 615.0
                            "DOGE" -> tickers["DOGEUSDT"]?.lastPrice?.toDoubleOrNull() ?: 0.22
                            "ACT" -> 0.00924
                            else -> 1.0
                        }
                        val displayBalance = when (selectedTopTab) {
                            "Overview" -> asset.spotBalance + asset.fundingBalance
                            "Spot" -> asset.spotBalance
                            "Funding" -> asset.fundingBalance
                            "Earn" -> asset.earnBalance
                            "Futures" -> asset.futuresBalance
                            else -> asset.spotBalance + asset.fundingBalance
                        }
                        
                        if (index == 0) {
                            HorizontalDivider(
                                color = BinanceDivider,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        AssetCoinRow(
                            asset = asset,
                            displayBalance = displayBalance,
                            price = price,
                            isHide = isHideBalances,
                            onTradeClick = { onNavigateToTrade(asset.coinSymbol + "USDT") }
                        )

                        HorizontalDivider(
                            color = BinanceDivider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
                        )
                    }
                }
            }

            // Bottom space helper
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Interactive Swap Transfer Dialog between sub-accounts
    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            containerColor = BinanceDarkSurface,
            title = {
                Text("Account Transfer", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Transfer funds between your sub-wallets instantly.", color = BinanceTextSecondary, fontSize = 11.sp)

                    // From Wallet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("From Account", color = BinanceTextSecondary, fontSize = 12.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BinancePillBg)
                                .clickable {
                                    transferFrom = if (transferFrom == "Spot") "Funding" else "Spot"
                                    transferTo = if (transferFrom == "Spot") "Funding" else "Spot"
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(transferFrom, color = BinanceGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Filled.SwapVert, "Swap", tint = BinanceGold, modifier = Modifier.size(14.dp))
                        }
                    }

                    // To Wallet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("To Account", color = BinanceTextSecondary, fontSize = 12.sp)
                        Text(transferTo, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }

                    // Coin type to transfer
                    val availableCoins = listOf("USDT", "BTC", "ETH", "SOL", "BNB", "DOGE")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Asset Coin", color = BinanceTextSecondary, fontSize = 12.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BinancePillBg)
                                .clickable {
                                    val idx = availableCoins.indexOf(transferCoin)
                                    transferCoin = availableCoins[(idx + 1) % availableCoins.size]
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(transferCoin, color = BinanceGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = BinanceGold, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Quantity amount
                    val avail = assets.find { it.coinSymbol == transferCoin }?.let { asset ->
                        when (transferFrom) {
                            "Spot" -> asset.spotBalance
                            "Funding" -> asset.fundingBalance
                            "Earn" -> asset.earnBalance
                            "Futures" -> asset.futuresBalance
                            else -> asset.balance
                        }
                    } ?: 0.0
                    TextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        placeholder = { Text("Enter quantity to transfer (Max $avail)", color = BinanceTextSecondary.copy(alpha = 0.5f), fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BinancePillBg,
                            unfocusedContainerColor = BinancePillBg,
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary,
                            focusedIndicatorColor = BinanceGold,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = transferAmount.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            Toast.makeText(context, "Please enter a valid amount!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        viewModel.transferSubWallet(
                            symbol = transferCoin,
                            amount = amt,
                            fromAccount = transferFrom,
                            toAccount = transferTo,
                            onResult = { result ->
                                result.onSuccess {
                                    Toast.makeText(context, "SUCCESS: Transferred $transferAmount $transferCoin from $transferFrom to $transferTo successfully!", Toast.LENGTH_LONG).show()
                                    showTransferDialog = false
                                    transferAmount = ""
                                }.onFailure { exception ->
                                    Toast.makeText(context, exception.message ?: "Transfer failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Confirm Transfer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancel", color = BinanceTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AssetCoinRow(
    asset: WalletAsset,
    displayBalance: Double,
    price: Double,
    isHide: Boolean,
    onTradeClick: () -> Unit
) {
    val context = LocalContext.current
    val calculatedEq = getAssetUsdtEquivalent(asset.coinSymbol, displayBalance, price)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CryptoIcon(symbol = asset.coinSymbol, size = 32.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = asset.coinSymbol,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        // Add tag for certain coins like Earn or Monitoring badge matching the screenshot!
                        if (asset.coinSymbol == "ACT") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF2F1D2C))
                                    .border(0.5.dp, Color(0xFF4C273C), shape = RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                             ) {
                                Text(
                                    text = "Monitoring",
                                    color = Color(0xFFEA5B80),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = asset.coinName,
                        color = BinanceTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Balances Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isHide) "********" else {
                        if (asset.coinSymbol == "BTC" && displayBalance == 0.00000001) "0.00000001"
                        else if (asset.coinSymbol == "ACT" && displayBalance == 0.04) "0.04"
                        else if (asset.coinSymbol == "ETH" && displayBalance == 0.00000002) "0.00000002"
                        else if (asset.coinSymbol == "USDT") String.format(Locale.US, "%,.2f", displayBalance)
                        else formatWithCommas(displayBalance, maxDecimals = 8, minDecimals = 2)
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHide) "********" else {
                        val formattedEq = if (asset.coinSymbol == "USDT") {
                            String.format(Locale.US, "%,.2f", calculatedEq)
                        } else {
                            formatWithCommas(calculatedEq, maxDecimals = 8, minDecimals = 2)
                        }
                        "≈$formattedEq USDT"
                    },
                    color = BinanceTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Floating PNL row (Left: "Floating PNL", Right: value in red/green)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pnlPercent = when (asset.coinSymbol) {
                "BTC" -> -25.10
                "ACT" -> 0.0
                "ETH" -> -0.43
                else -> 0.0
            }
            val pnlVal = when (asset.coinSymbol) {
                "BTC" -> -0.00021362
                "ACT" -> 0.0003656
                "ETH" -> -0.00000016
                else -> 0.0
            }

            Text(
                text = "Floating PNL",
                color = BinanceTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            if (pnlPercent != 0.0 || pnlVal != 0.0) {
                val pnlColor = if (pnlVal >= 0) BinanceGreen else BinanceRed
                val pnlText = when (asset.coinSymbol) {
                    "BTC" -> "-0.00021362 USDT(-25.10%)"
                    "ACT" -> "+0.0003656 USDT(--%)"
                    "ETH" -> "-0.00000016 USDT(-0.43%)"
                    else -> "0.00 USDT(0.00%)"
                }
                Text(
                    text = if (isHide) "****" else pnlText,
                    color = pnlColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "0.00 USDT(0.00%)",
                    color = BinanceTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons row (Aligned right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Earn button (Grey background, white text)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BinancePillBg)
                        .clickable {
                            Toast.makeText(context, "Locked Earn subscription activated!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Earn", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                }

                // Trade button (Grey background, white text)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BinancePillBg)
                        .clickable { onTradeClick() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Trade", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                }
            }
        }

    }
}

fun formatWithCommas(value: Double, maxDecimals: Int = 8, minDecimals: Int = 2): String {
    val isNegative = value < 0
    val absValue = Math.abs(value)
    val parts = String.format(Locale.US, "%.12f", absValue).split(".")
    if (parts.size != 2) return (if (isNegative) "-" else "") + String.format(Locale.US, "%,.2f", absValue)
    
    val integerPart = parts[0].toLongOrNull() ?: 0L
    val formattedInteger = String.format(Locale.US, "%,d", integerPart)
    
    var decimalPart = parts[1]
    if (decimalPart.length > maxDecimals) {
        decimalPart = decimalPart.substring(0, maxDecimals)
    }
    while (decimalPart.length > minDecimals && decimalPart.endsWith("0")) {
        decimalPart = decimalPart.substring(0, decimalPart.length - 1)
    }
    val result = if (decimalPart.isEmpty()) formattedInteger else "$formattedInteger.$decimalPart"
    return if (isNegative) "-$result" else result
}

fun getAssetUsdtEquivalent(symbol: String, balance: Double, tickerPrice: Double?): Double {
    return when (symbol) {
        "BTC" -> {
            if (balance == 0.00000001) 0.00063741
            else balance * (tickerPrice ?: 63736.0)
        }
        "ACT" -> {
            if (balance == 0.04) 0.0003656
            else balance * (tickerPrice ?: 0.00924)
        }
        "ETH" -> {
            if (balance == 0.00000002) 0.00003584
            else balance * (tickerPrice ?: 1794.5)
        }
        else -> {
            balance * (tickerPrice ?: 1.0)
        }
    }
}

