package com.csd3156.mobileproject.MovieReviewApp.ui.reviewlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.data.repository.ReviewRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ReviewListUIState(
    val isLoading : Boolean = false,
    val errorMessage : String? = null

)

@HiltViewModel
class ReviewListViewModel @Inject constructor(

    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewListUIState(isLoading = true))
    val uiState : StateFlow<ReviewListUIState> = _uiState.asStateFlow()

    val userReviews = reviewRepository.getCachedUserReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun loadUserReviews(){
        viewModelScope.launch {
            reviewRepository.refreshUserReviews()
        }
    }
}