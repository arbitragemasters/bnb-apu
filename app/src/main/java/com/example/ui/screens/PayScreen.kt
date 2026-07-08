package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.CryptoIcon
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val walletAssets by viewModel.walletAssets.collectAsState()

    var destinationDetails by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var selectedCoin by remember { mutableStateOf("USDT") }

    var isSubmitting by remember { mutableStateOf(false) }
    var isPinRequired by remember { mutableStateOf(false) }
    var payPinInput by remember { mutableStateOf("") }
    var transferReceipt by remember { mutableStateOf<PayReceipt?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceDarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceDarkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = BinanceTextPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Binance Pay",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )
        }

        if (transferReceipt != null) {
            // Receipt Details View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.OfflinePin, "Success", tint = BinanceGreen, modifier = Modifier.size(64.dp))
                Text("Binance Pay Completed", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = BinanceDarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recipient ID", color = BinanceTextSecondary, fontSize = 13.sp)
                            Text(transferReceipt!!.recipient, color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Method", color = BinanceTextSecondary, fontSize = 13.sp)
                            Text("Funding Balance", color = BinanceTextPrimary, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Sent", color = BinanceTextSecondary, fontSize = 13.sp)
                            Text("${transferReceipt!!.amount} ${transferReceipt!!.coin}", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction ID", color = BinanceTextSecondary, fontSize = 13.sp)
                            Text(transferReceipt!!.payId, color = BinanceGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        transferReceipt = null
                        destinationDetails = ""
                        transferAmount = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        } else if (isPinRequired) {
            // PIN Entry Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter Pay PIN", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Enter your 6-digit payment confirmation PIN to execute transfer.", color = BinanceTextSecondary, fontSize = 13.sp)

                TextField(
                    value = payPinInput,
                    onValueChange = { if (it.length <= 6) payPinInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BinanceDarkSurface,
                        unfocusedContainerColor = BinanceDarkSurface,
                        focusedTextColor = BinanceTextPrimary,
                        unfocusedTextColor = BinanceTextPrimary,
                        focusedIndicatorColor = BinanceGold,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Hint: PIN can be any 6-digits for this simulation.", color = BinanceTextSecondary, fontSize = 11.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isPinRequired = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceTextPrimary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (payPinInput.length != 6) {
                                Toast.makeText(context, "PIN must be exactly 6 digits!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            // Execute simulated transfer deduction
                            isPinRequired = false
                            isSubmitting = true
                            
                            val amount = transferAmount.toDoubleOrNull() ?: 0.0
                            // Subtract from balance
                            val asset = walletAssets.find { it.coinSymbol == selectedCoin }
                            if (asset != null && asset.balance >= amount) {
                                viewModel.executeTrade(
                                    isBuy = false,
                                    price = 1.0, // base 1 USDT for transfer
                                    amount = amount
                                )
                                transferReceipt = PayReceipt(
                                    recipient = destinationDetails,
                                    amount = amount,
                                    coin = selectedCoin,
                                    payId = (10000000..99999999).random().toString()
                                )
                            } else {
                                Toast.makeText(context, "Deduction failed: Insufficient balance", Toast.LENGTH_SHORT).show()
                            }
                            isSubmitting = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Confirm PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Main Input Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Transfer to another Binance user with zero network gas fees.",
                    color = BinanceTextSecondary,
                    fontSize = 13.sp
                )

                Text("Recipient Information", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextField(
                    value = destinationDetails,
                    onValueChange = { destinationDetails = it },
                    placeholder = { Text("Enter Email, Phone or Pay ID...", color = BinanceTextSecondary, fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BinanceDarkSurface,
                        unfocusedContainerColor = BinanceDarkSurface,
                        focusedTextColor = BinanceTextPrimary,
                        unfocusedTextColor = BinanceTextPrimary,
                        focusedIndicatorColor = BinanceGold,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Coin", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("USDT", "BTC", "ETH", "BNB").forEach { coin ->
                        val isSelected = selectedCoin == coin
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BinanceCardBg else BinanceDarkSurface)
                                .border(1.dp, if (isSelected) BinanceGold else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { selectedCoin = coin }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(coin, color = if (isSelected) BinanceGold else BinanceTextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Text("Transfer Amount", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextField(
                    value = transferAmount,
                    onValueChange = { transferAmount = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BinanceDarkSurface,
                        unfocusedContainerColor = BinanceDarkSurface,
                        focusedTextColor = BinanceTextPrimary,
                        unfocusedTextColor = BinanceTextPrimary,
                        focusedIndicatorColor = BinanceGold,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                val balanceAsset = walletAssets.find { it.coinSymbol == selectedCoin }
                val availVal = balanceAsset?.balance ?: 0.0
                Text(
                    text = "Funding Balance Available: ${formatWithCommas(availVal, maxDecimals = 8, minDecimals = 4)} $selectedCoin",
                    color = BinanceTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val amount = transferAmount.toDoubleOrNull() ?: 0.0
                        if (destinationDetails.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter recipient details!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amount <= 0.0) {
                            Toast.makeText(context, "Please enter a valid transfer amount!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val bal = balanceAsset?.balance ?: 0.0
                        if (bal < amount) {
                            Toast.makeText(context, "Insufficient $selectedCoin balance available!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Authenticate PIN
                        payPinInput = ""
                        isPinRequired = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Pay Instantly", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

data class PayReceipt(
    val recipient: String,
    val amount: Double,
    val coin: String,
    val payId: String
)

