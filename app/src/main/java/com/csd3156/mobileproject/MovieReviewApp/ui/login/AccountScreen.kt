package com.csd3156.mobileproject.MovieReviewApp.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.csd3156.mobileproject.MovieReviewApp.domain.model.AccountDomain
import kotlinx.serialization.Serializable

@Serializable
data object AccountScreen


private enum class AuthMode {
    LOGIN,
    REGISTER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun accountScreen(accountVM : AccountViewModel, modifier: Modifier, onNavigate : (AccountDomain) -> Unit){
    var mode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }

    val activeUser : AccountDomain? by accountVM.activeUser.collectAsState(initial = null)

    if (activeUser != null){
        accountVM.setIsLogout(false)
        onNavigate(activeUser!!)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader()
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                selected = mode == AuthMode.LOGIN,
                                onClick = { mode = AuthMode.LOGIN }
                            ) {
                                Text("Login")

                            }
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                selected = mode == AuthMode.REGISTER,
                                onClick = { mode = AuthMode.REGISTER }
                            ) {
                                Text("Register")
                            }
                        }

                        if (mode == AuthMode.LOGIN) {
                            LoginForm(accountVM){
                                account ->
                                onNavigate(account)
                            }
                        } else {
                            RegisterForm(accountVM) { account ->
                                onNavigate(account)
                            }
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun AuthHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Welcome to MovieReview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Login or create an account to manage your profile",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoginForm(accountVM: AccountViewModel,onSubmit: (account : AccountDomain) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var submitAttempted by rememberSaveable { mutableStateOf(false) }

    val usernameError = if (submitAttempted && username.isBlank()) "Username is required" else null
    val passwordError = if (submitAttempted && password.isBlank()) "Password is required" else null
    val accountUIState by accountVM.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(accountUIState.accountSelected) {
        accountUIState.accountSelected?.let { account ->
            onSubmit(account)
            accountVM.clearLoginResult()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Sign in",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        AuthTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            leadingIcon = Icons.Filled.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            errorMessage = usernameError
        )
        AuthPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            showPassword = showPassword,
            onTogglePasswordVisibility = { showPassword = !showPassword },
            imeAction = ImeAction.Done,
            errorMessage = passwordError
        )
        Button(
            onClick = {
                submitAttempted = true
                if (usernameError == null && passwordError == null) {
                    accountVM.login(username, password)
                }
            },
            enabled = !accountUIState.isLoginLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (accountUIState.isLoginLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        if (accountUIState.loginErrorMessage != null) {
            Text(
                text = accountUIState.loginErrorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun RegisterForm(accountVM: AccountViewModel, onSubmit: (account: AccountDomain) -> Unit) {
    var email by rememberSaveable() {mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }
    var submitAttempted by rememberSaveable { mutableStateOf(false) }

    val emailError = if (submitAttempted && email.isBlank()) "Email is required" else null
    val nameError = if (submitAttempted && name.isBlank()) "Display name is required" else null
    val usernameError = if (submitAttempted && username.isBlank()) "Username is required" else null
    val passwordError = if (submitAttempted && password.isBlank()) "Password is required" else null
    val confirmPasswordError = when {
        !submitAttempted -> null
        confirmPassword.isBlank() -> "Confirm password is required"
        confirmPassword != password -> "Passwords do not match"
        else -> null
    }
    val uiState by accountVM.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.registerSuccessMessage, uiState.accountSelected) {
        if (uiState.registerSuccessMessage != null && uiState.accountSelected != null) {
            val  account = uiState.accountSelected
            if (account != null){
                onSubmit(account)
            }
            accountVM.clearRegisterResult()
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create account",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Display name",
            leadingIcon = Icons.Filled.AccountCircle,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            errorMessage = nameError
        )
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Filled.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            errorMessage = emailError
        )
        AuthTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            leadingIcon = Icons.Filled.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            errorMessage = usernameError
        )
        AuthPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            showPassword = showPassword,
            onTogglePasswordVisibility = { showPassword = !showPassword },
            imeAction = ImeAction.Next,
            errorMessage = passwordError
        )
        AuthPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            showPassword = showConfirmPassword,
            onTogglePasswordVisibility = { showConfirmPassword = !showConfirmPassword },
            imeAction = ImeAction.Done,
            errorMessage = confirmPasswordError
        )
        Button(
            onClick = {
                submitAttempted = true
                if (nameError == null && usernameError == null && passwordError == null && confirmPasswordError == null) {
                    accountVM.register(email,username, password, name, bio)
                }
            },
            enabled = !uiState.isRegisterLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (uiState.isRegisterLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create account")
            }
        }
        if (uiState.registerErrorMessage != null) {
            Text(
                text = uiState.registerErrorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions,
    errorMessage: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = errorMessage != null,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        supportingText = {
            if (errorMessage != null) {
                Text(text = errorMessage)
            }
        },
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showPassword: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    imeAction: ImeAction,
    errorMessage: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = errorMessage != null,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (showPassword) "Hide password" else "Show password"
                )
            }
        },
        supportingText = {
            if (errorMessage != null) {
                Text(text = errorMessage)
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(14.dp)
    )
}
