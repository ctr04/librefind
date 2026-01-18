package com.jksalcedo.librefind.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.data.remote.firebase.AuthService
import com.jksalcedo.librefind.data.remote.firebase.FirestoreService
import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.model.Feedback
import com.jksalcedo.librefind.domain.model.FeedbackType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlternativeDetailViewModel(
    private val firestoreService: FirestoreService,
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow(AlternativeDetailState())
    val state: StateFlow<AlternativeDetailState> = _state.asStateFlow()

    private var currentAltId: String = ""

    fun loadAlternative(altId: String) {
        currentAltId = altId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val uid = authService.getCurrentUser()?.uid
            val alternative = firestoreService.getAlternativeById(altId)
            val userRating = if (uid != null && alternative != null) {
                firestoreService.getUserRating(altId, uid)
            } else null
            
            _state.update {
                it.copy(
                    isLoading = false,
                    alternative = alternative?.copy(userRating = userRating),
                    isSignedIn = uid != null,
                    username = if (uid != null) firestoreService.getProfile(uid)?.username else null
                )
            }
        }
    }

    fun rate(stars: Int) {
        val uid = authService.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            if (firestoreService.rateAlternative(currentAltId, uid, stars)) {
                _state.update { state ->
                    state.alternative?.let { alt ->
                        val newCount = if (alt.userRating == null) alt.ratingCount + 1 else alt.ratingCount
                        val oldRating = alt.userRating ?: 0
                        val newAvg = if (newCount > 0) {
                            (alt.ratingAvg * alt.ratingCount - oldRating + stars) / newCount
                        } else stars.toFloat()
                        state.copy(alternative = alt.copy(userRating = stars, ratingAvg = newAvg, ratingCount = newCount))
                    } ?: state
                }
            }
        }
    }

    fun submitFeedback(type: String, text: String) {
        val uid = authService.getCurrentUser()?.uid ?: return
        val username = _state.value.username ?: return
        
        viewModelScope.launch {
            val feedback = Feedback(
                uid = uid,
                username = username,
                type = FeedbackType.valueOf(type),
                text = text
            )
            firestoreService.submitFeedback(currentAltId, feedback)
        }
    }
}

data class AlternativeDetailState(
    val isLoading: Boolean = false,
    val alternative: Alternative? = null,
    val isSignedIn: Boolean = false,
    val username: String? = null
)
