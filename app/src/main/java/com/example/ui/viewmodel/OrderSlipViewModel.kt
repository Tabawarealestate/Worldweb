package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.OrderRepository
import com.example.domain.model.ContractDirection
import com.example.domain.model.Order
import com.example.domain.model.ProductType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderSlipState(
    val isOpen: Boolean = false,
    val symbol: String = "BTC/USD",
    val productType: ProductType = ProductType.HIGHER_LOWER,
    val direction: ContractDirection = ContractDirection.HIGHER,
    val durationSeconds: Long = 300, // 5 minutes
    val stake: Double = 5000.0,
    val currentPrice: Double = 0.0,
    val isSubmitting: Boolean = false,
    val lastPlacedOrder: Order? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val fee: Double get() = stake * 0.01
    val potentialPayout: Double get() = stake * 1.85
    val potentialProfit: Double get() = potentialPayout - stake
}

class OrderSlipViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _slipState = MutableStateFlow(OrderSlipState())
    val slipState: StateFlow<OrderSlipState> = _slipState.asStateFlow()

    val openOrders: StateFlow<List<Order>> = orderRepository.openOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settledOrders: StateFlow<List<Order>> = orderRepository.settledOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openSlip(
        symbol: String,
        direction: ContractDirection,
        productType: ProductType = ProductType.HIGHER_LOWER,
        currentPrice: Double = 0.0
    ) {
        _slipState.value = _slipState.value.copy(
            isOpen = true,
            symbol = symbol,
            direction = direction,
            productType = productType,
            currentPrice = currentPrice,
            errorMessage = null,
            successMessage = null
        )
    }

    fun closeSlip() {
        _slipState.value = _slipState.value.copy(isOpen = false)
    }

    fun setStake(newStake: Double) {
        if (newStake >= 0) {
            _slipState.value = _slipState.value.copy(stake = newStake, errorMessage = null)
        }
    }

    fun setDuration(seconds: Long) {
        _slipState.value = _slipState.value.copy(durationSeconds = seconds)
    }

    fun setDirection(dir: ContractDirection) {
        _slipState.value = _slipState.value.copy(direction = dir)
    }

    fun setProductType(type: ProductType) {
        _slipState.value = _slipState.value.copy(productType = type)
    }

    fun placeOrder() {
        val state = _slipState.value
        if (state.stake < 500.0) {
            _slipState.value = state.copy(errorMessage = "Minimum contract stake is ₦500")
            return
        }

        viewModelScope.launch {
            _slipState.value = _slipState.value.copy(isSubmitting = true, errorMessage = null)
            val result = orderRepository.placeOrder(
                symbol = state.symbol,
                productType = state.productType,
                direction = state.direction,
                stake = state.stake,
                durationSeconds = state.durationSeconds
            )

            if (result.isSuccess) {
                val order = result.getOrThrow()
                _slipState.value = _slipState.value.copy(
                    isSubmitting = false,
                    lastPlacedOrder = order,
                    successMessage = "Order #${order.id} placed successfully!",
                    isOpen = false
                )
            } else {
                _slipState.value = _slipState.value.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to place order"
                )
            }
        }
    }

    fun settleOrderNow(orderId: String) {
        viewModelScope.launch {
            orderRepository.settleOrder(orderId)
        }
    }
}
