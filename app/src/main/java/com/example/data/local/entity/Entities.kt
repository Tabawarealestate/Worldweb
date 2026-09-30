package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "instruments")
data class InstrumentEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val category: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val decimals: Int,
    val minStake: Double,
    val maxStake: Double,
    val defaultDurationSeconds: Long,
    val description: String
)

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val symbol: String,
    val price: Double,
    val bid: Double,
    val ask: Double,
    val spread: Double,
    val change24h: Double,
    val change24hPercent: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val status: String,
    val lastUpdated: Long,
    val providerName: String
)

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val symbol: String,
    val productType: String,
    val direction: String,
    val stake: Double,
    val potentialPayout: Double,
    val entryPrice: Double,
    val executionPrice: Double,
    val strikePrice: Double,
    val settlementPrice: Double?,
    val fee: Double,
    val status: String,
    val createdAt: Long,
    val expiresAt: Long,
    val settledAt: Long?,
    val pnl: Double,
    val currency: String,
    val idempotencyKey: String,
    val auditLog: String
)

@Entity(tableName = "surge_rounds")
data class SurgeRoundEntity(
    @PrimaryKey val roundId: String,
    val symbol: String,
    val referencePrice: Double,
    val currentMultiplier: Double,
    val burstMultiplier: Double,
    val status: String,
    val startTime: Long,
    val burstTime: Long?,
    val hashProof: String
)

@Entity(
    tableName = "surge_entries",
    indices = [
        Index(value = ["roundId"]),
        Index(value = ["userId"])
    ]
)
data class SurgeEntryEntity(
    @PrimaryKey val entryId: String,
    val roundId: String,
    val userId: String,
    val stake: Double,
    val autoCashoutMultiplier: Double?,
    val cashedOutMultiplier: Double?,
    val payout: Double,
    val status: String,
    val createdAt: Long,
    val settledAt: Long?
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val currency: String,
    val availableBalance: Double,
    val reservedBalance: Double,
    val lockedBalance: Double,
    val realizedPnl: Double
)

@Entity(
    tableName = "ledger_entries",
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["referenceId"])
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String,
    val accountType: String,
    val debit: Double,
    val credit: Double,
    val currency: String,
    val description: String,
    val referenceId: String,
    val timestamp: Long
)

@Entity(tableName = "financial_products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val productType: String,
    val durationSeconds: Long,
    val minStake: Double,
    val maxStake: Double,
    val feeRate: Double,
    val payoutRate: Double,
    val settlementRule: String,
    val active: Boolean
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val actor: String,
    val target: String,
    val details: String,
    val timestamp: Long
)
