package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CryptoViewModel
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PayScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.WithdrawalScreen
import com.example.ui.theme.*

import androidx.compose.ui.res.painterResource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val viewModel: CryptoViewModel = viewModel()
    val context = LocalContext.current

    // Active screen navigation tab: "Home", "Deposit", "Scan", "Pay", "Assets"
    var activeTab by remember { mutableStateOf("Assets") } // Start on Assets tab to show the replica wallet directly!

    // Overlay dialog state for Smiley Face helper
    var showSmileyTip by remember { mutableStateOf(false) }

    // Secondary sub-screen routes (like direct full-screen withdrawal or deposit)
    var isWithdrawalFullFlow by remember { mutableStateOf(false) }
    var isDepositFullFlow by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BinanceDarkBg),
        bottomBar = {
            if (!isWithdrawalFullFlow && !isDepositFullFlow && activeTab != "Scan") {
                Column {
                    HorizontalDivider(color = BinanceDivider, thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BinanceDarkSurface)
                            .navigationBarsPadding()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab definitions with custom visual layouts matching Binance
                        val navigationItems = listOf(
                            Triple("Home", R.drawable.ic_bottom_home, "Home"),
                            Triple("Deposit", R.drawable.ic_bottom_deposit, "Deposit"),
                            Triple("Scan", R.drawable.ic_bottom_scan, "Scan"),
                            Triple("Pay", R.drawable.ic_bottom_pay, "Pay"),
                            Triple("Assets", R.drawable.ic_bottom_assets, "Assets")
                        )

                        navigationItems.forEach { (name, iconRes, contentDescription) ->
                            val isSelected = activeTab == name
                            val itemColor = if (isSelected) Color.White else BinanceTextSecondary

                            Column(
                                modifier = Modifier
                                    .clickable {
                                        activeTab = name
                                        if (name == "Home") {
                                            viewModel.showTradingView.value = false
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                BottomBarCustomIcon(
                                    name = name,
                                    itemColor = itemColor,
                                    isSelected = isSelected
                                )
                                Text(
                                    text = name,
                                    color = itemColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isWithdrawalFullFlow -> {
                    WithdrawalScreen(
                        viewModel = viewModel,
                        onBack = { isWithdrawalFullFlow = false }
                    )
                }
                isDepositFullFlow -> {
                    DepositScreen(
                        viewModel = viewModel,
                        onBack = { isDepositFullFlow = false }
                    )
                }
                else -> {
                    when (activeTab) {
                        "Home" -> {
                            HomeScreen(viewModel = viewModel)
                        }
                        "Deposit" -> {
                            DepositScreen(
                                viewModel = viewModel,
                                onBack = { activeTab = "Assets" }
                            )
                        }
                        "Scan" -> {
                            ScanScreen(
                                onAddressScanned = { _ -> },
                                onBack = { activeTab = "Assets" }
                            )
                        }
                        "Pay" -> {
                            PayScreen(
                                viewModel = viewModel,
                                onBack = { activeTab = "Assets" }
                            )
                        }
                        "Assets" -> {
                            AssetsScreen(
                                viewModel = viewModel,
                                onNavigateToDeposit = { isDepositFullFlow = true },
                                onNavigateToWithdrawal = { isWithdrawalFullFlow = true },
                                onNavigateToTrade = { pair ->
                                    viewModel.selectPair(pair)
                                    viewModel.showTradingView.value = true
                                    activeTab = "Home"
                                }
                            )
                        }
                    }
                }
            }

            // Smiley face overlay message modal
            if (showSmileyTip) {
                AlertDialog(
                    onDismissRequest = { showSmileyTip = false },
                    containerColor = BinanceDarkSurface,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("☺", fontSize = 28.sp, color = BinanceGold)
                            Text("Smart Portfolio Advisor", color = BinanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Welcome to your premium Binance AI Space! We detected your ACT I (Act I : The AI Prophecy) position. You are positioned at the cutting edge of AI agent utilities.",
                                color = BinanceTextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "💡 Tip of the Day: Your overall portfolio is highly secure. Track live blockchain transitions in your Send/Withdrawal portal to observe real-time transaction speeds.",
                                color = BinanceTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSmileyTip = false },
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Excellent, Thanks!", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Beautiful, high-fidelity fully floating rounded-diamond gradient smiley button matching the user's screenshot
            if (!isWithdrawalFullFlow && !isDepositFullFlow && (activeTab == "Home" || activeTab == "Assets")) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                        .offset(x = 20.dp) // shift right to cut off, as requested
                        .size(48.dp)
                        .clickable { showSmileyTip = true },
                    contentAlignment = Alignment.Center
                ) {
                    // Rotated Diamond Background with dark outline matching the user's screenshot
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .graphicsLayer { rotationZ = 45f }
                            .background(
                                color = Color(0xFFFCD535), // Solid golden-yellow background
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF1E2026), // Thin dark outline matching the screenshot
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    // Unrotated, perfectly aligned upright face overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Smiley eyes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left eye
                            Box(
                                modifier = Modifier
                                    .size(width = 2.5.dp, height = 7.dp)
                                    .background(Color(0xFF1E2026), shape = RoundedCornerShape(1.dp))
                            )
                            // Right eye
                            Box(
                                modifier = Modifier
                                    .size(width = 2.5.dp, height = 7.dp)
                                    .background(Color(0xFF1E2026), shape = RoundedCornerShape(1.dp))
                            )
                        }

                        // Smiling mouth
                        Canvas(
                            modifier = Modifier
                                .size(width = 10.dp, height = 5.dp)
                        ) {
                            drawArc(
                                color = Color(0xFF1E2026),
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarCustomIcon(name: String, itemColor: Color, isSelected: Boolean) {
    if (name == "Home") {
        Icon(
            painter = painterResource(id = R.drawable.ic_bottom_home),
            contentDescription = "Home",
            tint = itemColor,
            modifier = Modifier.size(20.dp)
        )
        return
    }

    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val scaleX = w / 24f
        val scaleY = h / 24f

        when (name) {
            "Assets" -> {
                // Folder Path
                val folderPath = Path().apply {
                    moveTo(4f * scaleX, 9f * scaleY)
                    lineTo(4f * scaleX, 8f * scaleY)
                    quadraticTo(4f * scaleX, 6f * scaleY, 6f * scaleX, 6f * scaleY)
                    lineTo(10f * scaleX, 6f * scaleY)
                    quadraticTo(11f * scaleX, 6f * scaleY, 11f * scaleX, 7f * scaleY)
                    lineTo(11f * scaleX, 9f * scaleY)
                    lineTo(18f * scaleX, 9f * scaleY)
                    quadraticTo(20f * scaleX, 9f * scaleY, 20f * scaleX, 11f * scaleY)
                    lineTo(20f * scaleX, 17f * scaleY)
                    quadraticTo(20f * scaleX, 19f * scaleY, 18f * scaleX, 19f * scaleY)
                    lineTo(6f * scaleX, 19f * scaleY)
                    quadraticTo(4f * scaleX, 19f * scaleY, 4f * scaleX, 17f * scaleY)
                    close()
                }
                drawPath(
                    path = folderPath,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )

                // Gold/Yellow Filled Diamond inside
                val diamondPath = Path().apply {
                    moveTo(15.5f * scaleX, 11.5f * scaleY)
                    lineTo(18.5f * scaleX, 14.5f * scaleY)
                    lineTo(15.5f * scaleX, 17.5f * scaleY)
                    lineTo(12.5f * scaleX, 14.5f * scaleY)
                    close()
                }
                // Always gold color matching the photo
                drawPath(
                    path = diamondPath,
                    color = Color(0xFFF3BA2F)
                )
            }
            "Deposit" -> {
                // Open Tray Path with rounded corner endpoints (no recess going down)
                val trayPath = Path().apply {
                    moveTo(9f * scaleX, 10f * scaleY)
                    lineTo(7f * scaleX, 10f * scaleY)
                    quadraticTo(4f * scaleX, 10f * scaleY, 4f * scaleX, 13f * scaleY)
                    lineTo(4f * scaleX, 16f * scaleY)
                    quadraticTo(4f * scaleX, 19f * scaleY, 7f * scaleX, 19f * scaleY)
                    lineTo(17f * scaleX, 19f * scaleY)
                    quadraticTo(20f * scaleX, 19f * scaleY, 20f * scaleX, 16f * scaleY)
                    lineTo(20f * scaleX, 13f * scaleY)
                    quadraticTo(20f * scaleX, 10f * scaleY, 17f * scaleX, 10f * scaleY)
                    lineTo(15f * scaleX, 10f * scaleY)
                }
                drawPath(
                    path = trayPath,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )

                // Downward Arrow
                drawLine(
                    color = itemColor,
                    start = Offset(12f * scaleX, 4f * scaleY),
                    end = Offset(12f * scaleX, 11f * scaleY),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                val arrowTipPath = Path().apply {
                    moveTo(9.5f * scaleX, 8.5f * scaleY)
                    lineTo(12f * scaleX, 11f * scaleY)
                    lineTo(14.5f * scaleX, 8.5f * scaleY)
                }
                drawPath(
                    path = arrowTipPath,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )

                // Diamond outline inside the tray (hollow outline)
                val innerDiamondPath = Path().apply {
                    moveTo(12f * scaleX, 13f * scaleY)
                    lineTo(14.5f * scaleX, 15.5f * scaleY)
                    lineTo(12f * scaleX, 18f * scaleY)
                    lineTo(9.5f * scaleX, 15.5f * scaleY)
                    close()
                }
                drawPath(
                    path = innerDiamondPath,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )
            }
            "Pay" -> {
                // Coin Circle
                drawCircle(
                    color = itemColor,
                    radius = 4.2f * scaleX,
                    center = Offset(15.5f * scaleX, 8.5f * scaleY),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Hollow Diamond inside the Coin
                val payDiamondPath = Path().apply {
                    moveTo(15.5f * scaleX, 6.2f * scaleY)
                    lineTo(17.8f * scaleX, 8.5f * scaleY)
                    lineTo(15.5f * scaleX, 10.8f * scaleY)
                    lineTo(13.2f * scaleX, 8.5f * scaleY)
                    close()
                }
                drawPath(
                    path = payDiamondPath,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )

                // Hand Path 1 (Outer palm and wrist with flat horizontal base)
                val handPath1 = Path().apply {
                    moveTo(9.5f * scaleX, 15f * scaleY)
                    lineTo(11f * scaleX, 18f * scaleY)
                    lineTo(14f * scaleX, 18f * scaleY)
                    quadraticTo(17f * scaleX, 18f * scaleY, 18f * scaleX, 14f * scaleY)
                    lineTo(14.5f * scaleX, 11f * scaleY)
                }
                drawPath(
                    path = handPath1,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )

                // Hand Path 2 (Inner palm crease)
                val handPath2 = Path().apply {
                    moveTo(9.5f * scaleX, 15f * scaleY)
                    lineTo(12f * scaleX, 12.5f * scaleY)
                    quadraticTo(13.5f * scaleX, 14.5f * scaleY, 14.5f * scaleX, 14.5f * scaleY)
                }
                drawPath(
                    path = handPath2,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )

                // Hand Path 3 (Thumb support under coin)
                val handPath3 = Path().apply {
                    moveTo(12f * scaleX, 12.5f * scaleY)
                    quadraticTo(13.5f * scaleX, 12.5f * scaleY, 14.5f * scaleX, 13.5f * scaleY)
                }
                drawPath(
                    path = handPath3,
                    color = itemColor,
                    style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                )
            }
            "Scan" -> {
                // Corners
                val tlBracket = Path().apply {
                    moveTo(9f * scaleX, 5f * scaleY)
                    lineTo(7f * scaleX, 5f * scaleY)
                    quadraticTo(5f * scaleX, 5f * scaleY, 5f * scaleX, 7f * scaleY)
                    lineTo(5f * scaleX, 9f * scaleY)
                }
                val trBracket = Path().apply {
                    moveTo(15f * scaleX, 5f * scaleY)
                    lineTo(17f * scaleX, 5f * scaleY)
                    quadraticTo(19f * scaleX, 5f * scaleY, 19f * scaleX, 7f * scaleY)
                    lineTo(19f * scaleX, 9f * scaleY)
                }
                val blBracket = Path().apply {
                    moveTo(5f * scaleX, 15f * scaleY)
                    lineTo(5f * scaleX, 17f * scaleY)
                    quadraticTo(5f * scaleX, 19f * scaleY, 7f * scaleX, 19f * scaleY)
                    lineTo(9f * scaleX, 19f * scaleY)
                }
                val brBracket = Path().apply {
                    moveTo(19f * scaleX, 15f * scaleY)
                    lineTo(19f * scaleX, 17f * scaleY)
                    quadraticTo(19f * scaleX, 19f * scaleY, 17f * scaleX, 19f * scaleY)
                    lineTo(15f * scaleX, 19f * scaleY)
                }

                listOf(tlBracket, trBracket, blBracket, brBracket).forEach { path ->
                    drawPath(
                        path = path,
                        color = itemColor,
                        style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round)
                    )
                }

                // Center rounded minus line (pill) - NO vertical caps!
                drawLine(
                    color = itemColor,
                    start = Offset(9f * scaleX, 12f * scaleY),
                    end = Offset(15f * scaleX, 12f * scaleY),
                    strokeWidth = 2.0.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
