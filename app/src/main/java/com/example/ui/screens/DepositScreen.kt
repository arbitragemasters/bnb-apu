package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CryptoViewModel
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val assets by viewModel.walletAssets.collectAsState()

    var selectedCoin by remember { mutableStateOf("USDT") }
    var depositAmount by remember { mutableStateOf("1000") }
    var selectedNetwork by remember { mutableStateOf("TRC20") }
    
    var showCoinDropdown by remember { mutableStateOf(false) }
    var showNetworkDropdown by remember { mutableStateOf(false) }

    val networks = listOf("TRC20", "ERC20", "BEP20 (BSC)", "Solana")
    val mockAddress = when (selectedNetwork) {
        "TRC20" -> "TYsV2vM78X8ZkH8x6Zf89Y7HhgS8XgK3jS"
        "ERC20" -> "0xf7a90802e3b2b2a63339df5fa71c6a7bc60ab20e"
        "BEP20 (BSC)" -> "0x615ba802e3b2b2a63339df5fa71c6a7bc60ab2f2"
        else -> "7xH8sk9XgKS8vJz6Zf89Y7HhgS8XgK3jSTY90802eb"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceDarkBg)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    "Deposit Crypto",
                    color = BinanceTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BinanceTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDarkSurface)
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Title card
                Card(
                    colors = CardDefaults.cardColors(containerColor = BinanceCardBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = BinanceGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Specify the amount you want to credit to your balance. The simulation will deposit it directly into your local database wallet.",
                            color = BinanceTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Coin Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Select Coin", color = BinanceTextSecondary, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BinancePillBg)
                            .clickable { showCoinDropdown = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CryptoIcon(selectedCoin, size = 24.dp)
                                Text(selectedCoin, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = BinanceTextPrimary)
                        }

                        DropdownMenu(
                            expanded = showCoinDropdown,
                            onDismissRequest = { showCoinDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(BinanceDarkSurface)
                        ) {
                            assets.forEach { asset ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CryptoIcon(asset.coinSymbol, size = 20.dp)
                                            Text(asset.coinSymbol, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                                            Text("(${asset.coinName})", color = BinanceTextSecondary, fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedCoin = asset.coinSymbol
                                        showCoinDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Network Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Deposit Network", color = BinanceTextSecondary, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BinancePillBg)
                            .clickable { showNetworkDropdown = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedNetwork, color = BinanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Icon(Icons.Filled.ArrowDropDown, "Dropdown", tint = BinanceTextPrimary)
                        }

                        DropdownMenu(
                            expanded = showNetworkDropdown,
                            onDismissRequest = { showNetworkDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(BinanceDarkSurface)
                        ) {
                            networks.forEach { net ->
                                DropdownMenuItem(
                                    text = { Text(net, color = BinanceTextPrimary) },
                                    onClick = {
                                        selectedNetwork = net
                                        showNetworkDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Amount Input Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Deposit Amount (Simulated)", color = BinanceTextSecondary, fontSize = 12.sp)
                    TextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BinancePillBg,
                            unfocusedContainerColor = BinancePillBg,
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary,
                            focusedIndicatorColor = BinanceGold,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Text(selectedCoin, color = BinanceGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // High-fidelity QR Code Block with custom drawing on Canvas
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        val cells = 21
                        val cellSize = size.width / cells

                        // Draw clean white background first
                        drawRect(
                            color = Color.White,
                            size = size
                        )

                        for (r in 0 until cells) {
                            for (c in 0 until cells) {
                                // 1. Finder pattern top-left
                                if (r < 7 && c < 7) {
                                    val lr = r
                                    val lc = c
                                    val isBlack = lr == 0 || lr == 6 || lc == 0 || lc == 6 || 
                                                  (lr in 2..4 && lc in 2..4)
                                    if (isBlack) {
                                        drawRect(
                                            color = Color(0xFF1E2329),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                                // 2. Finder pattern top-right
                                else if (r < 7 && c >= 14) {
                                    val lr = r
                                    val lc = c - 14
                                    val isBlack = lr == 0 || lr == 6 || lc == 0 || lc == 6 || 
                                                  (lr in 2..4 && lc in 2..4)
                                    if (isBlack) {
                                        drawRect(
                                            color = Color(0xFF1E2329),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                                // 3. Finder pattern bottom-left
                                else if (r >= 14 && c < 7) {
                                    val lr = r - 14
                                    val lc = c
                                    val isBlack = lr == 0 || lr == 6 || lc == 0 || lc == 6 || 
                                                  (lr in 2..4 && lc in 2..4)
                                    if (isBlack) {
                                        drawRect(
                                            color = Color(0xFF1E2329),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                                // 4. Center logo cutout (skip drawing QR pixels)
                                else if (r in 8..12 && c in 8..12) {
                                    // Left blank for Binance logo
                                }
                                // 5. Timing patterns
                                else if (r == 6) {
                                    if (c % 2 == 0) {
                                        drawRect(
                                            color = Color(0xFF1E2329),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                                else if (c == 6) {
                                    if (r % 2 == 0) {
                                        drawRect(
                                            color = Color(0xFF1E2329),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                                // 6. Regular QR pixels (random noise)
                                else {
                                    val hash = (r * 157 + c * 313) xor (r * c)
                                    val isBlack = hash % 2 == 0
                                    if (isBlack) {
                                        drawRect(
                                            color = Color(0xFF1E2329),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }

                        // Draw a clean, crisp Binance logo right in the center cutout!
                        val centerPx = size.width / 2
                        val centerPy = size.height / 2
                        
                        // Draw a white circle backdrop in the center so the logo pops and doesn't overlap cells
                        val backdropRadius = cellSize * 2.8f
                        drawCircle(
                            color = Color.White,
                            radius = backdropRadius,
                            center = Offset(centerPx, centerPy)
                        )

                        // Draw the Binance logo inside the backdrop
                        withTransform({
                            rotate(45f, pivot = Offset(centerPx, centerPy))
                        }) {
                            val yellowColor = Color(0xFFF0B90B)
                            val outerSize = cellSize * 3.4f
                            val midSize = cellSize * 2.2f
                            val innerSize = cellSize * 1.0f

                            // 1. Outer Diamond
                            drawRect(
                                color = yellowColor,
                                topLeft = Offset(centerPx - outerSize / 2, centerPy - outerSize / 2),
                                size = androidx.compose.ui.geometry.Size(outerSize, outerSize)
                            )

                            // 2. Cutout to split the outer diamond into 4 corner tips
                            val gapWidth = cellSize * 0.45f
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(centerPx - outerSize, centerPy - gapWidth / 2),
                                size = androidx.compose.ui.geometry.Size(outerSize * 2, gapWidth)
                            )
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(centerPx - gapWidth / 2, centerPy - outerSize),
                                size = androidx.compose.ui.geometry.Size(gapWidth, outerSize * 2)
                            )

                            // 3. Draw white inner background square
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(centerPx - midSize / 2, centerPy - midSize / 2),
                                size = androidx.compose.ui.geometry.Size(midSize, midSize)
                            )

                            // 4. Center yellow diamond
                            drawRect(
                                color = yellowColor,
                                topLeft = Offset(centerPx - innerSize / 2, centerPy - innerSize / 2),
                                size = androidx.compose.ui.geometry.Size(innerSize, innerSize)
                            )
                        }
                    }
                }

                // Clickable Address Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BinancePillBg)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(mockAddress))
                            Toast.makeText(context, "Address copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deposit Address", color = BinanceTextSecondary, fontSize = 11.sp)
                        Icon(Icons.Filled.ContentCopy, "Copy", tint = BinanceGold, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = mockAddress,
                        color = BinanceTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Extra padding to ensure the scrollable area doesn't cover elements under bottom button
                Spacer(modifier = Modifier.height(100.dp))
            }

            // Pinned Bottom Button Area
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(BinanceDarkBg)
            ) {
                HorizontalDivider(color = BinanceDivider.copy(alpha = 0.4f), thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val amount = depositAmount.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.depositSimulatedFunds(selectedCoin, amount)
                            Toast.makeText(context, "SUCCESS: Credited ${depositAmount} ${selectedCoin} to your wallet!", Toast.LENGTH_LONG).show()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BinanceGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Confirm Simulated Deposit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

