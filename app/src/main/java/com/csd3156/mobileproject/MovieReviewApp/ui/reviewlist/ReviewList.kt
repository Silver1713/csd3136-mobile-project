package com.csd3156.mobileproject.MovieReviewApp.ui.reviewlist

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.let

@Serializable
data object ReviewList

private enum class ReviewSort(val label: String) {
    Newest("Newest"),
    Oldest("Oldest"),
    HighestRated("Highest Rated"),
    LowestRated("Lowest Rated")
}

private enum class MediaType {
    Movie,
    Tv
}

private data class PlaceholderReview(
    val movieTitle: String,
    val rating: Double,
    val date: LocalDate,
    val mediaType: MediaType,
    val content: String,
    val hasPhoto: Boolean = false,
    val isEdited: Boolean = false
)

private val placeholderReviews = listOf(
    PlaceholderReview(
        movieTitle = "Inception",
        rating = 9.4,
        date = LocalDate.of(2026, 2, 14),
        mediaType = MediaType.Movie,
        content = "Strong visuals, tight pacing, and a soundtrack that carries every scene.",
        hasPhoto = true,
        isEdited = false
    ),
    PlaceholderReview(
        movieTitle = "Spider-Man: Into the Spider-Verse",
        rating = 8.6,
        date = LocalDate.of(2026, 2, 8),
        mediaType = MediaType.Movie,
        content = "Creative animation style and great character moments from start to finish.",
        hasPhoto = false,
        isEdited = true
    ),
    PlaceholderReview(
        movieTitle = "Interstellar",
        rating = 9.0,
        date = LocalDate.of(2026, 1, 29),
        mediaType = MediaType.Movie,
        content = "Emotional core is solid and the set pieces feel massive even on rewatch.",
        hasPhoto = false,
        isEdited = false
    ),
    PlaceholderReview(
        movieTitle = "Dune: Part Two",
        rating = 8.2,
        date = LocalDate.of(2026, 1, 11),
        mediaType = MediaType.Movie,
        content = "Large-scale direction and sound design are excellent.",
        hasPhoto = true,
        isEdited = true
    ),
    PlaceholderReview(
        movieTitle = "The Last of Us",
        rating = 8.9,
        date = LocalDate.of(2025, 12, 20),
        mediaType = MediaType.Tv,
        content = "Excellent performances and strong episode pacing.",
        hasPhoto = false,
        isEdited = true
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviewListVM : ReviewListViewModel = hiltViewModel()
    val reviews by reviewListVM.userReviews.collectAsStateWithLifecycle()



    val monthHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(ReviewSort.Newest) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showFiltersSheet by remember { mutableStateOf(false) }

    var includeMovies by remember { mutableStateOf(true) }
    var includeTv by remember { mutableStateOf(true) }
    var withPhotosOnly by remember { mutableStateOf(false) }
    var editedOnly by remember { mutableStateOf(false) }

    var draftIncludeMovies by remember { mutableStateOf(includeMovies) }
    var draftIncludeTv by remember { mutableStateOf(includeTv) }
    var draftWithPhotosOnly by remember { mutableStateOf(withPhotosOnly) }
    var draftEditedOnly by remember { mutableStateOf(editedOnly) }

    val activeFilterCount = listOf(
        includeMovies != true || includeTv != true,
        withPhotosOnly,
        editedOnly
    ).count { it }

    val filteredReviews = reviews
        .filter { review ->
            review.movieTitle?.contains(searchQuery.trim(), ignoreCase = true) ?: false
        }
//        .filter { review ->
//            (includeMovies && review.mediaType == MediaType.Movie) ||
//                (includeTv && review.mediaType == MediaType.Tv)
//        }
//        .filter { review ->
//            (!withPhotosOnly || (review.photoPath?.isEmpty() == false)) && (!editedOnly || review.isEdited)
//        }
        .let { reviews ->
            when (selectedSort) {
                ReviewSort.Newest -> reviews.sortedByDescending {
                    // Convert string date
                    it.createdAt.toEpochMilli()
                }
                ReviewSort.Oldest -> reviews.sortedBy {
                    // Convert string date
                    it.createdAt.toEpochMilli()
                }
                ReviewSort.HighestRated -> reviews.sortedByDescending { it.rating }
                ReviewSort.LowestRated -> reviews.sortedBy { it.rating }
            }
        }

    val groupedReviews = filteredReviews.groupBy {
        val localDate = it.createdAt
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        localDate.withDayOfMonth(1) }
    val useMonthGrouping = selectedSort == ReviewSort.Newest || selectedSort == ReviewSort.Oldest

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reviews") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            if (placeholderReviews.isEmpty()) {
                Text(
                    text = "No reviews yet.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        placeholder = { Text("Search by movie title") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box {
                            AssistChip(
                                onClick = { sortMenuExpanded = !sortMenuExpanded },
                                label = { Text("Sort: ${selectedSort.label}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                ReviewSort.entries.forEach { sort ->
                                    DropdownMenuItem(
                                        text = { Text(sort.label) },
                                        onClick = {
                                            selectedSort = sort
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        AssistChip(
                            onClick = {
                                draftIncludeMovies = includeMovies
                                draftIncludeTv = includeTv
                                draftWithPhotosOnly = withPhotosOnly
                                draftEditedOnly = editedOnly
                                showFiltersSheet = true
                            },
                            label = {
                                Text(
                                    if (activeFilterCount > 0) {
                                        "Filters ($activeFilterCount)"
                                    } else {
                                        "Filters"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.FilterList, contentDescription = null)
                            }
                        )
                    }

                    if (filteredReviews.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No reviews match your current search or filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (useMonthGrouping) {
                                val monthComparator = if (selectedSort == ReviewSort.Newest) {
                                    compareByDescending<LocalDate> { it }
                                } else {
                                    compareBy<LocalDate> { it }
                                }

                                groupedReviews.toSortedMap(monthComparator).forEach { (month, reviews) ->
                                    item {
                                        Text(
                                            text = monthHeaderFormatter.format(month),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    items(reviews) { review ->
                                        val date = review.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
                                        val dateText = dateFormatter.format(date)
                                        ReviewCard(
                                            review = review,
                                            dateText = dateText
                                        )
                                    }
                                }
                            } else {
                                items(filteredReviews) { review ->
                                    val date = review.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
                                    val dateText = dateFormatter.format(date)
                                    ReviewCard(
                                        review = review,
                                        dateText = dateText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Filter Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Content Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = draftIncludeMovies,
                        onClick = { draftIncludeMovies = !draftIncludeMovies },
                        label = { Text("Movies") }
                    )
                    FilterChip(
                        selected = draftIncludeTv,
                        onClick = { draftIncludeTv = !draftIncludeTv },
                        label = { Text("TV") }
                    )
                }

                Text(
                    text = "Attributes",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = draftWithPhotosOnly,
                        onClick = { draftWithPhotosOnly = !draftWithPhotosOnly },
                        label = { Text("With Photos") }
                    )
                    FilterChip(
                        selected = draftEditedOnly,
                        onClick = { draftEditedOnly = !draftEditedOnly },
                        label = { Text("Edited") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            draftIncludeMovies = true
                            draftIncludeTv = true
                            draftWithPhotosOnly = false
                            draftEditedOnly = false
                        }
                    ) {
                        Text("Reset")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            includeMovies = draftIncludeMovies
                            includeTv = draftIncludeTv
                            withPhotosOnly = draftWithPhotosOnly
                            editedOnly = draftEditedOnly
                            showFiltersSheet = false
                        }
                    ) {
                        Text("Apply")
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: MovieReview?,
    dateText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = review?.movieTitle ?: "UNTITLED MOVIE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${"%.1f".format(review?.rating ?: 0)}/10 \u2022 $dateText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = review?.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
