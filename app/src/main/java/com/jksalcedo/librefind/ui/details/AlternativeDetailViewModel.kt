package com.jksalcedo.librefind.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.repository.AppRepository
import com.jksalcedo.librefind.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlternativeDetailViewModel(
    private val appRepository: AppRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlternativeDetailState())
    val state: StateFlow<AlternativeDetailState> = _state.asStateFlow()

    private var currentAltId: String = ""

    fun loadAlternative(altId: String) {
        currentAltId = altId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val user = authRepository.getCurrentUser()
            val alternative = appRepository.getAlternative(altId)

            val userRating: Int? = if (user != null) {
                appRepository.getUserVote(altId, user.uid)
            } else {
                null
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    alternative = alternative?.copy(userRating = userRating),
                    isSignedIn = user != null,
                    username = user?.username
                )
            }
        }
    }

    fun rate(stars: Int) {
        if (!_state.value.isSignedIn) return

        _state.update { state ->
            state.copy(
                alternative = state.alternative?.copy(userRating = stars)
            )
        }

        viewModelScope.launch {
            appRepository.castVote(currentAltId, "usability", stars)
            loadAlternative(currentAltId)
        }
    }

    fun submitFeedback(type: String, text: String) {
        viewModelScope.launch {
            appRepository.submitFeedback(currentAltId, type, text)
        }
    }
}

data class AlternativeDetailState(
    val isLoading: Boolean = false,
    val alternative: Alternative? = null,
    val isSignedIn: Boolean = false,
    val username: String? = null
)
