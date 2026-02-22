package com.csd3156.mobileproject.MovieReviewApp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import com.csd3156.mobileproject.MovieReviewApp.ui.components.LoadImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object Profile

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    reviewsCount: Int = 0,
    favoritesCount: Int = 0,
    onEditClick: () -> Unit = {},
    onMyReviews: () -> Unit = {},
    onMyWatchlist: () -> Unit = {},
    onAccountSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val currentScope = rememberCoroutineScope()

    val profileVM : ProfileViewModel = hiltViewModel()
    val currentAccount : AccountDomain? by profileVM.accountInfo.collectAsStateWithLifecycle(null)
    val reviewCount : Int by profileVM.reviewCount.collectAsStateWithLifecycle(0)
    val favoritesCount : Int by profileVM.watchlistCount.collectAsStateWithLifecycle(0)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            currentScope.launch {
                isRefreshing = true
                profileVM.refreshAccount()
                delay(800)

                isRefreshing = false
            }
        },

    ) {


        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top row: Profile + edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(Modifier.height(18.dp))

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!currentAccount?.profileUrl.isNullOrBlank()) {
                    LoadImage(
                        url = currentAccount?.profileUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = currentAccount?.name
                            ?.firstOrNull()
                            ?.uppercase()
                            ?: currentAccount?.username?.firstOrNull()?.uppercase()
                            ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Name placeholder bar
//        Box(
//            modifier = Modifier
//                .height(18.dp)
//                .width(160.dp)
//                .align(Alignment.CenterHorizontally)
//                .clip(RoundedCornerShape(6.dp))
//                .background(MaterialTheme.colorScheme.surfaceVariant)
//        ){
//
//        }
            // Name
            Column(
                modifier = Modifier.padding(16.dp)
                    .align(Alignment.CenterHorizontally),

                ) {
                Text(
                    currentAccount?.name ?: "YOUR NAME HERE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "@${currentAccount?.username ?: "username"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(18.dp))

            // Reviews + Favorites
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(value = reviewCount, label = "REVIEWS", modifier = Modifier.weight(1f))
                StatBox(value = favoritesCount, label = "FAVORITES", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "ACCOUNT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            AccountRow(icon = Icons.Default.RateReview, title = "My Reviews", onClick = onMyReviews)
            Spacer(Modifier.height(10.dp))
            AccountRow(
                icon = Icons.Default.Bookmark,
                title = "My Watchlist",
                onClick = onMyWatchlist
            )
            Spacer(Modifier.height(10.dp))
            AccountRow(
                icon = Icons.Default.Settings,
                title = "Account Settings",
                onClick = onAccountSettings
            )

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(Modifier.width(10.dp))
                Text("Log Out")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatBox(value: Int, label: String, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AccountRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
