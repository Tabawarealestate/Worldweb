package com.example.domain.engine

import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.QuoteDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.repository.WalletRepository
import com.example.domain.model.ContractDirection
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.ProductType
import java.util.UUID

class SettlementEngine(
    private val orderDao: OrderDao,
    private val quoteDao: QuoteDao,
    private val walletRepository: WalletRepository,
    private val auditDao: AuditDao
) {
    suspend fun evaluateAndSettlePendingOrders() {
        val now = System.currentTimeMillis()
        val openOrders = orderDao.getOrderById("") // placeholder or fetch open orders
    }

    suspend fun settleOrder(orderId: String): Result<Order> {
        val entity = orderDao.getOrderById(orderId) ?: return Result.failure(IllegalArgumentException("Order not found"))
        if (entity.status == OrderStatus.SETTLED.name || entity.status == OrderStatus.CANCELLED.name) {
            return Result.failure(IllegalStateException("Order $orderId is already in final state: ${entity.status}"))
        }

        val quote = quoteDao.getQuote(entity.symbol)
        val settlementPrice = quote?.price ?: entity.executionPrice

        val isWin = when (ContractDirection.valueOf(entity.direction)) {
            ContractDirection.HIGHER -> settlementPrice > entity.entryPrice
            ContractDirection.LOWER -> settlementPrice < entity.entryPrice
            ContractDirection.ABOVE -> settlementPrice >= entity.strikePrice
            ContractDirection.BELOW -> settlementPrice <= entity.strikePrice
            ContractDirection.TOUCH -> Math.abs(settlementPrice - entity.entryPrice) >= (entity.entryPrice * 0.001)
            ContractDirection.NO_TOUCH -> Math.abs(settlementPrice - entity.entryPrice) < (entity.entryPrice * 0.001)
            ContractDirection.IN_RANGE -> Math.abs(settlementPrice - entity.entryPrice) <= (entity.entryPrice * 0.005)
            ContractDirection.OUT_RANGE -> Math.abs(settlementPrice - entity.entryPrice) > (entity.entryPrice * 0.005)
        }

        val now = System.currentTimeMillis()
        val payout = if (isWin) entity.potentialPayout else 0.0
        val pnl = if (isWin) (entity.potentialPayout - entity.stake) else -entity.stake

        // Authoritative ledger settlement
        walletRepository.settleOrderFunds(entity.id, entity.stake, payout, isWin)

        val updatedEntity = entity.copy(
            status = OrderStatus.SETTLED.name,
            settlementPrice = settlementPrice,
            settledAt = now,
            pnl = pnl,
            auditLog = entity.auditLog + " | Settled at $now with price $settlementPrice, Win=$isWin, PnL=$pnl"
        )
        orderDao.updateOrder(updatedEntity)

        auditDao.insertLog(
            AuditLogEntity(
                action = "SETTLEMENT_COMPLETE",
                actor = "ENGINE_AUTHORITATIVE",
                target = entity.id,
                details = "Order ${entity.id} settled. Result=${if (isWin) "WIN" else "LOSS"}, Strike=${entity.entryPrice}, FinalPrice=$settlementPrice, PnL=$pnl",
                timestamp = now
            )
        )

        return Result.success(
            Order(
                id = updatedEntity.id,
                userId = updatedEntity.userId,
                symbol = updatedEntity.symbol,
                productType = ProductType.valueOf(updatedEntity.productType),
                direction = ContractDirection.valueOf(updatedEntity.direction),
                stake = updatedEntity.stake,
                potentialPayout = updatedEntity.potentialPayout,
                entryPrice = updatedEntity.entryPrice,
                executionPrice = updatedEntity.executionPrice,
                strikePrice = updatedEntity.strikePrice,
                settlementPrice = updatedEntity.settlementPrice,
                fee = updatedEntity.fee,
                status = OrderStatus.SETTLED,
                createdAt = updatedEntity.createdAt,
                expiresAt = updatedEntity.expiresAt,
                settledAt = updatedEntity.settledAt,
                pnl = updatedEntity.pnl,
                currency = updatedEntity.currency,
                idempotencyKey = updatedEntity.idempotencyKey,
                auditLog = updatedEntity.auditLog
            )
        )
    }
}
