package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AuditLogEntity
import com.example.data.repository.AdminRepository
import com.example.domain.model.FinancialProductConfig
import com.example.domain.model.ProductType
import com.example.domain.model.SystemHealth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminUiState(
    val productName: String = "BTC Fast 60s",
    val productSymbol: String = "BTC/USD",
    val productType: ProductType = ProductType.HIGHER_LOWER,
    val durationSeconds: Long = 60,
    val minStake: Double = 1000.0,
    val maxStake: Double = 500_000.0,
    val feeRate: Double = 0.01,
    val payoutRate: Double = 1.85,
    val settlementRule: String = "Settled against Binance Spot API at expiry",
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    val activeProducts: StateFlow<List<FinancialProductConfig>> = adminRepository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = adminRepository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemStatusList: List<SystemHealth> = adminRepository.getPublicSystemStatus()

    fun updateForm(
        name: String = _uiState.value.productName,
        symbol: String = _uiState.value.productSymbol,
        type: ProductType = _uiState.value.productType,
        duration: Long = _uiState.value.durationSeconds,
        min: Double = _uiState.value.minStake,
        max: Double = _uiState.value.maxStake,
        fee: Double = _uiState.value.feeRate,
        payout: Double = _uiState.value.payoutRate,
        rule: String = _uiState.value.settlementRule
    ) {
        _uiState.value = _uiState.value.copy(
            productName = name,
            productSymbol = symbol,
            productType = type,
            durationSeconds = duration,
            minStake = min,
            maxStake = max,
            feeRate = fee,
            payoutRate = payout,
            settlementRule = rule,
            successMessage = null,
            errorMessage = null
        )
    }

    fun saveProduct() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val result = adminRepository.createProduct(
                name = state.productName,
                symbol = state.productSymbol,
                productType = state.productType,
                durationSeconds = state.durationSeconds,
                minStake = state.minStake,
                maxStake = state.maxStake,
                feeRate = state.feeRate,
                payoutRate = state.payoutRate,
                settlementRule = state.settlementRule
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Financial Product '${state.productName}' published successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to save product"
                )
            }
        }
    }
}
