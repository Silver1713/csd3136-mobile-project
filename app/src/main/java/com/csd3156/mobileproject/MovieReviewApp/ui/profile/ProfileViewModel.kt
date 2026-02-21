package com.csd3156.mobileproject.MovieReviewApp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csd3156.mobileproject.MovieReviewApp.data.local.database.watchlist.WatchlistRepository
import com.csd3156.mobileproject.MovieReviewApp.data.repository.AccountRepository
import com.csd3156.mobileproject.MovieReviewApp.data.repository.ReviewRepository
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.csd3156.mobileproject.MovieReviewApp.ui.watchlist.WatchlistViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountAuth: FirebaseAuth,
    private val accountRepository: AccountRepository,
    private val watchlistRepository: WatchlistRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    init {
        refreshAccount()
    }

    val accountInfo : Flow<AccountDomain?>  = accountRepository.getActiveAccountRoom()
    val reviewCount : Flow<Int> = reviewRepository.getCachedUserReviewCount()
    val watchlistCount : Flow<Int> = watchlistRepository.getWatchlistCount()

    fun refreshAccount(){
        viewModelScope.launch {
            accountRepository.refreshActiveAccountRemote()
            reviewRepository.refreshUserReviews()
        }

    }







}