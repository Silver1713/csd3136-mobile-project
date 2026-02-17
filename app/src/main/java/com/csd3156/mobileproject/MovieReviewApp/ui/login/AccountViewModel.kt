package com.csd3156.mobileproject.MovieReviewApp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.Account
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.Account.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor (
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState : MutableStateFlow<AccountUIState> = MutableStateFlow(AccountUIState())
    val uiState : StateFlow<AccountUIState> = _uiState.asStateFlow()



    private suspend fun validate(
        username: String,
        password: String

    ) : Boolean {
        val account = repository.findAccountByUser(username) ?: return false
        return repository.verifyPassword(account, password)
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

            val account = repository.findAccountByUser(username)
            val isValid = account != null && repository.verifyPassword(account, password)

            if (isValid) {
                _uiState.update {
                    it.copy(
                        accountSelected = account,
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

    fun register(
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

            val normalizedUsername = username.trim()
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

            val existing = repository.findAccountByUser(normalizedUsername)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isRegisterLoading = false,
                        registerErrorMessage = "Username already exists"
                    )
                }
                return@launch
            }

            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            val newAccount = Account(
                uuid = UUID.randomUUID(),
                username = normalizedUsername,
                hashed_password = hashedPassword,
                name = name?.trim()?.ifBlank { null },
                bio = bio?.trim()?.ifBlank { null }
            )

            val createdId = repository.createAccount(newAccount)
            if (createdId > 0) {
                _uiState.update {
                    it.copy(
                        accountSelected = newAccount.copy(id = createdId.toInt()),
                        isRegisterLoading = false,
                        registerSuccessMessage = "Account created successfully",
                        registerErrorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isRegisterLoading = false,
                        registerErrorMessage = "Failed to create account"
                    )
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


data class AccountUIState (
    val accountSelected : Account? = null,
    val isLoginLoading: Boolean = false,
    val loginErrorMessage: String? = null,
    val isRegisterLoading: Boolean = false,
    val registerErrorMessage: String? = null,
    val registerSuccessMessage: String? = null
)
