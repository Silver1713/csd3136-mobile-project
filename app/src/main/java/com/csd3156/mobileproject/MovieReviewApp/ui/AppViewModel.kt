package com.csd3156.mobileproject.MovieReviewApp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUIState())
    val uiState: StateFlow<AppUIState> = _uiState


    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoggedIn = false)
            }
        }
    }

    fun setLoggedIn(value: Boolean)  {
        viewModelScope.launch{
            _uiState.update {
                it.copy(isLoggedIn = value)
            }
        }
    }

    fun loginAccount(id: Int){
        viewModelScope.launch {
            _uiState.update {
                it.copy(accountID = id)
            }
            setLoggedIn(true)
        }
    }


}


data class AppUIState (
    val isLoggedIn : Boolean = false,
    val accountID: Int = 0,
)