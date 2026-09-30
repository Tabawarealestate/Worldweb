package com.example.domain.model

enum class TradeOutcome {
    WON,
    LOSS,
    ACTIVE,
    VOID,
    CASHED_OUT,
    PARTIALLY_CASHED_OUT,
    EXPIRED,
    CANCELLED,
    PENDING_SETTLEMENT,
    SETTLED
}

data class TradeSelection(
    val id: String,
    val symbol: String,
    val productType: ProductType,
    val direction: ContractDirection,
    val durationSeconds: Long,
    val referencePrice: Double,
    val stake: Double,
    val potentialPayout: Double,
    val cashoutEligible: Boolean = true,
    val currentCashoutValue: Double = stake * 0.95,
    val outcome: TradeOutcome = TradeOutcome.ACTIVE
)

data class TradeSlipCode(
    val code: String, // e.g. FX7K9Q2M
    val selections: List<TradeSelection>,
    val createdAt: Long,
    val expiresAt: Long,
    val creatorId: String,
    val isRevoked: Boolean = false
)

data class FridayXEvent(
    val eventId: String,
    val title: String = "FRIDAY X WEEKLY MOMENT",
    val status: String = "LIVE", // SCHEDULED, LIVE, COMPLETED
    val minEntryUsd: Double = 10.0,
    val maxEntryUsd: Double = 500.0,
    val maxPotentialMultiplier: Double = 900.0, // max outcome up to $9,000 on max entry
    val currentMultiplier: Double = 1.00,
    val hashProof: String = "d41d8cd98f00b204e9800998ecf8427e",
    val countdownSeconds: Long = 3600
)

data class VisualTradeRound(
    val roundId: String,
    val symbol: String,
    val durationSeconds: Int = 15, // Ultra short: 10s, 15s, 20s
    val entryPrice: Double,
    val currentPrice: Double,
    val remainingSeconds: Int,
    val status: String = "ACTIVE",
    val direction: ContractDirection = ContractDirection.HIGHER
)

data class ReferralProfile(
    val referralId: String,
    val referralCode: String,
    val referralLink: String,
    val invitedUsersCount: Int = 14,
    val activeReferralsCount: Int = 9,
    val pendingRewardsUsd: Double = 45.0,
    val completedRewardsUsd: Double = 120.0
)
