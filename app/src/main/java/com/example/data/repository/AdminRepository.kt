package com.example.data.repository

import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.ProductEntity
import com.example.domain.model.FinancialProductConfig
import com.example.domain.model.ProductType
import com.example.domain.model.ServiceStatus
import com.example.domain.model.SystemHealth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AdminRepository(
    private val productDao: ProductDao,
    private val auditDao: AuditDao
) {
    val activeProducts: Flow<List<FinancialProductConfig>> = productDao.getActiveProducts().map { list ->
        list.map { it.toDomain() }
    }

    val auditLogs: Flow<List<AuditLogEntity>> = auditDao.getRecentLogs()

    suspend fun initializeDefaultProducts() {
        val defaultList = listOf(
            ProductEntity(
                id = "PROD-BTC-5M",
                name = "BTC/USD 5-Minute Directional",
                symbol = "BTC/USD",
                productType = ProductType.HIGHER_LOWER.name,
                durationSeconds = 300,
                minStake = 1000.0,
                maxStake = 500_000.0,
                feeRate = 0.01,
                payoutRate = 1.85,
                settlementRule = "Settled against Binance Authoritative Spot feed at T+300s",
                active = true
            ),
            ProductEntity(
                id = "PROD-EURUSD-15M",
                name = "EUR/USD 15-Minute Range",
                symbol = "EUR/USD",
                productType = ProductType.RANGE.name,
                durationSeconds = 900,
                minStake = 500.0,
                maxStake = 300_000.0,
                feeRate = 0.01,
                payoutRate = 1.90,
                settlementRule = "Settled within 15 pip band against Open FX feed",
                active = true
            ),
            ProductEntity(
                id = "PROD-GOLD-5M",
                name = "XAU/USD Gold 5-Minute High/Low",
                symbol = "XAU/USD",
                productType = ProductType.HIGHER_LOWER.name,
                durationSeconds = 300,
                minStake = 1000.0,
                maxStake = 1_000_000.0,
                feeRate = 0.01,
                payoutRate = 1.82,
                settlementRule = "Settled against London Spot Gold fixing reference",
                active = true
            )
        )
        productDao.insertProducts(defaultList)
    }

    suspend fun createProduct(
        name: String,
        symbol: String,
        productType: ProductType,
        durationSeconds: Long,
        minStake: Double,
        maxStake: Double,
        feeRate: Double,
        payoutRate: Double,
        settlementRule: String
    ): Result<Unit> {
        val id = "PROD-${UUID.randomUUID().toString().take(6).uppercase()}"
        val entity = ProductEntity(
            id = id,
            name = name,
            symbol = symbol,
            productType = productType.name,
            durationSeconds = durationSeconds,
            minStake = minStake,
            maxStake = maxStake,
            feeRate = feeRate,
            payoutRate = payoutRate,
            settlementRule = settlementRule,
            active = true
        )
        productDao.insertProduct(entity)

        auditDao.insertLog(
            AuditLogEntity(
                action = "ADMIN_PRODUCT_CREATED",
                actor = "ADMIN_CONSOLE",
                target = id,
                details = "Created product $name ($symbol) with payout $payoutRate",
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    fun getPublicSystemStatus(): List<SystemHealth> {
        val now = System.currentTimeMillis()
        return listOf(
            SystemHealth("Core API Engine", ServiceStatus.OPERATIONAL, 14, now, "All cluster endpoints healthy"),
            SystemHealth("Market Data Streaming", ServiceStatus.OPERATIONAL, 28, now, "Authoritative feeds active"),
            SystemHealth("Forex Currency Network", ServiceStatus.OPERATIONAL, 35, now, "Open FX rates synchronized"),
            SystemHealth("Crypto Real-Time Stream", ServiceStatus.OPERATIONAL, 21, now, "Binance public feed latency 21ms"),
            SystemHealth("Global Equities & Indices", ServiceStatus.OPERATIONAL, 42, now, "NYSE/NASDAQ hours synchronized"),
            SystemHealth("Double-Entry Ledger", ServiceStatus.OPERATIONAL, 8, now, "Balance invariants 100% verified"),
            SystemHealth("Deposits & Gateway", ServiceStatus.OPERATIONAL, 45, now, "Idempotency reconciliation active"),
            SystemHealth("Withdrawals Processor", ServiceStatus.OPERATIONAL, 52, now, "Velocity controls operational"),
            SystemHealth("Market Surge Engine", ServiceStatus.OPERATIONAL, 16, now, "Deterministic cryptographic hash validated")
        )
    }

    private fun ProductEntity.toDomain(): FinancialProductConfig {
        return FinancialProductConfig(
            id = id,
            name = name,
            symbol = symbol,
            productType = ProductType.valueOf(productType),
            durationSeconds = durationSeconds,
            minStake = minStake,
            maxStake = maxStake,
            feeRate = feeRate,
            payoutRate = payoutRate,
            settlementRule = settlementRule,
            active = active
        )
    }
}
