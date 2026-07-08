package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.core.graphics.PathParser

@Composable
fun SvgPathIcon(
    pathData: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    brush: Brush? = null
) {
    Canvas(modifier = modifier) {
        val path = try {
            PathParser.createPathFromPathData(pathData).asComposePath()
        } catch (e: Exception) {
            Path()
        }
        val scaleX = this.size.width / 32f
        val scaleY = this.size.height / 32f
        scale(scaleX, scaleY, pivot = Offset.Zero) {
            if (brush != null) {
                drawPath(path, brush)
            } else {
                drawPath(path, color)
            }
        }
    }
}

@Composable
fun CryptoIcon(
    symbol: String,
    size: Dp = 36.dp
) {
    // Premium brand-aligned gradients matching actual crypto visual identities
    val backgroundBrush = when (symbol) {
        "BTC" -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFAD33), Color(0xFFEC8205)) // Real BTC Orange Gradient
        )
        "ETH" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF8C8CFF), Color(0xFF3F5EFB)) // Premium Royal ETH Purple-Blue Gradient
        )
        "BNB" -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFCD535), Color(0xFFEF9E11)) // Real Binance Yellow Gradient
        )
        "USDT" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF3AC79E), Color(0xFF1B8465)) // Real Tether Emerald Gradient
        )
        else -> null
    }

    val backgroundColor = when (symbol) {
        "ACT" -> Color(0xFF181A20) // Slate-dark base matching its avatar frame
        "UAH" -> Color.Transparent
        "SOL", "DOGE", "ADA" -> Color.Transparent // Transparent so that downloaded circular high-fidelity logos render flawlessly
        else -> Color(0xFF2B3139)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .let { modifier ->
                if (backgroundBrush != null) {
                    modifier.background(backgroundBrush)
                } else {
                    modifier.background(backgroundColor)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (symbol) {
            "BTC" -> {
                SvgPathIcon(
                    pathData = "M23.189 14.02c.314-2.096-1.283-3.223-3.465-3.975l.708-2.84-1.728-.43-.69 2.765c-.454-.114-.92-.22-1.385-.326l.695-2.783L15.596 6l-.708 2.839c-.376-.086-.746-.17-1.104-.26l.002-.009-2.384-.595-.46 1.846s1.283.294 1.256.312c.7.175.826.638.805 1.006l-.806 3.235c.048.012.11.03.18.057l-.183-.045-1.13 4.532c-.086.212-.303.531-.793.41.018.025-1.256-.313-1.256-.313l-.858 1.978 2.25.561c.418.105.828.215 1.231.318l-.715 2.872 1.727.43.708-2.84c.472.127.93.245 1.378.357l-.706 2.828 1.728.43.715-2.866c2.948.558 5.164.333 6.097-2.333.752-2.146-.037-3.385-1.588-4.192 1.13-.26 1.98-1.003 2.207-2.538zm-3.95 5.538c-.533 2.147-4.148.986-5.32.695l.95-3.805c1.172.293 4.929.872 4.37 3.11zm.535-5.569c-.487 1.953-3.495.96-4.47.717l.86-3.45c.975.243 4.118.696 3.61 2.733z",
                    modifier = Modifier.fillMaxSize(0.62f)
                )
            }
            "ETH" -> {
                Canvas(modifier = Modifier.fillMaxSize(0.62f)) {
                    val scaleX = this.size.width / 32f
                    val scaleY = this.size.height / 32f
                    scale(scaleX, scaleY, pivot = Offset.Zero) {
                        try {
                            // Path 1
                            drawPath(
                                PathParser.createPathFromPathData("M16.498 4v8.87l7.497 3.35z").asComposePath(),
                                Color.White.copy(alpha = 0.602f)
                            )
                            // Path 2
                            drawPath(
                                PathParser.createPathFromPathData("M16.498 4L9 16.22l7.498-3.35z").asComposePath(),
                                Color.White
                            )
                            // Path 3
                            drawPath(
                                PathParser.createPathFromPathData("M16.498 21.968v6.027L24 17.616z").asComposePath(),
                                Color.White.copy(alpha = 0.602f)
                            )
                            // Path 4
                            drawPath(
                                PathParser.createPathFromPathData("M16.498 27.995v-6.028L9 17.616z").asComposePath(),
                                Color.White
                            )
                            // Path 5
                            drawPath(
                                PathParser.createPathFromPathData("M16.498 20.573l7.497-4.353-7.497-3.348z").asComposePath(),
                                Color.White.copy(alpha = 0.2f)
                            )
                            // Path 6
                            drawPath(
                                PathParser.createPathFromPathData("M9 16.22l7.498 4.353v-7.701z").asComposePath(),
                                Color.White.copy(alpha = 0.602f)
                            )
                        } catch (e: Exception) {
                            // fallback
                        }
                    }
                }
            }
            "BNB" -> {
                SvgPathIcon(
                    pathData = "M12.116 14.404L16 10.52l3.886 3.886 2.26-2.26L16 6l-6.144 6.144 2.26 2.26zM6 16l2.26-2.26L10.52 16l-2.26 2.26L6 16zm6.116 1.596L16 21.48l3.886-3.886 2.26 2.259L16 26l-6.144-6.144-.003-.003 2.263-2.257zM21.48 16l2.26-2.26L26 16l-2.26 2.26L21.48 16zm-3.188-.002h.002V16L16 18.294l-2.291-2.29-.004-.004.004-.003.401-.402.195-.195L16 13.706l2.293 2.293z",
                    modifier = Modifier.fillMaxSize(0.65f),
                    color = Color.White // Rich white lines over gorgeous gold gradient background
                )
            }
            "USDT" -> {
                SvgPathIcon(
                    pathData = "M17.922 17.383v-.002c-.11.008-.677.042-1.942.042-1.01 0-1.721-.03-1.971-.042v.003c-3.888-.171-6.79-.848-6.79-1.658 0-.809 2.902-1.486 6.79-1.66v2.644c.254.018.982.061 1.988.061 1.207 0 1.812-.05 1.925-.06v-2.643c3.88.173 6.775.85 6.775 1.658 0 .81-2.895 1.485-6.775 1.657m0-3.59v-2.366h5.414V7.819H8.595v3.608h5.414v2.365c-4.4.202-7.709 1.074-7.709 2.118 0 1.044 3.309 1.915 7.709 2.118v7.582h3.913v-7.584c4.393-.202 7.694-1.073 7.694-2.116 0-1.043-3.301-1.914-7.694-2.117",
                    modifier = Modifier.fillMaxSize(0.65f)
                )
            }
            "SOL" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_sol_logo),
                    contentDescription = "Solana Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            "DOGE" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_doge_logo),
                    contentDescription = "Dogecoin Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            "ACT" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_act_logo_1783363729963),
                    contentDescription = "ACT Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            "ADA" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_ada_logo),
                    contentDescription = "Cardano Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            "UAH" -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    // Top half blue
                    drawRect(
                        color = Color(0xFF0057B7),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, h / 2f)
                    )
                    // Bottom half yellow
                    drawRect(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(0f, h / 2f),
                        size = Size(w, h / 2f)
                    )
                }
                Text(
                    text = "₴",
                    color = Color.White,
                    fontSize = (size.value * 0.55).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {
                Text(
                    text = symbol.take(3),
                    color = Color.White,
                    fontSize = (size.value * 0.32).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
