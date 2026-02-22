package com.csd3156.mobileproject.MovieReviewApp.ui.accountsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.repository.AccountRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    data class UiState(
        val draftPhotoPath: String? = null,
        val pendingCapturePath: String? = null,
        val removePhoto: Boolean = false,
        val isSaving: Boolean = false,
        val saveError: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    val accountInfo : Flow<AccountDomain?>  = accountRepository.getActiveAccountRoom()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun refresh(){
        viewModelScope.launch {
            accountRepository.refreshActiveAccountRemote()
        }
    }

    fun setPendingCapturePath(path: String?) {
        val existingPending = _uiState.value.pendingCapturePath
        if (existingPending != null && existingPending != path) {
            deleteLocalFileIfExists(existingPending)
        }
        _uiState.value = _uiState.value.copy(pendingCapturePath = path)
    }

    fun handleTakePictureResult(success: Boolean) {
        val pendingPath = _uiState.value.pendingCapturePath
        val currentDraft = _uiState.value.draftPhotoPath
        if (success) {
            if (currentDraft != null && currentDraft != pendingPath) {
                deleteLocalFileIfExists(currentDraft)
            }
            _uiState.value = _uiState.value.copy(
                draftPhotoPath = pendingPath,
                pendingCapturePath = null,
                removePhoto = false,
                saveError = null
            )
        } else {
            pendingPath?.let(::deleteLocalFileIfExists)
            _uiState.value = _uiState.value.copy(pendingCapturePath = null)
        }
    }

    fun removeProfilePhoto() {
        _uiState.value.draftPhotoPath?.let(::deleteLocalFileIfExists)
        _uiState.value = _uiState.value.copy(
            draftPhotoPath = null,
            removePhoto = true,
            saveError = null
        )
    }

    fun clearDraftOnDismiss() {
        _uiState.value.pendingCapturePath?.let(::deleteLocalFileIfExists)
        _uiState.value.draftPhotoPath?.let(::deleteLocalFileIfExists)
        _uiState.value = _uiState.value.copy(
            pendingCapturePath = null,
            draftPhotoPath = null,
            removePhoto = false,
            saveError = null
        )
    }

    suspend fun updateAccount(fullName: String? = null, bio: String? = null): Boolean {
        _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
        val result = accountRepository.updateAccountProfile(
            fullName = fullName,
            bio = bio,
            localPhotoPath = _uiState.value.draftPhotoPath,
            removePhoto = _uiState.value.removePhoto
        )
        _uiState.value = when (result) {
            is RequestResult.Success -> _uiState.value.copy(
                isSaving = false,
                draftPhotoPath = null,
                pendingCapturePath = null,
                removePhoto = false,
                saveError = null
            )
            is RequestResult.Error -> _uiState.value.copy(
                isSaving = false,
                saveError = result.message ?: "Failed to update account"
            )
        }
        return result is RequestResult.Success
    }

    private fun deleteLocalFileIfExists(path: String) {
        runCatching {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }

}
