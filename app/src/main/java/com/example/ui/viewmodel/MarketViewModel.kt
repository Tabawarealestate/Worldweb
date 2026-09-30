package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AdminRepository
import com.example.data.repository.MarketDataRepository
import com.example.domain.model.Instrument
import com.example.domain.model.InstrumentCategory
import com.example.domain.model.MarketQuote
import com.example.domain.model.SystemHealth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MarketUiState(
    val selectedCategory: InstrumentCategory = InstrumentCategory.ALL,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val selectedSymbol: String? = null,
    val systemHealthList: List<SystemHealth> = emptyList(),
    val errorMessage: String? = null
)

class MarketViewModel(
    private val marketDataRepository: MarketDataRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    val allInstruments: StateFlow<List<Instrument>> = marketDataRepository.allInstruments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuotes: StateFlow<List<MarketQuote>> = marketDataRepository.allQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredQuotes: StateFlow<List<MarketQuote>> = combine(
        marketDataRepository.allQuotes,
        marketDataRepository.allInstruments,
        _uiState
    ) { quotes, instruments, state ->
        val instrumentMap = instruments.associateBy { it.symbol }
        quotes.filter { quote ->
            val inst = instrumentMap[quote.symbol]
            val matchesCategory = when (state.selectedCategory) {
                InstrumentCategory.ALL -> true
                InstrumentCategory.MARKET_SURGE -> quote.symbol in listOf("BTC/USD", "ETH/USD", "SOL/USD", "XAU/USD")
                else -> inst?.category == state.selectedCategory
            }
            val matchesSearch = state.searchQuery.isEmpty() ||
                    quote.symbol.contains(state.searchQuery, ignoreCase = true) ||
                    (inst?.name?.contains(state.searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            marketDataRepository.initializeCatalog()
            adminRepository.initializeDefaultProducts()
            refreshMarketData()
            startContinuousPricePolling()
        }
    }

    fun selectCategory(category: InstrumentCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectSymbol(symbol: String?) {
        _uiState.value = _uiState.value.copy(selectedSymbol = symbol)
    }

    fun refreshMarketData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            try {
                val reports = marketDataRepository.refreshQuotes()
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    systemHealthList = reports
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = e.localizedMessage ?: "Failed to refresh market data"
                )
            }
        }
    }

    private fun startContinuousPricePolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(4000) // Poll real market data every 4 seconds
                try {
                    marketDataRepository.refreshQuotes()
                } catch (_: Exception) {}
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
