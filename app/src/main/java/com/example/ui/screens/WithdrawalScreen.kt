package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CryptoViewModel
import com.example.ui.WithdrawalUiState
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.withdrawalState.collectAsState()
    val assets by viewModel.walletAssets.collectAsState()
    val transactions by viewModel.transactionHistory.collectAsState()
    val emailTimer by viewModel.emailTimer.collectAsState()

    var selectedCoin by remember { mutableStateOf("USDT") }
    var enteredAddress by remember { mutableStateOf("") }
    var enteredAmount by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf("TRC20") }

    var showCoinDropdown by remember { mutableStateOf(false) }
    var showNetworkDropdown by remember { mutableStateOf(false) }

    // MFA Verification Inputs
    var emailCodeInput by remember { mutableStateOf("") }
    var authenticatorCodeInput by remember { mutableStateOf("") }

    val networks = listOf("TRC20", "ERC20", "BEP20", "Solana")

    // Filter transaction history specifically for withdrawals
    val withdrawalHistory = remember(transactions) {
        transactions.filter { it.type == "WITHDRAWAL" }
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
                    "Send/Withdrawal Portal",
                    color = BinanceTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.resetWithdrawalFlow()
                    onBack()
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = BinanceTextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BinanceDarkSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when (val current = state) {
                    is WithdrawalUiState.Input, is WithdrawalUiState.InputError -> {
                        // Form Input State
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (current is WithdrawalUiState.InputError) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BinanceRed.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, BinanceRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = current.message,
                                        color = BinanceRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            // 1. Coin Dropdown
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Select Asset Coin", color = BinanceTextSecondary, fontSize = 12.sp)
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
                                            Text(selectedCoin, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
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
                                                        CryptoIcon(asset.coinSymbol, size = 18.dp)
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

                            // 2. Address Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Withdrawal Destination Address", color = BinanceTextSecondary, fontSize = 12.sp)
                                TextField(
                                    value = enteredAddress,
                                    onValueChange = { enteredAddress = it },
                                    placeholder = { Text("Long press or paste wallet address...", color = BinanceTextSecondary.copy(alpha = 0.4f), fontSize = 13.sp) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = BinancePillBg,
                                        unfocusedContainerColor = BinancePillBg,
                                        focusedTextColor = BinanceTextPrimary,
                                        unfocusedTextColor = BinanceTextPrimary,
                                        focusedIndicatorColor = BinanceGold,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // 3. Network Selector
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Withdrawal Blockchain Network", color = BinanceTextSecondary, fontSize = 12.sp)
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
                                        Text(selectedNetwork, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
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

                            // 4. Amount Entry
                            val selectedAsset = assets.find { it.coinSymbol == selectedCoin }
                            val available = selectedAsset?.balance ?: 0.0
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Withdrawal Amount", color = BinanceTextSecondary, fontSize = 12.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val formattedAvail = formatWithCommas(available, maxDecimals = 8, minDecimals = 4)
                                        Text("Available: $formattedAvail $selectedCoin", color = BinanceTextSecondary, fontSize = 11.sp)
                                        Text(
                                            "MAX",
                                            color = BinanceGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.clickable { enteredAmount = available.toString() }
                                        )
                                    }
                                }
                                TextField(
                                    value = enteredAmount,
                                    onValueChange = { enteredAmount = it },
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

                            // Submit Button to Trigger MFA
                            Button(
                                onClick = {
                                    val amt = enteredAmount.toDoubleOrNull() ?: 0.0
                                    viewModel.submitWithdrawalInput(selectedCoin, amt, enteredAddress, selectedNetwork)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Withdraw Funds", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    is WithdrawalUiState.MfaVerification -> {
                        // Multi-factor MFA authentication screen
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BinanceCardBg)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Security, "MFA", tint = BinanceGold, modifier = Modifier.size(28.dp))
                                Column {
                                    Text("Security Verification Required", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Confirming withdrawal of ${current.amount} ${current.coin} to ${current.network}.", color = BinanceTextSecondary, fontSize = 11.sp)
                                }
                            }

                            if (current.errorMessage != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BinanceRed.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, BinanceRed),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = current.errorMessage,
                                        color = BinanceRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // Step 1: Simulated Email Verification
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Email Verification Code", color = BinanceTextSecondary, fontSize = 12.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = emailCodeInput,
                                        onValueChange = { emailCodeInput = it },
                                        placeholder = { Text("Code sent to account email", color = BinanceTextSecondary.copy(alpha = 0.4f), fontSize = 13.sp) },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = BinancePillBg,
                                            unfocusedContainerColor = BinancePillBg,
                                            focusedTextColor = BinanceTextPrimary,
                                            unfocusedTextColor = BinanceTextPrimary,
                                            focusedIndicatorColor = BinanceGold,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.startEmailTimer()
                                            // Show mock system notification toast containing email code so user can actually copy-paste it!
                                            Toast.makeText(context, "System Simulation Email Code: ${current.generatedEmailCode}", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (emailTimer > 0) BinancePillBg else BinanceGold,
                                            contentColor = if (emailTimer > 0) BinanceTextSecondary else Color.Black
                                        ),
                                        enabled = emailTimer <= 0,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text(
                                            text = if (emailTimer > 0) "${emailTimer}s" else "Get Code",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Helpful instruction tip showing code so user doesn't get stuck
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BinanceGold.copy(alpha = 0.12f))
                                        .clickable {
                                            emailCodeInput = current.generatedEmailCode
                                        }
                                        .padding(8.dp)
                                ) {
                                    Icon(Icons.Outlined.Email, "Tip", tint = BinanceGold, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Simulation Email Code generated: ${current.generatedEmailCode} (Click to auto-fill)",
                                        color = BinanceGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Step 2: Google Authenticator Code
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Google Authenticator 2FA", color = BinanceTextSecondary, fontSize = 12.sp)
                                TextField(
                                    value = authenticatorCodeInput,
                                    onValueChange = { authenticatorCodeInput = it },
                                    placeholder = { Text("6-digit rotating 2FA authenticator code", color = BinanceTextSecondary.copy(alpha = 0.4f), fontSize = 13.sp) },
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
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Tip showing authenticator code
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BinanceGreen.copy(alpha = 0.12f))
                                        .clickable {
                                            authenticatorCodeInput = current.generatedGfaCode
                                        }
                                        .padding(8.dp)
                                ) {
                                    Icon(Icons.Filled.Shield, "GFA", tint = BinanceGreen, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Google Authenticator rotating code: ${current.generatedGfaCode} (Click to auto-fill)",
                                        color = BinanceGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.resetWithdrawalFlow() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinancePillBg, contentColor = BinanceTextPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.verifyWithdrawalMfa(emailCodeInput, authenticatorCodeInput)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(48.dp)
                                ) {
                                    Text("Verify & Send", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    is WithdrawalUiState.Submitting -> {
                        // Submitting spinner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = BinanceGold)
                                Text("Securing withdrawal pipeline...", color = BinanceTextSecondary, fontSize = 14.sp)
                            }
                        }
                    }

                    is WithdrawalUiState.Success -> {
                        // Success Confirmation
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Success",
                                tint = BinanceGreen,
                                modifier = Modifier.size(64.dp)
                            )

                            Text("Withdrawal Request Submitted!", color = BinanceTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BinanceCardBg)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Amount", color = BinanceTextSecondary, fontSize = 13.sp)
                                    Text("${current.amount} ${current.coin}", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Network", color = BinanceTextSecondary, fontSize = 13.sp)
                                    Text(current.network, color = BinanceTextPrimary, fontSize = 13.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Address", color = BinanceTextSecondary, fontSize = 13.sp)
                                    Text(current.address.take(15) + "...", color = BinanceTextPrimary, fontSize = 13.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Status", color = BinanceTextSecondary, fontSize = 13.sp)
                                    Text("PENDING (Awaiting blockchain verification)", color = BinanceGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Text(
                                text = "Note: This withdrawal transaction has been dispatched in the background. In the real-time blockchain simulation below, watch it confirm live!",
                                color = BinanceTextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.resetWithdrawalFlow()
                                    enteredAddress = ""
                                    enteredAmount = ""
                                    emailCodeInput = ""
                                    authenticatorCodeInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text("New Withdrawal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Real-time Blockchain Simulation Transaction History list
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BinanceDivider)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Live Blockchain Transits",
                        color = BinanceTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BinanceGreen)
                        )
                        Text("Simulated Blockchain Engine Active", color = BinanceGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (withdrawalHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No withdrawal records found.", color = BinanceTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(withdrawalHistory) { record ->
                    TransactionTransitItem(record)
                }
            }
        }
    }
}

@Composable
fun TransactionTransitItem(record: com.example.data.TransactionRecord) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val dateStr = formatter.format(Date(record.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = BinanceDarkSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(width = 0.5.dp, color = BinanceDivider, shape = RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "Withdraw",
                        tint = BinanceRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Withdrawal ${record.coinSymbol}", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Beautiful blinking status badges matching blockchain steps!
                val badgeColor = when (record.status) {
                    "PENDING" -> BinanceGold
                    "PROCESSING" -> Color(0xFF3B82F6) // Blue
                    "COMPLETED" -> BinanceGreen
                    else -> BinanceRed
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = record.status,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Amount", color = BinanceTextSecondary, fontSize = 12.sp)
                Text("-${record.amount} ${record.coinSymbol}", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Destination Address", color = BinanceTextSecondary, fontSize = 12.sp)
                Text(record.address?.take(18) + "...", color = BinanceTextPrimary, fontSize = 11.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TxID", color = BinanceTextSecondary, fontSize = 12.sp)
                Text(record.txHash.take(18) + "...", color = BinanceGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Confirmations", color = BinanceTextSecondary, fontSize = 12.sp)
                Text(
                    text = when (record.status) {
                        "PENDING" -> "0 / 12 (Awaiting block)"
                        "PROCESSING" -> "4 / 12 (Broadcasting)"
                        "COMPLETED" -> "12 / 12 (Confirmed)"
                        else -> "Failed"
                    },
                    color = BinanceTextPrimary,
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Timestamp", color = BinanceTextSecondary, fontSize = 12.sp)
                Text(dateStr, color = BinanceTextSecondary, fontSize = 11.sp)
            }
        }
    }
}

