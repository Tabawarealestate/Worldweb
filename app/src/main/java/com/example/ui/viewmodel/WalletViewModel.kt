package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.WalletRepository
import com.example.domain.model.LedgerRecord
import com.example.domain.model.PaymentAgent
import com.example.domain.model.WalletBalance
import com.example.domain.model.WorldwideCurrencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class WalletUiState(
    val isDepositOpen: Boolean = false,
    val isWithdrawOpen: Boolean = false,
    val depositAmount: String = "25000",
    val withdrawAmount: String = "10000",
    val selectedBank: String = "Access Bank PLC",
    val bankAccountNumber: String = "0123456789",
    val paymentMethod: String = "NIBSS Instant Transfer",
    val depositRail: String = "BANK", // "BANK", "AGENTS", "CRYPTO"
    val selectedCurrencyCode: String = "NGN",
    val selectedCryptoSymbol: String = "USDT",
    val selectedCryptoNetwork: String = "TRC-20",
    val selectedAgent: PaymentAgent? = null,
    val isProcessing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class WalletViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    val walletBalance: StateFlow<WalletBalance> = walletRepository.walletBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletBalance())

    val ledgerRecords: StateFlow<List<LedgerRecord>> = walletRepository.ledgerHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            walletRepository.initializeWalletIfEmpty()
        }
    }

    fun setCurrency(code: String) {
        _uiState.value = _uiState.value.copy(selectedCurrencyCode = code)
    }

    fun setDepositRail(rail: String) {
        _uiState.value = _uiState.value.copy(depositRail = rail)
    }

    fun setSelectedCrypto(symbol: String, network: String) {
        _uiState.value = _uiState.value.copy(
            selectedCryptoSymbol = symbol,
            selectedCryptoNetwork = network
        )
    }

    fun selectAgent(agent: PaymentAgent?) {
        _uiState.value = _uiState.value.copy(selectedAgent = agent)
    }

    fun openDepositDialog() {
        _uiState.value = _uiState.value.copy(isDepositOpen = true, successMessage = null, errorMessage = null)
    }

    fun closeDepositDialog() {
        _uiState.value = _uiState.value.copy(isDepositOpen = false)
    }

    fun openWithdrawDialog() {
        _uiState.value = _uiState.value.copy(isWithdrawOpen = true, successMessage = null, errorMessage = null)
    }

    fun closeWithdrawDialog() {
        _uiState.value = _uiState.value.copy(isWithdrawOpen = false)
    }

    fun setDepositAmount(amount: String) {
        _uiState.value = _uiState.value.copy(depositAmount = amount)
    }

    fun setWithdrawAmount(amount: String) {
        _uiState.value = _uiState.value.copy(withdrawAmount = amount)
    }

    fun setBankAccountNumber(acc: String) {
        _uiState.value = _uiState.value.copy(bankAccountNumber = acc)
    }

    fun setSelectedBank(bank: String) {
        _uiState.value = _uiState.value.copy(selectedBank = bank)
    }

    fun processDeposit() {
        val amount = _uiState.value.depositAmount.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a valid deposit amount")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            val idempotency = UUID.randomUUID().toString()
            val rail = _uiState.value.depositRail
            val curr = _uiState.value.selectedCurrencyCode
            val currInfo = WorldwideCurrencies.getByCode(curr)

            // Convert to NGN equivalent for internal ledger
            val amountInNgn = if (curr == "NGN") {
                amount
            } else {
                val usd = amount / currInfo.exchangeRateToUsd
                usd * 1620.0
            }

            val payRef = when (rail) {
                "CRYPTO" -> "CRYPTO-${_uiState.value.selectedCryptoSymbol}-${System.currentTimeMillis() % 100000}"
                "AGENTS" -> "AGENT-${_uiState.value.selectedAgent?.agentId ?: "P2P"}-${System.currentTimeMillis() % 100000}"
                else -> "BANK-$curr-${System.currentTimeMillis() % 100000}"
            }

            val result = walletRepository.processDeposit(amountInNgn, idempotency, payRef)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isDepositOpen = false,
                    successMessage = "Deposit of ${currInfo.symbol}${"%,.2f".format(amount)} via $rail settled! (Ref: $payRef)"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Deposit failed"
                )
            }
        }
    }

    fun processWithdrawal() {
        val amount = _uiState.value.withdrawAmount.toDoubleOrNull() ?: 0.0
        val acc = _uiState.value.bankAccountNumber
        val bank = _uiState.value.selectedBank
        val curr = _uiState.value.selectedCurrencyCode
        val currInfo = WorldwideCurrencies.getByCode(curr)

        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a valid withdrawal amount")
            return
        }

        val rail = _uiState.value.depositRail
        if (rail == "BANK" && acc.length < 8) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a valid account or IBAN number")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            val amountInNgn = if (curr == "NGN") {
                amount
            } else {
                val usd = amount / currInfo.exchangeRateToUsd
                usd * 1620.0
            }

            val result = walletRepository.requestWithdrawal(amountInNgn, acc, "$bank ($rail)")
            if (result.isSuccess) {
                val refId = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isWithdrawOpen = false,
                    successMessage = "Withdrawal of ${currInfo.symbol}${"%,.2f".format(amount)} queued via $rail. Ref: $refId"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Withdrawal failed"
                )
            }
        }
    }
}
