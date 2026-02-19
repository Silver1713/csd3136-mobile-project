package com.csd3156.mobileproject.MovieReviewApp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.Account
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import com.csd3156.mobileproject.MovieReviewApp.data.repository.AccountRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<AccountUIState> = MutableStateFlow(AccountUIState())
    val uiState: StateFlow<AccountUIState> = _uiState.asStateFlow()


    val activeUser : Flow<AccountDomain?> = repository.getActiveAccountRoom()

    init {
        viewModelScope.launch {
            repository.refreshActiveAccountRemote()
        }
    }


    fun login(
        username: String,
        password: String
    ) {

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    accountSelected = null,
                    isLoginLoading = true,
                    loginErrorMessage = null
                )
            }


            val accountDomain: AccountDomain? = repository.validateLoginAccount(username, password)

            if (accountDomain != null) {

                _uiState.update {
                    it.copy(
                        accountSelected = accountDomain,
                        isLoginLoading = false,
                        loginErrorMessage = null
                    )
                }

            } else {
                _uiState.update {
                    it.copy(
                        accountSelected = null,
                        isLoginLoading = false,
                        loginErrorMessage = "Invalid username or password"
                    )
                }

            }
        }
    }

    fun setLoginLoading(isLoading: Boolean) {
        _uiState.update {
            it.copy(isLoginLoading = isLoading)
        }
    }

    fun clearLoginResult() {
        _uiState.update {
            it.copy(
                accountSelected = null,
                loginErrorMessage = null
            )
        }
    }

    fun clearRegisterResult() {
        _uiState.update {
            it.copy(
                accountSelected = null,
                registerErrorMessage = null,
                registerSuccessMessage = null
            )
        }
    }

    fun register(
        email: String,
        username: String,
        password: String,
        name: String? = null,
        bio: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRegisterLoading = true,
                    registerErrorMessage = null,
                    registerSuccessMessage = null
                )
            }

            val normalizedUsername = username.trim().lowercase()
            if (normalizedUsername.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isRegisterLoading = false,
                        registerErrorMessage = "Username cannot be empty"
                    )
                }
                return@launch
            }

            if (!isPasswordComplex(password)) {
                _uiState.update {
                    it.copy(
                        isRegisterLoading = false,
                        registerErrorMessage = "Password must be 8+ chars with upper, lower, number, and symbol"
                    )
                }
                return@launch
            }


            val result: RequestResult<String?> =
                repository.registerAccount(email, normalizedUsername, password)
            when (result) {
                is RequestResult.Success -> {
                    val uid = result.data
                    if (uid != null) {
                        val newAccount = repository.getAccountByUID(uid)
                        _uiState.update {
                            it.copy(
                                accountSelected = newAccount,
                                isRegisterLoading = false,
                                registerSuccessMessage = "Account created successfully",
                                registerErrorMessage = null
                            )
                        }
                    }

                }

                is RequestResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            registerErrorMessage = "Error creating account: ${result.message}"
                        )


                    }


                }


            }
        }
    }

    private fun isPasswordComplex(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        return hasUpper && hasLower && hasDigit && hasSymbol
    }


}


data class AccountUIState(
    val accountSelected: AccountDomain? = null,
    val isLoginLoading: Boolean = false,
    val loginErrorMessage: String? = null,
    val isRegisterLoading: Boolean = false,
    val registerErrorMessage: String? = null,
    val registerSuccessMessage: String? = null
)
