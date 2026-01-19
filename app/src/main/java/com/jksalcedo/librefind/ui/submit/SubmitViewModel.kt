package com.jksalcedo.librefind.ui.submit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.librefind.data.remote.firebase.AuthService
import com.jksalcedo.librefind.data.remote.firebase.DuplicateResult
import com.jksalcedo.librefind.data.remote.firebase.FirestoreService
import com.jksalcedo.librefind.domain.model.Submission
import com.jksalcedo.librefind.domain.model.SubmissionType
import com.jksalcedo.librefind.domain.model.SubmittedApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmitUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val proprietaryTargets: List<String> = emptyList(),
    val duplicateWarning: String? = null,
    val packageNameError: String? = null,
    val repoUrlError: String? = null,
    val submittedAppName: String? = null
)

class SubmitViewModel(
    private val authService: AuthService,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmitUiState())
    val uiState: StateFlow<SubmitUiState> = _uiState.asStateFlow()

    init {
        loadProprietaryTargets()
    }

    private fun loadProprietaryTargets() {
        viewModelScope.launch {
            val targets = firestoreService.getProprietaryTargets()
            _uiState.value = _uiState.value.copy(proprietaryTargets = targets)
        }
    }

    fun submit(
        type: SubmissionType,
        appName: String,
        packageName: String,
        description: String,
        repoUrl: String = "",
        fdroidId: String = "",
        license: String = "",
        proprietaryPackages: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val user = authService.getCurrentUser()
            if (user == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Not signed in")
                return@launch
            }

            if (_uiState.value.packageNameError != null || _uiState.value.repoUrlError != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Please fix validation errors")
                return@launch
            }

            val profile = firestoreService.getProfile(user.uid)
            if (profile == null) {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = "Profile not set up")
                return@launch
            }

            val submission = Submission(
                type = type,
                proprietaryPackages = proprietaryPackages,
                submitterUid = user.uid,
                submitterUsername = profile.username,
                submittedApp = SubmittedApp(
                    name = appName,
                    packageName = packageName,
                    repoUrl = repoUrl,
                    fdroidId = fdroidId,
                    description = description,
                    license = license
                )
            )

            firestoreService.submitEntry(submission)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        submittedAppName = appName
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Submission failed"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = SubmitUiState(proprietaryTargets = _uiState.value.proprietaryTargets)
    }

    private var checkDuplicateJob: kotlinx.coroutines.Job? = null

    fun checkDuplicate(name: String, packageName: String) {
        checkDuplicateJob?.cancel()
        checkDuplicateJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            if (name.isBlank() && packageName.isBlank()) {
                _uiState.value = _uiState.value.copy(duplicateWarning = null)
                return@launch
            }

            val result = firestoreService.checkDuplicateApp(name, packageName)
            val warning = when (result) {
                is DuplicateResult.ProprietaryMatch ->
                    "This app is already in our database as a proprietary target: ${result.name}"

                is DuplicateResult.FossMatch ->
                    "This app is already in our database as a FOSS solution: ${result.name}"

                is DuplicateResult.Error ->
                    null // Ignore errors
                DuplicateResult.NoMatch ->
                    null
            }
            _uiState.value = _uiState.value.copy(duplicateWarning = warning)
        }
    }

    fun validatePackageName(packageName: String) {
        // Regex: ^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]$
        // Starts with a letter
        // Contains lowercase letters, numbers, underscores
        // Must have at least one dot separating parts
        // Parts must start with letter/number/underscore (regex says [a-z0-9_]+ so yes)
        // Ends with letter/number/underscore
        val regex = Regex("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$")
        val isValid = regex.matches(packageName)
        
        _uiState.value = _uiState.value.copy(
            packageNameError = if (isValid) null else "Invalid package name format (e.g. com.example.app)"
        )
    }

    fun validateRepoUrl(url: String) {
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(repoUrlError = null)
            return
        }
        
        val isValid = url.startsWith("https://")
        _uiState.value = _uiState.value.copy(
            repoUrlError = if (isValid) null else "URL must start with https://"
        )
    }
}
