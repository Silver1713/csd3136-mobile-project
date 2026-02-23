package com.csd3156.mobileproject.MovieReviewApp.ui.movies.details

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieDetails
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieReview
import com.csd3156.mobileproject.MovieReviewApp.domain.model.MovieVideo
import com.csd3156.mobileproject.MovieReviewApp.domain.model.WatchProvider
import com.csd3156.mobileproject.MovieReviewApp.ui.components.LoadImage
import com.csd3156.mobileproject.MovieReviewApp.ui.components.Sections
import com.csd3156.mobileproject.MovieReviewApp.ui.movies.list.MovieListViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import java.io.File
import kotlin.math.roundToInt

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.csd3156.mobileproject.MovieReviewApp.ui.watchlist.WatchlistViewModel
import com.csd3156.mobileproject.MovieReviewApp.domain.model.toMovie
import com.csd3156.mobileproject.MovieReviewApp.data.remote.api.RequestResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: MovieDetails?,
    movieVM: MovieListViewModel,
    reviews: List<MovieReview>,
    videos: List<MovieVideo>,
    watchProviders: List<WatchProvider>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onReviewClick: (MovieReview) -> Unit,
    onSeeAllReviews: () -> Unit,
    combinedAverageRating: Double,
    combinedRatingCount: Int,
    modifier: Modifier = Modifier

) {
    var shouldShowReviewDialog by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val detailUiState by movieVM.uiState.collectAsState()
    val author by movieVM.currentAccount.collectAsState(null)
    var reviewerName by rememberSaveable { mutableStateOf("") }
    var reviewContent by rememberSaveable { mutableStateOf("") }
    var reviewRating by rememberSaveable { mutableStateOf(6f) }
    val context = LocalContext.current
    val reviewPhotoPath = detailUiState.reviewPhotoPath

    val watchlistVM : WatchlistViewModel = hiltViewModel()






    val cameraPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        movieVM.handleTakePictureResult(success)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val granted = results.values.all { it }
        if (granted) {
            val (captureUri, absolutePath) = context.createReviewImageFile() ?: run {
                Toast.makeText(context, "Unable to access camera", Toast.LENGTH_SHORT).show()
                movieVM.setPendingCapturePath(null)
                return@rememberLauncherForActivityResult
            }
            movieVM.setPendingCapturePath(absolutePath)
            takePictureLauncher.launch(captureUri)
        } else {
            Toast.makeText(context, "Camera permission is required to add a photo", Toast.LENGTH_SHORT).show()
        }
    }

    var trailerToPlay by remember { mutableStateOf<MovieVideo?>(null) }
    val primaryTrailer = remember(videos) {
        val youtubeVideos = videos.filter { it.site.equals("YouTube", ignoreCase = true) }
        youtubeVideos.firstOrNull { it.isYoutubeTrailer && !it.official }
            ?: youtubeVideos.firstOrNull { it.isYoutubeTrailer }
            ?: youtubeVideos.firstOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
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
        when {
            movie != null -> {
                val isSaved by watchlistVM.isSaved(movie.id).collectAsState(initial = false)

                MovieDetailContent(
                    movie = movie,
                    reviews = reviews,
                    watchProviders = watchProviders,
                    combinedAverageRating = combinedAverageRating,
                    combinedRatingCount = combinedRatingCount,
                    hasTrailer = primaryTrailer != null,
                    onWatchTrailer = {
                        primaryTrailer?.let { trailerToPlay = it }
                    },
                    onBack = onBack,
                    onWriteReview = { shouldShowReviewDialog = true },
                    onReviewClick = onReviewClick,
                    onSeeAllReviews = onSeeAllReviews,
                    isSaved = isSaved,
                    onToggleWatchlist = {
                        watchlistVM.toggle(movie.toMovie(), isSaved)
                    }
                )
            }

            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unable to load movie details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        trailerToPlay?.let { video ->
            TrailerPlayerDialog(video = video, onDismiss = { trailerToPlay = null })
        }

        AnimatedVisibility(visible = errorMessage != null && movie != null) {
            errorMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (shouldShowReviewDialog) {
        WriteReviewDialog(
            name = author?.name ?: reviewerName,
            onNameChange = { reviewerName = it },
            content = reviewContent,
            onContentChange = { reviewContent = it },
            rating = reviewRating,
            onRatingChange = { reviewRating = it },
            photoPath = reviewPhotoPath,
            onAddPhoto = {
                permissionLauncher.launch(cameraPermissions)
            },
            onRemovePhoto = {
                movieVM.clearReviewPhoto()
            },
            onDismiss = {
                shouldShowReviewDialog = false
                movieVM.clearReviewPhotoDraft()
                movieVM.clearReviewSubmitError()
                reviewerName = ""
                reviewContent = ""
                reviewRating = 6f
            },
            onSubmit = {
                coroutineScope.launch {
                    val normalizedRating = (reviewRating * 10).roundToInt() / 10.0
                    val result = movieVM.addLocalReview(
                        movieId = movie?.id ?: return@launch,
                        movieTitle = movie.title,
                        author = reviewerName.trim(),
                        rating = normalizedRating,
                        content = reviewContent.trim(),
                        photoPath = reviewPhotoPath
                    )
                    if (result is RequestResult.Success) {
                        Toast.makeText(context, "Review saved locally", Toast.LENGTH_SHORT).show()
                        reviewerName = ""
                        reviewContent = ""
                        reviewRating = 6f
                        movieVM.resetReviewPhotoDraftState()
                        shouldShowReviewDialog = false
                    }
                }
            }
            ,
            isSubmitting = detailUiState.isSubmittingReview,
            submitError = detailUiState.reviewSubmitError
        )
    }
}

@Composable
private fun MovieDetailContent(
    movie: MovieDetails,
    reviews: List<MovieReview>,
    watchProviders: List<WatchProvider>,
    combinedAverageRating: Double,
    combinedRatingCount: Int,
    hasTrailer: Boolean,
    onWatchTrailer: () -> Unit,
    onBack: () -> Unit,
    onWriteReview: () -> Unit,
    onReviewClick: (MovieReview) -> Unit,
    onSeeAllReviews: () -> Unit,
    isSaved: Boolean,
    onToggleWatchlist: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            MovieHeroSection(movie, onBack = onBack)
        }
        item {
            ActionButtons(
                hasTrailer = hasTrailer,
                onWatchTrailer = onWatchTrailer,
                isSaved = isSaved,
                onToggleWatchlist = onToggleWatchlist
            )
        }
        item {
            Sections(
                title = "Synopsis",
                desc = movie.overview,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
        if (watchProviders.isNotEmpty()) {
            item {
                WatchProvidersSection(watchProviders)
            }
        }
        item {
            ReviewsHeader(onSeeAllReviews = onSeeAllReviews)
        }
        item {
            MovieRatingSummary(
                rating = combinedAverageRating,
                ratingCount = combinedRatingCount
            )
        }
        item {
            ReviewActions(onWriteReview = onWriteReview)
        }
        if (reviews.isEmpty()) {
            item {
                Text(
                    text = "No reviews yet. Be the first to share your thoughts!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp)
                )
            }
        } else {
            items(reviews.take(3)) { review ->
                ReviewCard(
                    review = review,
                    onClick = { onReviewClick(review) }
                )
            }
        }
    }
}

@Composable
private fun MovieHeroSection(movie: MovieDetails, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        LoadImage(
            url = movie.backdropUrl.ifBlank { movie.posterUrl },
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Go back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(12.dp)

            ){

                movie.genres.forEach {
                    Text(
                        text = it.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MovieMetadataChip(movie.releaseDate.take(4))
                MovieMetadataSeparator()
                MovieMetadataChip(movie.getFormattedRuntime())
                MovieMetadataSeparator()
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", movie.rating),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun MovieMetadataChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f)
    )
}

@Composable
private fun MovieMetadataSeparator() {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    )
}

@Composable
private fun WriteReviewDialog(
    name: String,
    onNameChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    photoPath: String?,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    submitError: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Write a Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Your thoughts") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 6
                )
                Column {
                    Text(
                        text = "Rating: ${String.format("%.1f", rating)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = rating,
                        onValueChange = onRatingChange,
                        valueRange = 0f..10f
                    )
                }
                if (photoPath != null) {
                    Text(
                        text = "Attached photo",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LoadImage(
                        url = Uri.fromFile(File(photoPath)).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentDescription = "Review photo",
                        contentScale = ContentScale.Crop
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onAddPhoto, enabled = !isSubmitting) {
                            Text("Retake photo")
                        }
                        TextButton(onClick = onRemovePhoto, enabled = !isSubmitting) {
                            Text("Remove photo")
                        }
                    }
                } else {
                    TextButton(onClick = onAddPhoto, enabled = !isSubmitting) {
                        Text("Add photo")
                    }
                }
                submitError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = content.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ActionButtons(
    hasTrailer: Boolean,
    onWatchTrailer: () -> Unit,
    isSaved: Boolean,
    onToggleWatchlist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MovieActionButton(
            text = "Watch Trailer",
            icon = Icons.Rounded.PlayArrow,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            enabled = hasTrailer,
            onClick = onWatchTrailer
        )
        MovieActionButton(
//            text = "Watchlist",
//            icon = Icons.Rounded.BookmarkAdd,
            icon = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkAdd,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            text = if (isSaved) "Saved" else "Watchlist",
            onClick = onToggleWatchlist

        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(26.dp),
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.6f),
        tonalElevation = if (enabled) 4.dp else 0.dp,
        shadowElevation = if (enabled) 6.dp else 0.dp,
        border = borderColor?.let { androidx.compose.foundation.BorderStroke(1.dp, it) },
        contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.6f),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WatchProvidersSection(watchProviders: List<WatchProvider>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Sections("Where to watch", null)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(watchProviders.take(10)) { provider ->
                WatchProviderChip(provider)
            }
        }
    }
}

@Composable
private fun WatchProviderChip(provider: WatchProvider) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = provider.name.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = provider.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewsHeader(onSeeAllReviews: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onSeeAllReviews) {
            Text(text = "See all")
        }
    }
}

@Composable
private fun MovieRatingSummary(rating: Double, ratingCount: Int) {
    val breakdown = remember(rating) {
        val bucketLabels = listOf("8-10", "6-8", "4-6", "2-4", "0-2")
        val base = (rating / 10.0).coerceIn(0.1, 1.0).toFloat()
        bucketLabels.mapIndexed { index, label ->
            val progress = (base - index * 0.15f).coerceIn(0.05f, 1f)
            label to progress
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "of 10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$ratingCount ratings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier
                    .width(1.dp)
                    .height(90.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            ) {}
            Column(modifier = Modifier.weight(1f)) {
                breakdown.forEach { (label, progress) ->
                    RatingBreakdownRow(label = label, progress = progress)
                }
            }
        }
    }
}

@Composable
private fun RatingBreakdownRow(label: String, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(34.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ReviewActions(onWriteReview: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = "Write Review",
            subtitle = "Share your thoughts",
            icon = Icons.Rounded.Edit,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.surface,
            onClick = onWriteReview
        )
        QuickActionCard(
            title = "Voice Note",
            subtitle = "Record audio review",
            icon = Icons.Rounded.MicNone,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: MovieReview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = review.author.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.author,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = review.createdAt.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                review.rating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            review.photoPath?.let { path ->
                Spacer(Modifier.height(12.dp))
                LoadImage(
                    url = if (path.startsWith("http", ignoreCase = true)) {
                        path
                    } else {
                        Uri.fromFile(File(path)).toString()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentDescription = "Review photo",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun TrailerPlayerDialog(
    video: MovieVideo,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (video.name.isNotBlank()) video.name else "Movie Trailer",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Playing from ${video.site}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close trailer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val context = LocalContext.current
                    TrailerYoutubePlayer(
                        videoKey = video.key,
                        onPlaybackError = {
                            Toast.makeText(
                                context,
                                "Trailer can't be embedded. Opening in YouTube.",
                                Toast.LENGTH_SHORT
                            ).show()
                            runCatching {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/embed/${video.key}")
                                )
                                context.startActivity(intent)
                            }
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

private fun Context.createReviewImageFile(): Pair<Uri, String>? {
    return runCatching {
        val imageDir = File(filesDir, "reviews").apply { if (!exists()) mkdirs() }
        val imageFile = File.createTempFile("review_${System.currentTimeMillis()}", ".jpg", imageDir)
        val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
        contentUri to imageFile.absolutePath
    }.getOrNull()
}

@Composable
private fun TrailerYoutubePlayer(
    videoKey: String,
    modifier: Modifier = Modifier,
    onPlaybackError: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val youTubePlayerView = remember(videoKey) {
        YouTubePlayerView(context).apply {
            lifecycleOwner.lifecycle.addObserver(this)
            enableAutomaticInitialization = false
            val listener = object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoKey, 0f)
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    if (error == PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ||
                        error == PlayerConstants.PlayerError.UNKNOWN
                    ) {
                        onPlaybackError()
                    }
                }
            }
            val options = IFramePlayerOptions.Builder(this.context)
                .controls(1)
                .autoplay(1)
                .ivLoadPolicy(1)
                .build()
            initialize(listener, options)
        }
    }

    DisposableEffect(youTubePlayerView, lifecycleOwner) {
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youTubePlayerView)
            youTubePlayerView.release()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        factory = { _ -> youTubePlayerView }
    )
}
