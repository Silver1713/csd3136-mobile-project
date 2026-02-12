package com.csd3156.mobileproject.MovieReviewApp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun setIsSearching(value: Boolean) {
        _isSearching.value = value
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return HomeScreenViewModel() as T
    }
}
