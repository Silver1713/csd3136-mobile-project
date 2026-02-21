package com.csd3156.mobileproject.MovieReviewApp.ui.accountsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountDAO
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.AccountFirestoreService
import com.csd3156.mobileproject.MovieReviewApp.data.repository.AccountRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

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

    fun updateAccount(fullName: String? = null, bio: String? = null, profilePicUrl : String? = null){
        viewModelScope.launch {
            accountRepository.changeAccountCredentials(fullName, bio, profilePicUrl)
        }

    }

}