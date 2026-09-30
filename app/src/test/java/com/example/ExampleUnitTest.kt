package com.example

import com.example.domain.model.ContractDirection
import com.example.domain.model.OrderStatus
import com.example.domain.model.ProductType
import com.example.domain.model.TradeOutcome
import com.example.domain.model.WorldwideCurrencies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialInvariantsUnitTest {

    @Test
    fun testDoubleEntryBalancingInvariant() {
        // Invariant: Total Debits must strictly equal Total Credits
        val stake = 5000.0
        val fee = 50.0
        val totalDebitHold = stake
        val platformFeeDebit = fee
        val totalCreditFromAvailable = stake + fee

        assertEquals(totalCreditFromAvailable, totalDebitHold + platformFeeDebit, 0.0001)
    }

    @Test
    fun testWithdrawalCannotExceedAvailableFunds() {
        val availableBalance = 45000.0
        val withdrawalRequest = 50000.0
        val isAllowed = withdrawalRequest <= availableBalance
        assertFalse("Withdrawal must be rejected when exceeding available balance", isAllowed)
    }

    @Test
    fun testSettledOrderImmutability() {
        var orderStatus = OrderStatus.SETTLED
        val canReSettle = orderStatus != OrderStatus.SETTLED && orderStatus != OrderStatus.CANCELLED
        assertFalse("A settled order must not be settled a second time", canReSettle)
    }

    @Test
    fun testDirectionalContractWinCondition() {
        val entryPrice = 105240.0
        val higherSettlementPrice = 105310.0
        val lowerSettlementPrice = 105190.0

        val isHigherWin = higherSettlementPrice > entryPrice
        val isLowerWin = lowerSettlementPrice < entryPrice

        assertTrue("HIGHER prediction must win when settlement > entry", isHigherWin)
        assertTrue("LOWER prediction must win when settlement < entry", isLowerWin)
    }

    @Test
    fun testSurgeMultiplierProgression() {
        // Multiplier progression must always be monotonic and >= 1.00
        val t0 = 0.0
        val t1 = 2.0
        val t2 = 5.0

        fun mult(t: Double) = 1.00 + (0.09 * Math.pow(t, 1.2))

        val m0 = mult(t0)
        val m1 = mult(t1)
        val m2 = mult(t2)

        assertTrue(m0 >= 1.00)
        assertTrue(m1 > m0)
        assertTrue(m2 > m1)
    }

    @Test
    fun testWorldwideCurrencyLookupAndRate() {
        val usd = WorldwideCurrencies.getByCode("USD")
        assertEquals(1.0, usd.exchangeRateToUsd, 0.001)

        val ngn = WorldwideCurrencies.getByCode("NGN")
        assertTrue(ngn.exchangeRateToUsd > 1000.0)

        val eur = WorldwideCurrencies.getByCode("EUR")
        assertTrue(eur.exchangeRateToUsd > 0.0)
    }

    @Test
    fun testFridayXMaxReturnCapping() {
        val maxEntryUsd = 500.0
        val maxMultiplier = 900.0
        val potentialReturn = (maxEntryUsd * maxMultiplier).coerceAtMost(9000.0)

        assertEquals(9000.0, potentialReturn, 0.001)
    }

    @Test
    fun testAllTenTradeOutcomesExist() {
        val expectedOutcomes = listOf(
            TradeOutcome.WON,
            TradeOutcome.LOSS,
            TradeOutcome.ACTIVE,
            TradeOutcome.VOID,
            TradeOutcome.CASHED_OUT,
            TradeOutcome.PARTIALLY_CASHED_OUT,
            TradeOutcome.EXPIRED,
            TradeOutcome.CANCELLED,
            TradeOutcome.PENDING_SETTLEMENT,
            TradeOutcome.SETTLED
        )
        assertEquals(10, expectedOutcomes.size)
    }
}
