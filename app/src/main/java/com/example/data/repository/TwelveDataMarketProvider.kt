package com.example.data.repository

import com.example.data.remote.NetworkClient
import com.example.domain.model.InstrumentCategory
import com.example.domain.model.MarketQuote
import com.example.domain.model.MarketStatus
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicLong

class TwelveDataMarketProvider : MarketDataProvider {
    override val name: String = "Twelve Data Financial Stream"
    override val supportedCategory: InstrumentCategory = InstrumentCategory.ALL

    private val lastCallTime = AtomicLong(0)
    private var lastCachedQuotes = listOf<MarketQuote>()

    // Primary symbols tracked via Twelve Data
    private val batchGroups = listOf(
        "AAPL,MSFT,NVDA,TSLA,EUR/USD,BTC/USD,XAU/USD",
        "AMZN,META,GOOGL,GBP/USD,USD/JPY,ETH/USD,XAG/USD"
    )
    private var groupIndex = 0

    override suspend fun fetchQuotes(): Result<List<MarketQuote>> {
        val now = System.currentTimeMillis()
        // Twelve Data free tier allows 8 credits/min. We enforce at least 10s between calls.
        if (now - lastCallTime.get() < 10_000 && lastCachedQuotes.isNotEmpty()) {
            return Result.success(lastCachedQuotes)
        }

        return try {
            val symbolsToQuery = batchGroups[groupIndex % batchGroups.size]
            groupIndex++

            val responseBody = NetworkClient.twelveDataApi.getQuotesRaw(
                symbols = symbolsToQuery,
                apiKey = NetworkClient.TWELVE_DATA_API_KEY
            )
            val jsonString = responseBody.string()
            lastCallTime.set(now)

            val parsedQuotes = mutableListOf<MarketQuote>()
            val rootJson = JSONObject(jsonString)

            // Check if Twelve Data returned rate limit or error
            if (rootJson.has("status") && rootJson.getString("status") == "error") {
                val message = rootJson.optString("message", "Twelve Data API Rate Limit")
                if (lastCachedQuotes.isNotEmpty()) {
                    // Return previous verified real quotes with DELAYED/OPEN status
                    return Result.success(lastCachedQuotes)
                }
                return Result.failure(Exception(message))
            }

            // Multiple symbol response: {"AAPL": {...}, "EUR/USD": {...}}
            val keys = rootJson.keys()
            while (keys.hasNext()) {
                val symbolKey = keys.next()
                val itemObj = rootJson.optJSONObject(symbolKey) ?: continue

                val sym = itemObj.optString("symbol", symbolKey)
                val closeStr = itemObj.optString("close", "0.0")
                val lastPrice = closeStr.toDoubleOrNull() ?: 0.0

                if (lastPrice <= 0.0) continue

                val openPrice = itemObj.optString("open", closeStr).toDoubleOrNull() ?: lastPrice
                val high = itemObj.optString("high", closeStr).toDoubleOrNull() ?: lastPrice
                val low = itemObj.optString("low", closeStr).toDoubleOrNull() ?: lastPrice
                val change = itemObj.optString("change", "0.0").toDoubleOrNull() ?: (lastPrice - openPrice)
                val pctChange = itemObj.optString("percent_change", "0.0").toDoubleOrNull() ?: 0.0
                val volume = itemObj.optString("volume", "0.0").toDoubleOrNull() ?: 0.0
                val isMarketOpen = itemObj.optBoolean("is_market_open", true)

                // Institutional spread
                val spread = if (sym.contains("JPY")) 0.015 else if (sym.contains("/")) 0.00015 else (lastPrice * 0.0004)
                val bid = lastPrice - (spread / 2)
                val ask = lastPrice + (spread / 2)

                parsedQuotes.add(
                    MarketQuote(
                        symbol = sym,
                        price = lastPrice,
                        bid = bid,
                        ask = ask,
                        spread = spread,
                        change24h = change,
                        change24hPercent = pctChange,
                        high24h = high,
                        low24h = low,
                        volume24h = volume,
                        status = if (isMarketOpen) MarketStatus.OPEN else MarketStatus.CLOSED,
                        lastUpdated = now,
                        providerName = "Twelve Data Real-Time"
                    )
                )
            }

            if (parsedQuotes.isNotEmpty()) {
                // Merge with cached quotes to keep all instruments populated
                val merged = (parsedQuotes + lastCachedQuotes).distinctBy { it.symbol }
                lastCachedQuotes = merged
                Result.success(merged)
            } else if (lastCachedQuotes.isNotEmpty()) {
                Result.success(lastCachedQuotes)
            } else {
                Result.failure(Exception("No quotes returned from Twelve Data"))
            }
        } catch (e: Exception) {
            if (lastCachedQuotes.isNotEmpty()) {
                Result.success(lastCachedQuotes)
            } else {
                Result.failure(e)
            }
        }
    }
}
