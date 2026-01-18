package com.jksalcedo.librefind.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.domain.model.AppItem
import com.jksalcedo.librefind.domain.model.AppStatus
import com.jksalcedo.librefind.domain.model.SovereigntyScore
import com.jksalcedo.librefind.domain.usecase.ScanInventoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Dashboard screen
 *
 * Manages app scanning state and sovereignty score calculation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val scanInventoryUseCase: ScanInventoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        viewModelScope.launch {
            refreshTrigger
                .onStart { emit(Unit) } // Trigger initial scan
                .flatMapLatest {
                    _state.update { it.copy(isLoading = true, error = null) }
                    scanInventoryUseCase()
                }
                .collect { apps ->
                    val score = calculateScore(apps)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            apps = apps,
                            sovereigntyScore = score,
                            error = null
                        )
                    }
                }
        }
    }

    fun scan() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    private fun calculateScore(apps: List<AppItem>): SovereigntyScore {
        val totalApps = apps.size
        val fossCount = apps.count { it.status == AppStatus.FOSS }
        val propCount = apps.count { it.status == AppStatus.PROP }
        val unknownCount = apps.count { it.status == AppStatus.UNKN }

        return SovereigntyScore(
            totalApps = totalApps,
            fossCount = fossCount,
            proprietaryCount = propCount,
            unknownCount = unknownCount
        )
    }
}

/**
 * UI State for Dashboard screen
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val apps: List<AppItem> = emptyList(),
    val sovereigntyScore: SovereigntyScore? = null,
    val error: String? = null
)
