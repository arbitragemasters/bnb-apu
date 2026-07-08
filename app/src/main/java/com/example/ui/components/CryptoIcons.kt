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
    color: Color = Color.White
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
            drawPath(path, color)
        }
    }
}

@Composable
fun CryptoIcon(
    symbol: String,
    size: Dp = 36.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when (symbol) {
                    "BTC" -> Color(0xFFF7931A) // Real BTC orange
                    "ETH" -> Color(0xFF627EEA) // Real ETH purple-blue
                    "BNB" -> Color(0xFFF3BA2F) // Real BNB gold
                    "SOL" -> Color(0xFF66F9A1) // Real SOL mint green background
                    "DOGE" -> Color(0xFFC3A634) // Real DOGE bronze-gold
                    "ACT" -> Color(0xFF181A20) // Custom dark base to fit the avatar
                    "USDT" -> Color(0xFF26A17B) // Real USDT Tether green
                    "ADA" -> Color(0xFF0D1E30) // Real ADA navy blue
                    "UAH" -> Color.Transparent // Draws Ukrainian flag canvas
                    else -> Color(0xFF2B3139)
                }
            ),
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
                    modifier = Modifier.fillMaxSize(0.65f)
                )
            }
            "USDT" -> {
                SvgPathIcon(
                    pathData = "M17.922 17.383v-.002c-.11.008-.677.042-1.942.042-1.01 0-1.721-.03-1.971-.042v.003c-3.888-.171-6.79-.848-6.79-1.658 0-.809 2.902-1.486 6.79-1.66v2.644c.254.018.982.061 1.988.061 1.207 0 1.812-.05 1.925-.06v-2.643c3.88.173 6.775.85 6.775 1.658 0 .81-2.895 1.485-6.775 1.657m0-3.59v-2.366h5.414V7.819H8.595v3.608h5.414v2.365c-4.4.202-7.709 1.074-7.709 2.118 0 1.044 3.309 1.915 7.709 2.118v7.582h3.913v-7.584c4.393-.202 7.694-1.073 7.694-2.116 0-1.043-3.301-1.914-7.694-2.117",
                    modifier = Modifier.fillMaxSize(0.65f)
                )
            }
            "SOL" -> {
                SvgPathIcon(
                    pathData = "M9.925 19.687a.59.59 0 01.415-.17h14.366a.29.29 0 01.207.497l-2.838 2.815a.59.59 0 01-.415.171H7.294a.291.291 0 01-.207-.498l2.838-2.815zm0-10.517A.59.59 0 0110.34 9h14.366c.261 0 .392.314.207.498l-2.838 2.815a.59.59 0 01-.415.17H7.294a.291.291 0 01-.207-.497L9.925 9.17zm12.15 5.225a.59.59 0 00-.415-.17H7.294a.291.291 0 00-.207.498l2.838 2.815c.11.109.26.17.415.17h14.366a.291.291 0 00.207-.498l-2.838-2.815z",
                    modifier = Modifier.fillMaxSize(0.62f)
                )
            }
            "DOGE" -> {
                SvgPathIcon(
                    pathData = "M13.248 14.61h4.314v2.286h-4.314v4.818h2.721c1.077 0 1.958-.145 2.644-.437.686-.291 1.224-.694 1.615-1.21a4.4 4.4 0 00.796-1.815 11.4 11.4 0 00.21-2.252 11.4 11.4 0 00-.21-2.252 4.396 4.396 0 00-.796-1.815c-.391-.516-.93-.919-1.615-1.21-.686-.292-1.567-.437-2.644-.437h-2.721v4.325zm-2.766 2.286H9v-2.285h1.482V8h6.549c1.21 0 2.257.21 3.142.627.885.419 1.607.99 2.168 1.715.56.724.977 1.572 1.25 2.543.273.971.409 2.01.409 3.115a11.47 11.47 0 01-.41 3.115c-.272.97-.689 1.819-1.25 2.543-.56.725-1.282 1.296-2.167 1.715-.885.418-1.933.627-3.142.627h-6.549v-7.104z",
                    modifier = Modifier.fillMaxSize(0.62f)
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
                SvgPathIcon(
                    pathData = "M15.725 6.06c.479-.247 1.064.324.81.795-.149.384-.71.486-.996.193-.303-.28-.204-.836.186-.989zm-5.155.546c.291-.118.66.144.63.457.03.338-.39.588-.687.427-.393-.15-.348-.778.057-.884zm10.558.893c-.455-.054-.527-.758-.09-.9.34-.162.652.143.702.46-.072.27-.302.518-.612.44zm-9.385 1.265c.487-.303 1.181.148 1.106.705-.025.561-.783.887-1.211.507-.414-.298-.351-.982.105-1.212zm7.43.322c.217-.55 1.097-.568 1.344-.032.245.417-.056.934-.491 1.076-.577.106-1.124-.508-.853-1.044zm-4.069 1.013c-.005-.474.433-.826.89-.859.304.06.634.187.764.488.243.416.027.987-.41 1.178-.2.11-.438.069-.656.056-.333-.16-.614-.477-.588-.863zm-7.666.69c.445-.27 1.045.22.876.696-.092.411-.654.578-.975.316-.343-.246-.289-.837.1-1.013zm16.462-.002c.377-.288 1 .043.954.511.026.427-.513.75-.887.53-.412-.183-.455-.807-.067-1.04zm-6.64.851c.622-.22 1.362.043 1.716.59.468.667.22 1.683-.507 2.066-.752.453-1.851.07-2.13-.758-.315-.74.145-1.666.92-1.898zm-3.653.073c.69-.32 1.619-.052 1.952.642.392.676.089 1.617-.612 1.966-.702.393-1.693.095-2.032-.63-.381-.702-.043-1.655.692-1.978zM9.95 12.94c.053-.437.472-.722.895-.752a.98.98 0 01.87.857c-.03.45-.383.888-.867.886-.533.045-1-.477-.898-.991zm10.802-.656c.547-.313 1.306.142 1.282.76.037.655-.803 1.116-1.347.732-.566-.32-.522-1.22.065-1.492zm-8.63 2.307c.638-.173 1.37.123 1.683.701.343.582.203 1.39-.33 1.818-.685.626-1.946.374-2.31-.48-.419-.783.09-1.833.956-2.04zm6.927-.003c.621-.175 1.351.06 1.685.617.442.637.231 1.588-.426 1.998-.69.477-1.756.227-2.136-.519-.46-.771.003-1.861.877-2.096zm-11.04.726c.552-.205 1.164.394.94.933-.136.49-.839.672-1.202.31-.425-.34-.268-1.095.262-1.243zm14.969.782a.836.836 0 01.788-.874c.378.06.746.36.716.765.035.535-.62.898-1.084.647-.217-.109-.328-.328-.42-.538zM5.294 15.58c.332-.143.743.14.667.503-.018.411-.635.57-.861.226-.2-.239-.08-.606.194-.73zm20.949-.009c.234-.163.61-.046.702.223.157.294-.131.696-.467.647-.472.042-.624-.665-.235-.87zm-12.317 1.973c.874-.223 1.814.494 1.82 1.38.056.895-.87 1.688-1.764 1.482-.692-.11-1.235-.766-1.212-1.453-.002-.658.502-1.27 1.156-1.409zm3.462-.001c.887-.244 1.855.486 1.841 1.392.047.878-.85 1.645-1.726 1.47-.825-.104-1.433-.995-1.203-1.783.116-.524.562-.95 1.088-1.08zm-6.676.545c.614-.103 1.19.57.941 1.144-.182.612-1.086.777-1.486.278-.468-.48-.118-1.356.545-1.422zm10.154.027c.548-.226 1.22.24 1.178.825.022.643-.808 1.087-1.343.711-.607-.337-.496-1.33.165-1.536zm2.838 2.8c-.214-.393.175-.914.62-.841.22-.004.375.167.516.311.029.233.078.511-.119.69-.267.333-.872.238-1.017-.16zm-16.268-.732c.415-.271 1.012.134.918.61-.05.423-.59.664-.945.424-.382-.217-.368-.836.027-1.034zm8.193.883c.543-.235 1.235.23 1.183.818.04.65-.815 1.1-1.346.71-.59-.335-.491-1.321.163-1.528zm-3.794.871c.462-.239 1.082.174 1.04.684.014.418-.4.774-.82.712-.347-.007-.573-.314-.685-.605.006-.317.139-.67.465-.79zm7.686.008c.476-.29 1.152.126 1.107.67.012.57-.752.934-1.195.56-.428-.293-.376-.997.088-1.23zm1.337 3.25c-.212-.314.037-.693.38-.765.277.055.57.26.511.574-.04.427-.674.557-.891.192zm-10.611-.273c.084-.25.288-.497.587-.432.435.03.564.676.183.875-.342.227-.74-.084-.77-.443zm5.12.287c.083-.37.568-.549.888-.353.212.09.274.322.328.52a8.822 8.822 0 00-.08.31c-.131.152-.3.305-.518.3-.405.047-.771-.404-.619-.777z",
                    modifier = Modifier.fillMaxSize(0.62f)
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
