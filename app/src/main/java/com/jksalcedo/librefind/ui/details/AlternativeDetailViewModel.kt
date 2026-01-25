package com.jksalcedo.librefind.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.model.VoteType
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

            val userVotes: Map<String, Int?> = if (user != null) {
                appRepository.getUserVote(altId, user.uid)
            } else {
                mapOf("usability" to null, "privacy" to null, "features" to null)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    alternative = alternative?.copy(
                        userUsabilityRating = userVotes["usability"],
                        userPrivacyRating = userVotes["privacy"],
                        userFeaturesRating = userVotes["features"],
                        userRating = userVotes["usability"]
                    ),
                    isSignedIn = user != null,
                    username = user?.username
                )
            }
        }
    }

    fun rate(stars: Int) {
        rateDimension(VoteType.USABILITY, stars)
    }

    fun rateDimension(voteType: VoteType, stars: Int) {
        if (!_state.value.isSignedIn) return

        _state.update { state ->
            state.copy(
                alternative = state.alternative?.copy(
                    userUsabilityRating = if (voteType == VoteType.USABILITY) stars else state.alternative.userUsabilityRating,
                    userPrivacyRating = if (voteType == VoteType.PRIVACY) stars else state.alternative.userPrivacyRating,
                    userFeaturesRating = if (voteType == VoteType.FEATURES) stars else state.alternative.userFeaturesRating,
                    userRating = if (voteType == VoteType.USABILITY) stars else state.alternative.userRating
                )
            )
        }

        viewModelScope.launch {
            appRepository.castVote(currentAltId, voteType.key, stars)
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
