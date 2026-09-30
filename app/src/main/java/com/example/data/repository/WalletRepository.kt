package com.example.data.repository

import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.LedgerDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.data.local.entity.WalletEntity
import com.example.domain.model.LedgerRecord
import com.example.domain.model.WalletBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WalletRepository(
    private val walletDao: WalletDao,
    private val ledgerDao: LedgerDao,
    private val auditDao: AuditDao
) {
    val walletBalance: Flow<WalletBalance> = walletDao.getWallet("NGN").map { entity ->
        if (entity != null) {
            WalletBalance(
                currency = entity.currency,
                availableBalance = entity.availableBalance,
                reservedBalance = entity.reservedBalance,
                lockedBalance = entity.lockedBalance,
                realizedPnl = entity.realizedPnl
            )
        } else {
            WalletBalance("NGN", 50_000.0, 0.0, 0.0, 0.0)
        }
    }

    val ledgerHistory: Flow<List<LedgerRecord>> = ledgerDao.getAllEntries().map { list ->
        list.map {
            LedgerRecord(
                id = it.id,
                transactionId = it.transactionId,
                accountType = it.accountType,
                debit = it.debit,
                credit = it.credit,
                currency = it.currency,
                description = it.description,
                referenceId = it.referenceId,
                timestamp = it.timestamp
            )
        }
    }

    suspend fun initializeWalletIfEmpty() {
        val existing = walletDao.getWalletDirect("NGN")
        if (existing == null) {
            // Initial seed with opening ledger balance entry
            val initialBalance = 100_000.0 // Initial approved starting capital
            val txId = "TX-INIT-${UUID.randomUUID().toString().take(8)}"
            val now = System.currentTimeMillis()

            val wallet = WalletEntity("NGN", initialBalance, 0.0, 0.0, 0.0)
            walletDao.insertOrUpdateWallet(wallet)

            // Double entry: Debit User Available Asset, Credit Capital/Equity Account
            val entries = listOf(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "ASSET:USER_AVAILABLE_NGN",
                    debit = initialBalance,
                    credit = 0.0,
                    currency = "NGN",
                    description = "Initial opening balance allocation",
                    referenceId = "ACCOUNT_OPEN",
                    timestamp = now
                ),
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "EQUITY:TREASURY_RESERVE_NGN",
                    debit = 0.0,
                    credit = initialBalance,
                    currency = "NGN",
                    description = "Initial opening balance funding offset",
                    referenceId = "ACCOUNT_OPEN",
                    timestamp = now
                )
            )
            ledgerDao.insertEntries(entries)
            auditDao.insertLog(
                AuditLogEntity(
                    action = "WALLET_INITIALIZED",
                    actor = "SYSTEM_TREASURY",
                    target = "USER_DEFAULT_WALLET",
                    details = "Credited NGN $initialBalance via balanced double-entry ledger transaction $txId",
                    timestamp = now
                )
            )
        }
    }

    suspend fun processDeposit(amount: Double, idempotencyKey: String, paymentRef: String): Result<WalletBalance> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Deposit amount must be positive"))
        val now = System.currentTimeMillis()

        val currentWallet = walletDao.getWalletDirect("NGN") ?: WalletEntity("NGN", 0.0, 0.0, 0.0, 0.0)
        val newAvailable = currentWallet.availableBalance + amount
        val updated = currentWallet.copy(availableBalance = newAvailable)
        walletDao.insertOrUpdateWallet(updated)

        val txId = "DEP-$idempotencyKey"
        // Double-entry record
        val ledgerEntries = listOf(
            LedgerEntryEntity(
                transactionId = txId,
                accountType = "ASSET:USER_AVAILABLE_NGN",
                debit = amount,
                credit = 0.0,
                currency = "NGN",
                description = "Settled verified customer deposit: $paymentRef",
                referenceId = idempotencyKey,
                timestamp = now
            ),
            LedgerEntryEntity(
                transactionId = txId,
                accountType = "LIABILITY:SETTLEMENT_GATEWAY_NGN",
                debit = 0.0,
                credit = amount,
                currency = "NGN",
                description = "Customer deposit settlement receivable",
                referenceId = idempotencyKey,
                timestamp = now
            )
        )
        ledgerDao.insertEntries(ledgerEntries)
        auditDao.insertLog(
            AuditLogEntity(
                action = "DEPOSIT_SETTLED",
                actor = "PAYMENT_PROCESSOR",
                target = "USER_WALLET",
                details = "Deposit of NGN $amount settled with ref $paymentRef, txId $txId",
                timestamp = now
            )
        )

        return Result.success(
            WalletBalance(
                currency = updated.currency,
                availableBalance = updated.availableBalance,
                reservedBalance = updated.reservedBalance,
                lockedBalance = updated.lockedBalance,
                realizedPnl = updated.realizedPnl
            )
        )
    }

    suspend fun reserveFundsForOrder(stake: Double, fee: Double, orderId: String): Result<Unit> {
        val totalDebit = stake + fee
        val current = walletDao.getWalletDirect("NGN") ?: return Result.failure(IllegalStateException("Wallet not found"))
        if (current.availableBalance < totalDebit) {
            return Result.failure(IllegalStateException("Insufficient available balance (Available: ₦${current.availableBalance})"))
        }

        val updated = current.copy(
            availableBalance = current.availableBalance - totalDebit,
            reservedBalance = current.reservedBalance + stake
        )
        walletDao.insertOrUpdateWallet(updated)

        val txId = "HOLD-$orderId"
        val now = System.currentTimeMillis()
        val entries = listOf(
            // Debit reserved asset, credit available asset
            LedgerEntryEntity(
                transactionId = txId,
                accountType = "ASSET:USER_RESERVED_STAKE_NGN",
                debit = stake,
                credit = 0.0,
                currency = "NGN",
                description = "Stake lock for contract order $orderId",
                referenceId = orderId,
                timestamp = now
            ),
            LedgerEntryEntity(
                transactionId = txId,
                accountType = "ASSET:USER_AVAILABLE_NGN",
                debit = 0.0,
                credit = totalDebit,
                currency = "NGN",
                description = "Release funds for order hold & platform fee",
                referenceId = orderId,
                timestamp = now
            ),
            LedgerEntryEntity(
                transactionId = txId,
                accountType = "REVENUE:PLATFORM_FEES_NGN",
                debit = fee,
                credit = 0.0,
                currency = "NGN",
                description = "Order placement fee execution",
                referenceId = orderId,
                timestamp = now
            )
        )
        ledgerDao.insertEntries(entries)
        return Result.success(Unit)
    }

    suspend fun settleOrderFunds(
        orderId: String,
        stake: Double,
        payout: Double,
        isWin: Boolean
    ): Result<Unit> {
        val current = walletDao.getWalletDirect("NGN") ?: return Result.failure(IllegalStateException("Wallet not found"))
        val now = System.currentTimeMillis()
        val txId = "SETTLE-$orderId"

        val newReserved = (current.reservedBalance - stake).coerceAtLeast(0.0)
        val pnl = if (isWin) (payout - stake) else -stake
        val newAvailable = if (isWin) current.availableBalance + payout else current.availableBalance
        val newPnl = current.realizedPnl + pnl

        val updated = current.copy(
            availableBalance = newAvailable,
            reservedBalance = newReserved,
            realizedPnl = newPnl
        )
        walletDao.insertOrUpdateWallet(updated)

        val ledgerEntries = mutableListOf<LedgerEntryEntity>()
        if (isWin) {
            // Balanced double-entry for win: Release hold and credit payout from market clearing pool
            ledgerEntries.add(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "ASSET:USER_AVAILABLE_NGN",
                    debit = payout,
                    credit = 0.0,
                    currency = "NGN",
                    description = "Contract payout settlement: WIN order $orderId",
                    referenceId = orderId,
                    timestamp = now
                )
            )
            ledgerEntries.add(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "ASSET:USER_RESERVED_STAKE_NGN",
                    debit = 0.0,
                    credit = stake,
                    currency = "NGN",
                    description = "Release reserved stake on winning settlement",
                    referenceId = orderId,
                    timestamp = now
                )
            )
            ledgerEntries.add(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "LIABILITY:CLEARING_HOUSE_RESERVE_NGN",
                    debit = 0.0,
                    credit = payout - stake,
                    currency = "NGN",
                    description = "Market profit deduction from clearing reserve",
                    referenceId = orderId,
                    timestamp = now
                )
            )
        } else {
            // Loss: Reserved stake forfeited to clearing house
            ledgerEntries.add(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "ASSET:USER_RESERVED_STAKE_NGN",
                    debit = 0.0,
                    credit = stake,
                    currency = "NGN",
                    description = "Forfeit reserved stake on contract expiration/loss",
                    referenceId = orderId,
                    timestamp = now
                )
            )
            ledgerEntries.add(
                LedgerEntryEntity(
                    transactionId = txId,
                    accountType = "EQUITY:CLEARING_HOUSE_RESERVE_NGN",
                    debit = stake,
                    credit = 0.0,
                    currency = "NGN",
                    description = "Liquidated stake credited to market liquidity reserve",
                    referenceId = orderId,
                    timestamp = now
                )
            )
        }
        ledgerDao.insertEntries(ledgerEntries)
        auditDao.insertLog(
            AuditLogEntity(
                action = "ORDER_SETTLED_LEDGER",
                actor = "SETTLEMENT_ENGINE",
                target = "ORDER_$orderId",
                details = "Settled order $orderId: Win=$isWin, Stake=$stake, Payout=$payout, PnL=$pnl",
                timestamp = now
            )
        )
        return Result.success(Unit)
    }

    suspend fun requestWithdrawal(amount: Double, bankAccount: String, bankName: String): Result<String> {
        val current = walletDao.getWalletDirect("NGN") ?: return Result.failure(IllegalStateException("Wallet not found"))
        if (amount <= 0) return Result.failure(IllegalArgumentException("Withdrawal amount must be greater than zero"))
        if (amount > current.availableBalance) {
            return Result.failure(IllegalStateException("Withdrawal exceeds available funds (Available: ₦${current.availableBalance})"))
        }

        // Velocity & Limits Check
        if (amount > 1_000_000.0) {
            return Result.failure(IllegalStateException("Single withdrawal exceeds tiered compliance ceiling of ₦1,000,000"))
        }

        val withdrawalId = "WD-${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        // Move funds from available to locked
        val updated = current.copy(
            availableBalance = current.availableBalance - amount,
            lockedBalance = current.lockedBalance + amount
        )
        walletDao.insertOrUpdateWallet(updated)

        val entries = listOf(
            LedgerEntryEntity(
                transactionId = withdrawalId,
                accountType = "LIABILITY:USER_WITHDRAWAL_ESCROW_NGN",
                debit = amount,
                credit = 0.0,
                currency = "NGN",
                description = "Withdrawal request escrow lock to $bankName ($bankAccount)",
                referenceId = withdrawalId,
                timestamp = now
            ),
            LedgerEntryEntity(
                transactionId = withdrawalId,
                accountType = "ASSET:USER_AVAILABLE_NGN",
                debit = 0.0,
                credit = amount,
                currency = "NGN",
                description = "Funds placed in withdrawal review stage",
                referenceId = withdrawalId,
                timestamp = now
            )
        )
        ledgerDao.insertEntries(entries)
        auditDao.insertLog(
            AuditLogEntity(
                action = "WITHDRAWAL_REQUESTED",
                actor = "USER_CLIENT",
                target = withdrawalId,
                details = "Requested withdrawal of ₦$amount to $bankName. Status: UNDER_REVIEW",
                timestamp = now
            )
        )

        return Result.success(withdrawalId)
    }
}
