package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {
    @GET("api/v3/ticker/24hr")
    suspend fun getTicker24hr(
        @Query("symbol") symbol: String
    ): BinanceTicker

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>
}

@JsonClass(generateAdapter = true)
data class BinanceTicker(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val lastPrice: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val openPrice: String
)

// Represents a parsed candlestick for UI consumption
data class Candle(
    val openTime: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float
)
