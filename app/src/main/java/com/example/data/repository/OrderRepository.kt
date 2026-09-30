package com.example.data.repository

import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.QuoteDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.OrderEntity
import com.example.domain.engine.SettlementEngine
import com.example.domain.model.ContractDirection
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.ProductType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OrderRepository(
    private val orderDao: OrderDao,
    private val quoteDao: QuoteDao,
    private val walletRepository: WalletRepository,
    private val auditDao: AuditDao
) {
    val settlementEngine = SettlementEngine(orderDao, quoteDao, walletRepository, auditDao)

    val allOrders: Flow<List<Order>> = orderDao.getAllOrders().map { list ->
        list.map { it.toDomain() }
    }

    val openOrders: Flow<List<Order>> = orderDao.getOpenOrders().map { list ->
        list.map { it.toDomain() }
    }

    val settledOrders: Flow<List<Order>> = orderDao.getSettledOrders().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun placeOrder(
        symbol: String,
        productType: ProductType,
        direction: ContractDirection,
        stake: Double,
        durationSeconds: Long,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Result<Order> {
        // 1. Idempotency Check
        val existing = orderDao.getOrderByFieldIdempotencyKey(idempotencyKey)
        if (existing != null) {
            return Result.success(existing.toDomain())
        }

        // 2. Risk & Stake Limits
        if (stake < 500.0) {
            return Result.failure(IllegalArgumentException("Minimum stake is ₦500"))
        }
        if (stake > 500_000.0) {
            return Result.failure(IllegalArgumentException("Maximum allowed stake per contract is ₦500,000"))
        }

        // 3. Market Quote Validation
        val quote = quoteDao.getQuote(symbol)
            ?: return Result.failure(IllegalStateException("Market data unavailable for $symbol"))

        val executionPrice = when (direction) {
            ContractDirection.HIGHER, ContractDirection.ABOVE -> quote.ask
            ContractDirection.LOWER, ContractDirection.BELOW -> quote.bid
            else -> quote.price
        }

        val feeRate = 0.01 // 1% execution fee
        val fee = stake * feeRate
        val payoutMultiplier = 1.85 // 85% net return on win
        val potentialPayout = stake * payoutMultiplier

        val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val now = System.currentTimeMillis()
        val expiresAt = now + (durationSeconds * 1000)

        // 4. Reserve Funds in Double-Entry Ledger
        val reserveResult = walletRepository.reserveFundsForOrder(stake, fee, orderId)
        if (reserveResult.isFailure) {
            return Result.failure(reserveResult.exceptionOrNull() ?: Exception("Failed to lock stake"))
        }

        val auditLog = "Order created at $now. Symbol=$symbol, EntryPrice=$executionPrice, Expiry=$expiresAt"

        val entity = OrderEntity(
            id = orderId,
            userId = "USER_DEFAULT",
            symbol = symbol,
            productType = productType.name,
            direction = direction.name,
            stake = stake,
            potentialPayout = potentialPayout,
            entryPrice = quote.price,
            executionPrice = executionPrice,
            strikePrice = quote.price,
            settlementPrice = null,
            fee = fee,
            status = OrderStatus.OPEN.name,
            createdAt = now,
            expiresAt = expiresAt,
            settledAt = null,
            pnl = 0.0,
            currency = "NGN",
            idempotencyKey = idempotencyKey,
            auditLog = auditLog
        )
        orderDao.insertOrder(entity)

        auditDao.insertLog(
            AuditLogEntity(
                action = "ORDER_PLACED",
                actor = "USER_DEFAULT",
                target = orderId,
                details = "Placed $direction on $symbol with stake ₦$stake. Payout: ₦$potentialPayout",
                timestamp = now
            )
        )

        return Result.success(entity.toDomain())
    }

    suspend fun settleOrder(orderId: String): Result<Order> {
        return settlementEngine.settleOrder(orderId)
    }

    private fun OrderEntity.toDomain(): Order {
        return Order(
            id = id,
            userId = userId,
            symbol = symbol,
            productType = ProductType.valueOf(productType),
            direction = ContractDirection.valueOf(direction),
            stake = stake,
            potentialPayout = potentialPayout,
            entryPrice = entryPrice,
            executionPrice = executionPrice,
            strikePrice = strikePrice,
            settlementPrice = settlementPrice,
            fee = fee,
            status = OrderStatus.valueOf(status),
            createdAt = createdAt,
            expiresAt = expiresAt,
            settledAt = settledAt,
            pnl = pnl,
            currency = currency,
            idempotencyKey = idempotencyKey,
            auditLog = auditLog
        )
    }
}
