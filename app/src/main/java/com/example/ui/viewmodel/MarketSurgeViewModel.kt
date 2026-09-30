package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MarketSurgeRepository
import com.example.domain.model.SurgeEntry
import com.example.domain.model.SurgeRound
import com.example.domain.model.SurgeRoundStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SurgeUiState(
    val currentRound: SurgeRound? = null,
    val userEntry: SurgeEntry? = null,
    val stakeInput: Double = 2000.0,
    val autoCashoutInput: Double? = 2.00,
    val pastMultipliers: List<Double> = listOf(1.84, 2.45, 1.12, 4.20, 1.65, 3.10, 1.35, 6.50),
    val isEntering: Boolean = false,
    val isCashingOut: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class MarketSurgeViewModel(
    private val surgeRepository: MarketSurgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurgeUiState())
    val uiState: StateFlow<SurgeUiState> = _uiState.asStateFlow()

    val latestRound: StateFlow<SurgeRound?> = surgeRepository.latestRound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var roundLoopJob: Job? = null

    init {
        startRoundEngine()
    }

    fun setStake(stake: Double) {
        if (stake >= 0) {
            _uiState.value = _uiState.value.copy(stakeInput = stake, error = null)
        }
    }

    fun setAutoCashout(multiplier: Double?) {
        _uiState.value = _uiState.value.copy(autoCashoutInput = multiplier)
    }

    private fun startRoundEngine() {
        roundLoopJob?.cancel()
        roundLoopJob = viewModelScope.launch {
            while (isActive) {
                // 1. Create fresh round
                val round = surgeRepository.createNewRound("BTC/USD")
                _uiState.value = _uiState.value.copy(
                    currentRound = round,
                    userEntry = null,
                    error = null
                )

                // 2. Active Surge Phase: Multiplier climbs
                var mult = 1.00
                val burst = round.burstMultiplier
                val startTime = System.currentTimeMillis()

                while (mult < burst && isActive) {
                    delay(120) // tick every 120ms
                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                    mult = 1.00 + (0.09 * Math.pow(elapsedSec, 1.2))
                    if (mult >= burst) {
                        mult = burst
                        surgeRepository.updateMultiplier(round.roundId, mult)
                        break
                    }
                    surgeRepository.updateMultiplier(round.roundId, mult)

                    val updatedRound = round.copy(currentMultiplier = mult)
                    _uiState.value = _uiState.value.copy(currentRound = updatedRound)

                    // Auto cash out check
                    val entry = _uiState.value.userEntry
                    val autoTarget = _uiState.value.autoCashoutInput
                    if (entry != null && entry.status == "ACTIVE" && autoTarget != null && mult >= autoTarget) {
                        cashOutNow()
                    }
                }

                // 3. Burst Phase
                val past = (_uiState.value.pastMultipliers.takeLast(9) + burst)
                val burstedRound = round.copy(
                    currentMultiplier = burst,
                    status = SurgeRoundStatus.BURST
                )
                val burstedEntry = _uiState.value.userEntry?.let {
                    if (it.status == "ACTIVE") it.copy(status = "BUSTED") else it
                }
                _uiState.value = _uiState.value.copy(
                    currentRound = burstedRound,
                    userEntry = burstedEntry,
                    pastMultipliers = past
                )

                delay(4000) // 4 seconds intermission before next round
            }
        }
    }

    fun enterCurrentRound() {
        val round = _uiState.value.currentRound ?: return
        val stake = _uiState.value.stakeInput

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEntering = true, error = null)
            val result = surgeRepository.enterRound(
                roundId = round.roundId,
                stake = stake,
                autoCashout = _uiState.value.autoCashoutInput
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isEntering = false,
                    userEntry = result.getOrThrow(),
                    message = "Entered Surge round with ₦$stake"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isEntering = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Failed to enter round"
                )
            }
        }
    }

    fun cashOutNow() {
        val round = _uiState.value.currentRound ?: return
        val entry = _uiState.value.userEntry ?: return
        if (entry.status != "ACTIVE") return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCashingOut = true)
            val result = surgeRepository.cashOut(entry.entryId, round.roundId)
            if (result.isSuccess) {
                val payout = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isCashingOut = false,
                    userEntry = entry.copy(
                        status = "CASHED_OUT",
                        cashedOutMultiplier = round.currentMultiplier,
                        payout = payout
                    ),
                    message = "Cashed out at ${"%.2f".format(round.currentMultiplier)}x! Won ₦${"%,.0f".format(payout)}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isCashingOut = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        roundLoopJob?.cancel()
    }
}
