package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            Spacer(modifier = Modifier.height(10.dp))

            // Mock QR Code Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Generates beautiful grid of pixels resembling QR code
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val cells = 15
                    val cellSize = size.width / cells
                    for (r in 0 until cells) {
                        for (c in 0 until cells) {
                            // Draw corner finding patterns
                            val isCorner = (r < 4 && c < 4) || (r < 4 && c >= cells - 4) || (r >= cells - 4 && c < 4)
                            if (isCorner) {
                                val isBorder = r == 0 || r == 3 || c == 0 || c == 3 ||
                                               (r == 0 && c >= cells - 4) || (r == 3 && c >= cells - 4) ||
                                               (c == cells - 4 && r < 4) || (c == cells - 1 && r < 4) ||
                                               (r == cells - 4 && c < 4) || (r == cells - 1 && c < 4) ||
                                               (c == 0 && r >= cells - 4) || (c == 3 && r >= cells - 4)
                                drawRect(
                                    color = if (isBorder) Color.Black else Color.White,
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                )
                            } else {
                                // Draw random mock QR dots
                                val rand = (r * 13 + c * 37) % 2 == 0
                                if (rand) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                    )
                                }
                            }
                        }
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

            Spacer(modifier = Modifier.weight(1f))

            // Execute simulated deposit action
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
