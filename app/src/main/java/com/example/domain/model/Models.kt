package com.example.domain.model

enum class InstrumentCategory(val displayName: String) {
    ALL("All Markets"),
    FOREX("Forex"),
    CRYPTO("Crypto"),
    STOCKS("Stocks"),
    INDICES("Indices"),
    COMMODITIES("Commodities & Metals"),
    MARKET_SURGE("Market Surge")
}

enum class MarketStatus(val label: String) {
    OPEN("Open"),
    CLOSED("Market Closed"),
    PRE_MARKET("Pre-Market"),
    SUSPENDED("Suspended"),
    UNAVAILABLE("Data Unavailable")
}

data class Instrument(
    val symbol: String,
    val name: String,
    val category: InstrumentCategory,
    val baseCurrency: String,
    val quoteCurrency: String,
    val decimals: Int = 2,
    val minStake: Double = 500.0,
    val maxStake: Double = 500_000.0,
    val defaultDurationSeconds: Long = 300, // 5 minutes
    val description: String = ""
)

data class MarketQuote(
    val symbol: String,
    val price: Double,
    val bid: Double = price * 0.9998,
    val ask: Double = price * 1.0002,
    val spread: Double = price * 0.0004,
    val change24h: Double = 0.0,
    val change24hPercent: Double = 0.0,
    val high24h: Double = price * 1.01,
    val low24h: Double = price * 0.99,
    val volume24h: Double = 100_000.0,
    val status: MarketStatus = MarketStatus.OPEN,
    val lastUpdated: Long = System.currentTimeMillis(),
    val providerName: String = "Twelve Data Stream"
)

enum class ProductType(val title: String) {
    HIGHER_LOWER("Higher / Lower"),
    ABOVE_BELOW("Above / Below Strike"),
    TOUCH_NO_TOUCH("Touch / No Touch"),
    RANGE("Range Contract"),
    MARKET_SURGE("Market Surge Multiplier"),
    SPOT_BUY("Spot Buy"),
    SPOT_SELL("Spot Sell")
}

enum class ContractDirection(val title: String) {
    HIGHER("Higher ↑"),
    LOWER("Lower ↓"),
    ABOVE("Above Strike"),
    BELOW("Below Strike"),
    TOUCH("Touch Barrier"),
    NO_TOUCH("No Touch"),
    IN_RANGE("In Range"),
    OUT_RANGE("Breakout")
}

enum class OrderStatus {
    PENDING,
    OPEN,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    EXPIRED,
    SETTLED,
    REJECTED
}

data class Order(
    val id: String,
    val userId: String,
    val symbol: String,
    val productType: ProductType,
    val direction: ContractDirection,
    val stake: Double,
    val potentialPayout: Double,
    val entryPrice: Double,
    val executionPrice: Double,
    val strikePrice: Double = entryPrice,
    val settlementPrice: Double? = null,
    val fee: Double,
    val status: OrderStatus,
    val createdAt: Long,
    val expiresAt: Long,
    val settledAt: Long? = null,
    val pnl: Double = 0.0,
    val currency: String = "NGN",
    val idempotencyKey: String,
    val auditLog: String = ""
)

enum class SurgeRoundStatus {
    COUNTDOWN,
    ACTIVE,
    BURST,
    SETTLED
}

data class SurgeRound(
    val roundId: String,
    val symbol: String,
    val referencePrice: Double,
    val currentMultiplier: Double,
    val burstMultiplier: Double,
    val status: SurgeRoundStatus,
    val startTime: Long,
    val burstTime: Long? = null,
    val hashProof: String = ""
)

data class SurgeEntry(
    val entryId: String,
    val roundId: String,
    val userId: String,
    val stake: Double,
    val autoCashoutMultiplier: Double? = null,
    val cashedOutMultiplier: Double? = null,
    val payout: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE, CASHED_OUT, BUSTED
    val createdAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null
)

data class WalletBalance(
    val currency: String = "NGN",
    val availableBalance: Double = 50_000.0,
    val reservedBalance: Double = 0.0,
    val lockedBalance: Double = 0.0,
    val realizedPnl: Double = 0.0
) {
    val totalBalance: Double get() = availableBalance + reservedBalance + lockedBalance
}

data class LedgerRecord(
    val id: Long = 0,
    val transactionId: String,
    val accountType: String, // ASSET_USER_AVAILABLE, ASSET_USER_RESERVED, LIABILITY_PLATFORM_STAKE, REVENUE_FEES
    val debit: Double,
    val credit: Double,
    val currency: String,
    val description: String,
    val referenceId: String,
    val timestamp: Long
)

data class FinancialProductConfig(
    val id: String,
    val name: String,
    val symbol: String,
    val productType: ProductType,
    val durationSeconds: Long,
    val minStake: Double,
    val maxStake: Double,
    val feeRate: Double = 0.01,
    val payoutRate: Double = 1.85, // 85% return on win
    val settlementRule: String,
    val active: Boolean = true
)

enum class ServiceStatus {
    OPERATIONAL,
    DEGRADED,
    OUTAGE
}

data class SystemHealth(
    val componentName: String,
    val status: ServiceStatus,
    val latencyMs: Long,
    val lastChecked: Long,
    val details: String
)
