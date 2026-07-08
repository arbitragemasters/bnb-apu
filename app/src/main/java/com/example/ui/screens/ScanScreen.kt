package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ScanScreen(
    onAddressScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(false) }

    // Loop vertical laser animation
    val infiniteTransition = rememberInfiniteTransition()
    val laserYPercent by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Deep black for scanner feel
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceDarkSurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "Scan QR Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
            }

            IconButton(onClick = { isFlashOn = !isFlashOn }) {
                Icon(
                    imageVector = Icons.Filled.FlashOn,
                    contentDescription = "Toggle Flash",
                    tint = if (isFlashOn) BinanceGold else BinanceTextSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Camera viewfinder overlay simulation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Dim background
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    size = size
                )

                // Define viewfinder square parameters
                val boxSize = minOf(w, h) * 0.65f
                val left = (w - boxSize) / 2
                val top = (h - boxSize) / 2

                // Clear out the viewfinder square
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                )

                // Draw bright neon gold corners for the camera focus frame
                val cornerLen = 30.dp.toPx()
                val thickness = 4.dp.toPx()
                val gold = Color(0xFFF0B90B)

                // Top-Left corner
                drawLine(gold, Offset(left, top), Offset(left + cornerLen, top), thickness)
                drawLine(gold, Offset(left, top), Offset(left, top + cornerLen), thickness)

                // Top-Right corner
                drawLine(gold, Offset(left + boxSize, top), Offset(left + boxSize - cornerLen, top), thickness)
                drawLine(gold, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLen), thickness)

                // Bottom-Left corner
                drawLine(gold, Offset(left, top + boxSize), Offset(left + cornerLen, top + boxSize), thickness)
                drawLine(gold, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLen), thickness)

                // Bottom-Right corner
                drawLine(gold, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLen, top + boxSize), thickness)
                drawLine(gold, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLen), thickness)

                // Draw horizontal moving laser line
                val laserY = top + (boxSize * laserYPercent)
                drawLine(
                    color = Color(0xFF0ECB81), // Green laser line
                    start = Offset(left + 10.dp.toPx(), laserY),
                    end = Offset(left + boxSize - 10.dp.toPx(), laserY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Central content layout
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Align QR code inside the frame to scan",
                    color = BinanceTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Simulation Trigger Button (auto-scans address and opens withdrawal)
                Button(
                    onClick = {
                        val mockBtcAddr = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
                        onAddressScanned(mockBtcAddr)
                        Toast.makeText(context, "QR Scan Success! Wallet address scanned.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.QrCode, "QR")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto Scan Wallet QR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
