package com.jksalcedo.librefind.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.data.remote.firebase.AuthService
import com.jksalcedo.librefind.data.remote.firebase.FirestoreService
import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.model.Feedback
import com.jksalcedo.librefind.domain.model.FeedbackType
import com.jksalcedo.librefind.domain.usecase.GetAlternativeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getAlternativeUseCase: GetAlternativeUseCase,
    private val firestoreService: FirestoreService,
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    fun loadAlternatives(packageName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val alternatives = getAlternativeUseCase(packageName)
                val uid = authService.getCurrentUser()?.uid
                
                // Fetch user ratings for each alternative
                val withRatings = alternatives.map { alt ->
                    if (uid != null) {
                        val userRating = firestoreService.getUserRating(alt.id, uid)
                        alt.copy(userRating = userRating)
                    } else alt
                }
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        packageName = packageName,
                        alternatives = withRatings,
                        isSignedIn = uid != null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load alternatives"
                    )
                }
            }
        }
    }

    fun rateAlternative(altId: String, stars: Int) {
        val uid = authService.getCurrentUser()?.uid ?: return
        
        viewModelScope.launch {
            if (firestoreService.rateAlternative(altId, uid, stars)) {
                // Update local state
                _state.update { state ->
                    state.copy(
                        alternatives = state.alternatives.map { alt ->
                            if (alt.id == altId) {
                                val newCount = if (alt.userRating == null) alt.ratingCount + 1 else alt.ratingCount
                                val oldRating = alt.userRating ?: 0
                                val newAvg = if (newCount > 0) {
                                    (alt.ratingAvg * alt.ratingCount - oldRating + stars) / newCount
                                } else stars.toFloat()
                                alt.copy(userRating = stars, ratingAvg = newAvg, ratingCount = newCount)
                            } else alt
                        }
                    )
                }
            }
        }
    }

    fun retry(packageName: String) {
        loadAlternatives(packageName)
    }
}

data class DetailsState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val alternatives: List<Alternative> = emptyList(),
    val isSignedIn: Boolean = false,
    val error: String? = null
)
