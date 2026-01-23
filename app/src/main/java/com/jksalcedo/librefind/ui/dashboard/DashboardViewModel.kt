package com.jksalcedo.librefind.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.domain.model.AppItem
import com.jksalcedo.librefind.domain.model.AppStatus
import com.jksalcedo.librefind.domain.model.SovereigntyScore
import com.jksalcedo.librefind.domain.repository.IgnoredAppsRepository
import com.jksalcedo.librefind.domain.usecase.ScanInventoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val scanInventoryUseCase: ScanInventoryUseCase,
    private val ignoredAppsRepository: IgnoredAppsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<AppStatus?>(null)

    init {
        viewModelScope.launch {
            combine(
                refreshTrigger
                    .onStart { emit(Unit) }
                    .flatMapLatest {
                        _state.update { it.copy(isLoading = true, error = null) }
                        scanInventoryUseCase()
                    },
                ignoredAppsRepository.getIgnoredPackageNames(),
                _searchQuery,
                _statusFilter
            ) { apps, ignoredPackages, query, statusFilter ->
                val filtered = apps
                    .filter { it.packageName !in ignoredPackages }
                    .filter { app ->
                        query.isBlank() ||
                        app.label.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                    }
                    .filter { app ->
                        statusFilter == null || app.status == statusFilter
                    }
                Pair(filtered, apps.filter { it.packageName !in ignoredPackages })
            }.collect { (filteredApps, allApps) ->
                val score = calculateScore(allApps)
                _state.update {
                    it.copy(
                        isLoading = false,
                        apps = filteredApps,
                        sovereigntyScore = score,
                        searchQuery = _searchQuery.value,
                        statusFilter = _statusFilter.value,
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: AppStatus?) {
        _statusFilter.value = status
    }

    fun ignoreApp(packageName: String) {
        viewModelScope.launch {
            ignoredAppsRepository.ignoreApp(packageName)
        }
    }

    fun restoreApp(packageName: String) {
        viewModelScope.launch {
            ignoredAppsRepository.restoreApp(packageName)
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

data class DashboardState(
    val isLoading: Boolean = false,
    val apps: List<AppItem> = emptyList(),
    val sovereigntyScore: SovereigntyScore? = null,
    val searchQuery: String = "",
    val statusFilter: AppStatus? = null,
    val error: String? = null
)


