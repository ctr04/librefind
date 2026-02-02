package com.jksalcedo.librefind.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.domain.model.Report
import com.jksalcedo.librefind.domain.repository.AppRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReportsUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyReportsViewModel(
    private val repository: AppRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReportsUiState())
    val uiState: StateFlow<MyReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val reports = repository.getMyReports(userId)
                _uiState.update { it.copy(reports = reports, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to load reports", isLoading = false) }
            }
        }
    }
}
