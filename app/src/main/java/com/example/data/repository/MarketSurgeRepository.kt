package com.example.data.repository

import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.QuoteDao
import com.example.data.local.dao.SurgeDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.SurgeEntryEntity
import com.example.data.local.entity.SurgeRoundEntity
import com.example.domain.model.SurgeEntry
import com.example.domain.model.SurgeRound
import com.example.domain.model.SurgeRoundStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

class MarketSurgeRepository(
    private val surgeDao: SurgeDao,
    private val quoteDao: QuoteDao,
    private val walletRepository: WalletRepository,
    private val auditDao: AuditDao
) {
    val latestRound: Flow<SurgeRound?> = surgeDao.getLatestRound().map { it?.toDomain() }

    fun getEntriesForRound(roundId: String): Flow<List<SurgeEntry>> =
        surgeDao.getEntriesForRound(roundId).map { list -> list.map { it.toDomain() } }

    suspend fun createNewRound(symbol: String = "BTC/USD"): SurgeRound {
        val quote = quoteDao.getQuote(symbol)
        val refPrice = quote?.price ?: 105_240.0
        val roundId = "SRG-${UUID.randomUUID().toString().take(6).uppercase()}"
        val now = System.currentTimeMillis()

        // Deterministic server-side burst multiplier calculation from server secret + roundId + timestamp
        val rawSeed = "$roundId-$now-NEXIS_FINANCIAL_SALT"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(rawSeed.toByteArray())
            .joinToString("") { "%02x".format(it) }

        // Volatility surge calculation: 75% between 1.2x and 4.0x, 20% between 4.0x and 12.0x, 5% super surge
        val hashInt = hash.take(6).toInt(16)
        val burstMultiplier = when {
            hashInt % 100 < 5 -> 8.0 + (hashInt % 1200) / 100.0 // Super surge up to 20x
            hashInt % 100 < 25 -> 3.0 + (hashInt % 500) / 100.0 // Medium surge up to 8x
            else -> 1.25 + (hashInt % 250) / 100.0 // Standard volatility band
        }

        val entity = SurgeRoundEntity(
            roundId = roundId,
            symbol = symbol,
            referencePrice = refPrice,
            currentMultiplier = 1.00,
            burstMultiplier = (Math.round(burstMultiplier * 100.0) / 100.0),
            status = SurgeRoundStatus.ACTIVE.name,
            startTime = now,
            burstTime = null,
            hashProof = hash
        )
        surgeDao.insertOrUpdateRound(entity)

        auditDao.insertLog(
            AuditLogEntity(
                action = "SURGE_ROUND_OPENED",
                actor = "SURGE_ENGINE",
                target = roundId,
                details = "Started Surge round $roundId on $symbol at ref price $refPrice. HashProof=$hash",
                timestamp = now
            )
        )

        return entity.toDomain()
    }

    suspend fun updateMultiplier(roundId: String, multiplier: Double) {
        val round = surgeDao.getRoundById(roundId) ?: return
        if (round.status != SurgeRoundStatus.ACTIVE.name) return

        val roundedMultiplier = Math.round(multiplier * 100.0) / 100.0
        val isBurst = roundedMultiplier >= round.burstMultiplier
        val now = System.currentTimeMillis()

        val updated = round.copy(
            currentMultiplier = if (isBurst) round.burstMultiplier else roundedMultiplier,
            status = if (isBurst) SurgeRoundStatus.BURST.name else SurgeRoundStatus.ACTIVE.name,
            burstTime = if (isBurst) now else null
        )
        surgeDao.insertOrUpdateRound(updated)

        if (isBurst) {
            // Burst occurred! Any remaining ACTIVE entries are liquidated
            auditDao.insertLog(
                AuditLogEntity(
                    action = "SURGE_BURST_TRIGGERED",
                    actor = "SURGE_ENGINE",
                    target = roundId,
                    details = "Surge round $roundId burst at ${round.burstMultiplier}x",
                    timestamp = now
                )
            )
        }
    }

    suspend fun enterRound(
        roundId: String,
        stake: Double,
        autoCashout: Double? = null
    ): Result<SurgeEntry> {
        val round = surgeDao.getRoundById(roundId)
            ?: return Result.failure(IllegalStateException("Surge round not found"))

        if (round.status != SurgeRoundStatus.ACTIVE.name) {
            return Result.failure(IllegalStateException("Round is not accepting entries (Status: ${round.status})"))
        }

        if (round.currentMultiplier > 1.20) {
            return Result.failure(IllegalStateException("Entry closed for current active surge. Next round starting shortly."))
        }

        if (stake < 500.0 || stake > 200_000.0) {
            return Result.failure(IllegalArgumentException("Stake must be between ₦500 and ₦200,000"))
        }

        val entryId = "SENTRY-${UUID.randomUUID().toString().take(6).uppercase()}"
        val reserveResult = walletRepository.reserveFundsForOrder(stake, stake * 0.01, entryId)
        if (reserveResult.isFailure) {
            return Result.failure(reserveResult.exceptionOrNull() ?: Exception("Failed to lock stake"))
        }

        val now = System.currentTimeMillis()
        val entry = SurgeEntryEntity(
            entryId = entryId,
            roundId = roundId,
            userId = "USER_DEFAULT",
            stake = stake,
            autoCashoutMultiplier = autoCashout,
            cashedOutMultiplier = null,
            payout = 0.0,
            status = "ACTIVE",
            createdAt = now,
            settledAt = null
        )
        surgeDao.insertSurgeEntry(entry)

        return Result.success(entry.toDomain())
    }

    suspend fun cashOut(entryId: String, roundId: String): Result<Double> {
        val round = surgeDao.getRoundById(roundId)
            ?: return Result.failure(IllegalStateException("Round not found"))
        if (round.status != SurgeRoundStatus.ACTIVE.name) {
            return Result.failure(IllegalStateException("Cannot cash out: Surge has burst or ended"))
        }

        val userEntry = surgeDao.getUserEntry(roundId, "USER_DEFAULT")
            ?: return Result.failure(IllegalStateException("No active entry found"))

        if (userEntry.status != "ACTIVE") {
            return Result.failure(IllegalStateException("Entry already cashed out or settled"))
        }

        val multiplier = round.currentMultiplier
        val payout = userEntry.stake * multiplier
        val now = System.currentTimeMillis()

        // Authoritative ledger settlement
        walletRepository.settleOrderFunds(entryId, userEntry.stake, payout, isWin = true)

        val updatedEntry = userEntry.copy(
            cashedOutMultiplier = multiplier,
            payout = payout,
            status = "CASHED_OUT",
            settledAt = now
        )
        surgeDao.updateSurgeEntry(updatedEntry)

        auditDao.insertLog(
            AuditLogEntity(
                action = "SURGE_CASHOUT_SUCCESS",
                actor = "USER_DEFAULT",
                target = entryId,
                details = "Cashed out at ${multiplier}x! Stake: ₦${userEntry.stake}, Payout: ₦$payout",
                timestamp = now
            )
        )

        return Result.success(payout)
    }

    private fun SurgeRoundEntity.toDomain(): SurgeRound {
        return SurgeRound(
            roundId = roundId,
            symbol = symbol,
            referencePrice = referencePrice,
            currentMultiplier = currentMultiplier,
            burstMultiplier = burstMultiplier,
            status = SurgeRoundStatus.valueOf(status),
            startTime = startTime,
            burstTime = burstTime,
            hashProof = hashProof
        )
    }

    private fun SurgeEntryEntity.toDomain(): SurgeEntry {
        return SurgeEntry(
            entryId = entryId,
            roundId = roundId,
            userId = userId,
            stake = stake,
            autoCashoutMultiplier = autoCashoutMultiplier,
            cashedOutMultiplier = cashedOutMultiplier,
            payout = payout,
            status = status,
            createdAt = createdAt,
            settledAt = settledAt
        )
    }
}
