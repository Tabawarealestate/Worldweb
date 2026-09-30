package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class BinanceTickerResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "lastPrice") val lastPrice: String,
    @Json(name = "bidPrice") val bidPrice: String?,
    @Json(name = "askPrice") val askPrice: String?,
    @Json(name = "priceChange") val priceChange: String,
    @Json(name = "priceChangePercent") val priceChangePercent: String,
    @Json(name = "highPrice") val highPrice: String,
    @Json(name = "lowPrice") val lowPrice: String,
    @Json(name = "volume") val volume: String
)

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    @Json(name = "result") val result: String,
    @Json(name = "base_code") val baseCode: String,
    @Json(name = "rates") val rates: Map<String, Double> = emptyMap(),
    @Json(name = "time_last_update_unix") val timeLastUpdateUnix: Long = 0
)

@JsonClass(generateAdapter = true)
data class TwelveDataQuoteItem(
    @Json(name = "symbol") val symbol: String = "",
    @Json(name = "name") val name: String? = null,
    @Json(name = "exchange") val exchange: String? = null,
    @Json(name = "datetime") val datetime: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "open") val open: String? = null,
    @Json(name = "high") val high: String? = null,
    @Json(name = "low") val low: String? = null,
    @Json(name = "close") val close: String? = null,
    @Json(name = "previous_close") val previousClose: String? = null,
    @Json(name = "change") val change: String? = null,
    @Json(name = "percent_change") val percentChange: String? = null,
    @Json(name = "volume") val volume: String? = null,
    @Json(name = "is_market_open") val isMarketOpen: Boolean? = true
)

interface BinanceApiService {
    @GET("api/v3/ticker/24hr")
    suspend fun get24hTickers(
        @Query("symbols") symbolsJson: String
    ): List<BinanceTickerResponse>
}

interface OpenExchangeApiService {
    @GET("v6/latest/USD")
    suspend fun getUsdRates(): ExchangeRateResponse
}

interface TwelveDataApiService {
    @GET("quote")
    suspend fun getQuotesRaw(
        @Query("symbol") symbols: String,
        @Query("apikey") apiKey: String
    ): ResponseBody
}

object NetworkClient {
    const val TWELVE_DATA_API_KEY = "1205b6ecb0354aeaab07424aad620208"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    val binanceApi: BinanceApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BinanceApiService::class.java)
    }

    val openExchangeApi: OpenExchangeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenExchangeApiService::class.java)
    }

    val twelveDataApi: TwelveDataApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.twelvedata.com/")
            .client(okHttpClient)
            .build()
            .create(TwelveDataApiService::class.java)
    }
}
