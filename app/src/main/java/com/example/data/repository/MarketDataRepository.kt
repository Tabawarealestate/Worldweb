package com.example.data.repository

import com.example.data.local.dao.InstrumentDao
import com.example.data.local.dao.QuoteDao
import com.example.data.local.entity.InstrumentEntity
import com.example.data.local.entity.QuoteEntity
import com.example.domain.model.Instrument
import com.example.domain.model.InstrumentCategory
import com.example.domain.model.MarketQuote
import com.example.domain.model.MarketStatus
import com.example.domain.model.ServiceStatus
import com.example.domain.model.SystemHealth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarketDataRepository(
    private val instrumentDao: InstrumentDao,
    private val quoteDao: QuoteDao,
    private val twelveDataProvider: MarketDataProvider = TwelveDataMarketProvider(),
    private val cryptoProvider: MarketDataProvider = BinanceCryptoProvider(),
    private val forexProvider: MarketDataProvider = ForexExchangeProvider(),
    private val equityProvider: MarketDataProvider = GlobalEquityAndCommodityProvider()
) {
    val allQuotes: Flow<List<MarketQuote>> = quoteDao.getAllQuotes().map { entities ->
        entities.map { it.toDomain() }
    }

    val allInstruments: Flow<List<Instrument>> = instrumentDao.getAllInstruments().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getQuote(symbol: String): Flow<MarketQuote?> = quoteDao.getQuoteFlow(symbol).map {
        it?.toDomain()
    }

    suspend fun initializeCatalog() {
        val defaultInstruments = listOf(
            // FOREX
            InstrumentEntity("EUR/USD", "Euro / US Dollar", InstrumentCategory.FOREX.name, "EUR", "USD", 5, 500.0, 500_000.0, 300, "Major FX currency pair"),
            InstrumentEntity("GBP/USD", "British Pound / US Dollar", InstrumentCategory.FOREX.name, "GBP", "USD", 5, 500.0, 500_000.0, 300, "Cable FX pair"),
            InstrumentEntity("USD/JPY", "US Dollar / Japanese Yen", InstrumentCategory.FOREX.name, "USD", "JPY", 3, 500.0, 500_000.0, 300, "Major FX pair"),
            InstrumentEntity("USD/CHF", "US Dollar / Swiss Franc", InstrumentCategory.FOREX.name, "USD", "CHF", 5, 500.0, 500_000.0, 300, "Swissie pair"),
            InstrumentEntity("AUD/USD", "Australian Dollar / US Dollar", InstrumentCategory.FOREX.name, "AUD", "USD", 5, 500.0, 500_000.0, 300, "Aussie pair"),
            InstrumentEntity("USD/CAD", "US Dollar / Canadian Dollar", InstrumentCategory.FOREX.name, "USD", "CAD", 5, 500.0, 500_000.0, 300, "Loonie pair"),
            InstrumentEntity("NZD/USD", "New Zealand Dollar / US Dollar", InstrumentCategory.FOREX.name, "NZD", "USD", 5, 500.0, 500_000.0, 300, "Kiwi pair"),
            InstrumentEntity("EUR/GBP", "Euro / British Pound", InstrumentCategory.FOREX.name, "EUR", "GBP", 5, 500.0, 500_000.0, 300, "Euro sterling cross"),
            InstrumentEntity("EUR/JPY", "Euro / Japanese Yen", InstrumentCategory.FOREX.name, "EUR", "JPY", 3, 500.0, 500_000.0, 300, "Euro yen cross"),
            InstrumentEntity("GBP/JPY", "British Pound / Japanese Yen", InstrumentCategory.FOREX.name, "GBP", "JPY", 3, 500.0, 500_000.0, 300, "Geppy cross"),

            // CRYPTO
            InstrumentEntity("BTC/USD", "Bitcoin / US Dollar", InstrumentCategory.CRYPTO.name, "BTC", "USD", 2, 1000.0, 1_000_000.0, 180, "Global primary cryptocurrency"),
            InstrumentEntity("ETH/USD", "Ethereum / US Dollar", InstrumentCategory.CRYPTO.name, "ETH", "USD", 2, 1000.0, 1_000_000.0, 180, "Smart contract platform"),
            InstrumentEntity("SOL/USD", "Solana / US Dollar", InstrumentCategory.CRYPTO.name, "SOL", "USD", 2, 500.0, 500_000.0, 180, "High throughput blockchain"),
            InstrumentEntity("XRP/USD", "Ripple XRP / US Dollar", InstrumentCategory.CRYPTO.name, "XRP", "USD", 4, 500.0, 250_000.0, 180, "Cross-border payment asset"),
            InstrumentEntity("BNB/USD", "BNB Chain / US Dollar", InstrumentCategory.CRYPTO.name, "BNB", "USD", 2, 500.0, 500_000.0, 180, "Utility token"),

            // COMMODITIES & METALS
            InstrumentEntity("XAU/USD", "Gold Spot (Troy Ounce)", InstrumentCategory.COMMODITIES.name, "XAU", "USD", 2, 1000.0, 1_000_000.0, 300, "Precious metal benchmark"),
            InstrumentEntity("XAG/USD", "Silver Spot (Troy Ounce)", InstrumentCategory.COMMODITIES.name, "XAG", "USD", 3, 500.0, 500_000.0, 300, "Industrial & investment silver"),
            InstrumentEntity("WTI", "Crude Oil WTI Light Sweet", InstrumentCategory.COMMODITIES.name, "WTI", "USD", 2, 1000.0, 500_000.0, 300, "US benchmark crude"),
            InstrumentEntity("Brent", "Brent Crude North Sea", InstrumentCategory.COMMODITIES.name, "BRENT", "USD", 2, 1000.0, 500_000.0, 300, "Global benchmark oil"),

            // INDICES
            InstrumentEntity("NASDAQ 100", "US Tech 100", InstrumentCategory.INDICES.name, "NDX", "USD", 1, 1000.0, 1_000_000.0, 300, "Top 100 tech equities"),
            InstrumentEntity("S&P 500", "US 500 Large Cap", InstrumentCategory.INDICES.name, "SPX", "USD", 1, 1000.0, 1_000_000.0, 300, "Broad market US index"),
            InstrumentEntity("Dow Jones", "Wall Street 30", InstrumentCategory.INDICES.name, "DJI", "USD", 0, 1000.0, 1_000_000.0, 300, "Industrial average"),
            InstrumentEntity("DAX", "Germany 40", InstrumentCategory.INDICES.name, "DAX", "EUR", 1, 1000.0, 500_000.0, 300, "German benchmark index"),
            InstrumentEntity("FTSE 100", "UK 100", InstrumentCategory.INDICES.name, "UKX", "GBP", 1, 1000.0, 500_000.0, 300, "London blue chips"),
            InstrumentEntity("Nikkei", "Japan 225", InstrumentCategory.INDICES.name, "N225", "JPY", 0, 1000.0, 500_000.0, 300, "Tokyo headline index"),

            // STOCKS
            InstrumentEntity("AAPL", "Apple Inc.", InstrumentCategory.STOCKS.name, "AAPL", "USD", 2, 500.0, 500_000.0, 300, "Consumer electronics & services"),
            InstrumentEntity("MSFT", "Microsoft Corp.", InstrumentCategory.STOCKS.name, "MSFT", "USD", 2, 500.0, 500_000.0, 300, "Enterprise software & cloud"),
            InstrumentEntity("NVDA", "NVIDIA Corp.", InstrumentCategory.STOCKS.name, "NVDA", "USD", 2, 500.0, 500_000.0, 300, "Semiconductors & AI hardware"),
            InstrumentEntity("TSLA", "Tesla Inc.", InstrumentCategory.STOCKS.name, "TSLA", "USD", 2, 500.0, 500_000.0, 300, "EVs & clean energy"),
            InstrumentEntity("AMZN", "Amazon.com Inc.", InstrumentCategory.STOCKS.name, "AMZN", "USD", 2, 500.0, 500_000.0, 300, "E-commerce & cloud"),
            InstrumentEntity("META", "Meta Platforms", InstrumentCategory.STOCKS.name, "META", "USD", 2, 500.0, 500_000.0, 300, "Social technology"),
            InstrumentEntity("GOOGL", "Alphabet Inc.", InstrumentCategory.STOCKS.name, "GOOGL", "USD", 2, 500.0, 500_000.0, 300, "Search & technology conglomerate")
        )
        instrumentDao.insertInstruments(defaultInstruments)
    }

    suspend fun refreshQuotes(): List<SystemHealth> {
        val healthReports = mutableListOf<SystemHealth>()

        // 1. Twelve Data Real-Time Feed (Stocks, Gold, Forex, Crypto)
        val tdStart = System.currentTimeMillis()
        val tdResult = twelveDataProvider.fetchQuotes()
        val tdLatency = System.currentTimeMillis() - tdStart
        if (tdResult.isSuccess) {
            val quotes = tdResult.getOrThrow()
            quoteDao.insertQuotes(quotes.map { it.toEntity() })
            healthReports.add(
                SystemHealth("Twelve Data Real-Time Stream", ServiceStatus.OPERATIONAL, tdLatency, System.currentTimeMillis(), "Authorized Twelve Data Stream Active")
            )
        } else {
            healthReports.add(
                SystemHealth("Twelve Data Real-Time Stream", ServiceStatus.DEGRADED, tdLatency, System.currentTimeMillis(), "Rate limit/delayed: ${tdResult.exceptionOrNull()?.localizedMessage ?: "Cooldown"}")
            )
        }

        // 2. Crypto High-Frequency Feed (Binance)
        val cryptoStart = System.currentTimeMillis()
        val cryptoResult = cryptoProvider.fetchQuotes()
        val cryptoLatency = System.currentTimeMillis() - cryptoStart
        if (cryptoResult.isSuccess) {
            val quotes = cryptoResult.getOrThrow()
            quoteDao.insertQuotes(quotes.map { it.toEntity() })
            healthReports.add(
                SystemHealth("Crypto Market Stream", ServiceStatus.OPERATIONAL, cryptoLatency, System.currentTimeMillis(), "Connected to Binance Stream")
            )
        } else {
            healthReports.add(
                SystemHealth("Crypto Market Stream", ServiceStatus.DEGRADED, cryptoLatency, System.currentTimeMillis(), "Crypto stream: ${cryptoResult.exceptionOrNull()?.localizedMessage ?: "Timeout"}")
            )
        }

        // 3. Forex Feed (Open Exchange Rates)
        val forexStart = System.currentTimeMillis()
        val forexResult = forexProvider.fetchQuotes()
        val forexLatency = System.currentTimeMillis() - forexStart
        if (forexResult.isSuccess) {
            val quotes = forexResult.getOrThrow()
            quoteDao.insertQuotes(quotes.map { it.toEntity() })
            healthReports.add(
                SystemHealth("Forex Rates Feed", ServiceStatus.OPERATIONAL, forexLatency, System.currentTimeMillis(), "Connected to Open FX Rates")
            )
        } else {
            healthReports.add(
                SystemHealth("Forex Rates Feed", ServiceStatus.DEGRADED, forexLatency, System.currentTimeMillis(), "FX stream: ${forexResult.exceptionOrNull()?.localizedMessage ?: "Timeout"}")
            )
        }

        // 4. Equities & Commodities Fallback/Baseline Feed
        val eqStart = System.currentTimeMillis()
        val eqResult = equityProvider.fetchQuotes()
        val eqLatency = System.currentTimeMillis() - eqStart
        if (eqResult.isSuccess) {
            val quotes = eqResult.getOrThrow()
            quoteDao.insertQuotes(quotes.map { it.toEntity() })
            healthReports.add(
                SystemHealth("Equities & Commodities Feed", ServiceStatus.OPERATIONAL, eqLatency, System.currentTimeMillis(), "Authorized Institutional Index Feed")
            )
        }

        return healthReports
    }

    private fun QuoteEntity.toDomain(): MarketQuote {
        return MarketQuote(
            symbol = symbol,
            price = price,
            bid = bid,
            ask = ask,
            spread = spread,
            change24h = change24h,
            change24hPercent = change24hPercent,
            high24h = high24h,
            low24h = low24h,
            volume24h = volume24h,
            status = MarketStatus.valueOf(status),
            lastUpdated = lastUpdated,
            providerName = providerName
        )
    }

    private fun MarketQuote.toEntity(): QuoteEntity {
        return QuoteEntity(
            symbol = symbol,
            price = price,
            bid = bid,
            ask = ask,
            spread = spread,
            change24h = change24h,
            change24hPercent = change24hPercent,
            high24h = high24h,
            low24h = low24h,
            volume24h = volume24h,
            status = status.name,
            lastUpdated = lastUpdated,
            providerName = providerName
        )
    }

    private fun InstrumentEntity.toDomain(): Instrument {
        return Instrument(
            symbol = symbol,
            name = name,
            category = InstrumentCategory.valueOf(category),
            baseCurrency = baseCurrency,
            quoteCurrency = quoteCurrency,
            decimals = decimals,
            minStake = minStake,
            maxStake = maxStake,
            defaultDurationSeconds = defaultDurationSeconds,
            description = description
        )
    }
}
