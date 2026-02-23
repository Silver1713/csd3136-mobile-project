package com.csd3156.mobileproject.MovieReviewApp.ui.accountsettings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.ui.components.LoadImage
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object AccountSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(

    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {

    val accountSettingViewModel : AccountSettingViewModel = hiltViewModel()

    val accountInfo by accountSettingViewModel.accountInfo.collectAsStateWithLifecycle(null)
    val uiState by accountSettingViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var displayName by rememberSaveable { mutableStateOf(accountInfo?.name ?: "") }
    var bio by rememberSaveable {
        mutableStateOf(accountInfo?.bio ?: "")
    }

    LaunchedEffect(
        accountInfo
    ) {
        accountInfo?.let {
            displayName = it.name ?: ""
            bio = it.bio ?: ""
        }
    }

    val cameraPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    var pendingCropOutputPath by remember { mutableStateOf<String?>(null) }

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val cropOutputPath = pendingCropOutputPath
        if (result.resultCode == Activity.RESULT_OK) {
            if (!cropOutputPath.isNullOrBlank()) {
                accountSettingViewModel.setPendingCapturePath(cropOutputPath)
                accountSettingViewModel.handleTakePictureResult(true)
            } else {
                accountSettingViewModel.handleTakePictureResult(false)
                Toast.makeText(context, "Failed to crop image", Toast.LENGTH_SHORT).show()
            }
        } else {
            cropOutputPath?.let { path ->
                runCatching {
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }
            val error = result.data?.let { UCrop.getError(it) }
            accountSettingViewModel.handleTakePictureResult(false)
            if (error != null) {
                Toast.makeText(context, "Crop failed: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
        pendingCropOutputPath = null
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            accountSettingViewModel.handleTakePictureResult(false)
            return@rememberLauncherForActivityResult
        }

        val sourcePath = uiState.pendingCapturePath
        if (sourcePath.isNullOrBlank()) {
            accountSettingViewModel.handleTakePictureResult(false)
            Toast.makeText(context, "Unable to read captured photo", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val cropDestinationPath = context.createProfileCropOutputPath() ?: run {
            accountSettingViewModel.handleTakePictureResult(false)
            Toast.makeText(context, "Unable to prepare crop image", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        pendingCropOutputPath = cropDestinationPath
        val sourceUri = Uri.fromFile(File(sourcePath))
        val destinationUri = Uri.fromFile(File(cropDestinationPath))
        val cropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(720, 720)
            .withOptions(
                UCrop.Options().apply {
                    setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                    setCompressionQuality(85)
                    setHideBottomControls(true)
                    setFreeStyleCropEnabled(false)
                }
            )
            .getIntent(context)
        cropLauncher.launch(cropIntent)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val granted = results.values.all { it }
        if (granted) {
            val (captureUri, absolutePath) = context.createProfileImageFile() ?: run {
                Toast.makeText(context, "Unable to access camera", Toast.LENGTH_SHORT).show()
                accountSettingViewModel.setPendingCapturePath(null)
                return@rememberLauncherForActivityResult
            }
            accountSettingViewModel.setPendingCapturePath(absolutePath)
            takePictureLauncher.launch(captureUri)
        } else {
            Toast.makeText(context, "Camera permission is required to change profile photo", Toast.LENGTH_SHORT).show()
        }
    }

    val profileImageUrl = when {
        !uiState.draftPhotoPath.isNullOrBlank() -> Uri.fromFile(File(uiState.draftPhotoPath)).toString()
        uiState.removePhoto -> null
        !accountInfo?.profileUrl.isNullOrBlank() -> accountInfo?.profileUrl
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        accountSettingViewModel.clearDraftOnDismiss()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Profile Photo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl != null) {
                        LoadImage(
                            url = profileImageUrl,
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape),
                            contentDescription = "Profile photo"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { permissionLauncher.launch(cameraPermissions) },
                        enabled = !uiState.isSaving
                    ) {
                        Text("Change Photo")
                    }
                    OutlinedButton(
                        onClick = {
                            accountSettingViewModel.removeProfilePhoto()
                        },
                        enabled = !uiState.isSaving
                    ) {
                        Text("Remove")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Name",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Enter your name") }
            )

            Text(
                text = "Bio",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Tell others about your taste in movies") }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        val result = accountSettingViewModel.updateAccount(
                            fullName = displayName,
                            bio = bio
                        )
                        if (result) {
                            onSave()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }
            uiState.saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(92.dp))
        }
    }
}

private fun Context.createProfileImageFile(): Pair<Uri, String>? {
    return runCatching {
        val imageDir = File(filesDir, "profiles").apply { if (!exists()) mkdirs() }
        val imageFile = File.createTempFile("profile_${System.currentTimeMillis()}", ".jpg", imageDir)
        val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
        contentUri to imageFile.absolutePath
    }.getOrNull()
}

private fun Context.createProfileCropOutputPath(): String? {
    return runCatching {
        val imageDir = File(filesDir, "profiles").apply { if (!exists()) mkdirs() }
        File.createTempFile("profile_crop_${System.currentTimeMillis()}", ".jpg", imageDir).absolutePath
    }.getOrNull()
}
