package com.example.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.components.ArtmuseumSegmentedControl
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.ArtmuseumOliveEmblem
import com.example.ui.components.ArtmuseumPrimaryButton
import com.example.ui.components.ArtmuseumSecondaryButton
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.MuseumBorder
import com.example.ui.theme.MuseumCream
import com.example.ui.theme.MutedTaupe
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.WarmPaper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activityContext = context as? Activity ?: context
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type returned")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in with Google failed. Please try again.")
        }
    }
}

fun signOutArtmuseum(
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtmuseumWelcomeAuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmailAuthSheet by remember { mutableStateOf(false) }

    val permissionsToRequest = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            add(Manifest.permission.CAMERA)
        }.toTypedArray()
    }

    var showPermissionIntroDialog by rememberSaveable { mutableStateOf(false) }
    var hasPromptedIntroPermissions by rememberSaveable { mutableStateOf(false) }
    var permissionsGrantedNotice by rememberSaveable { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        showPermissionIntroDialog = false
        permissionsGrantedNotice = results.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPromptedIntroPermissions) {
            hasPromptedIntroPermissions = true
            delay(400L)
            val needsNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            } else false
            val needsStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            }
            if (needsNotif || needsStorage) {
                showPermissionIntroDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    val triggerGoogleSignIn = {
        errorMessage = null
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MuseumCream)
    ) {
        // Top painterly background blending seamlessly into Museum Cream (matches Screen 1 of mockup)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .align(Alignment.Center)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Botanical Olive Emblem
            ArtmuseumOliveEmblem(
                size = 44.dp,
                showCircleMedallion = false
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Wordmark "Artmuseum"
            Text(
                text = "Artmuseum",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = PlayfairDisplayFontFamily,
                    fontSize = 38.sp,
                    color = InkPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Hero painting from mockup with soft top/bottom cream vignette
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.art_welcome_sea),
                    contentDescription = "Classical coastal painting",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Soft gradient fade at top and bottom edges to match mockup atmosphere
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MuseumCream.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MuseumCream.copy(alpha = 0.94f))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quote in italic serif
            Text(
                text = "“Art is not what you see,\nbut what you make others see.”",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlayfairDisplayFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                    color = InkPrimary.copy(alpha = 0.82f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtle pager indicator line matching mockup
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(MutedTaupe.copy(alpha = 0.65f))
                )
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(MutedTaupe.copy(alpha = 0.25f))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Intro Permissions Access Prompt Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEDE9E0))
                    .clickable { showPermissionIntroDialog = true }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("intro_permissions_chip")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF5D634E),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (permissionsGrantedNotice) "Gallery & Notifications Configured ✓" else "Enable Gallery & Notification Access",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFBEAE8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9C2F2F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Primary Pill Button: Continue with Google
            ArtmuseumPrimaryButton(
                text = "Continue with Google",
                onClick = triggerGoogleSignIn,
                isLoading = isLoading,
                leadingContent = { GoogleBadgeIcon() },
                modifier = Modifier.fillMaxWidth(),
                testTag = "google_sign_in_button"
            )

            Spacer(modifier = Modifier.height(11.dp))

            // Secondary Pill Button: Continue with Email
            ArtmuseumSecondaryButton(
                text = "Continue with Email",
                onClick = { showEmailAuthSheet = true },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = InkPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "continue_with_email_button"
            )

            Spacer(modifier = Modifier.height(11.dp))

            // Tertiary Button: Explore as Guest
            ArtmuseumSecondaryButton(
                text = "Explore as Guest",
                onClick = onContinueAsGuest,
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = InkPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "continue_as_guest_button"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Already have an account? Sign In
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(ArtmuseumShapeTokens.Pill)
                    .clickable { triggerGoogleSignIn() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("sign_in_footer_button")
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        color = MutedTaupe
                    )
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Developer Credit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .testTag("developer_credit_rahul_shah")
            ) {
                Text(
                    text = "Made with ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        color = MutedTaupe
                    )
                )
                Text(
                    text = "❤",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFFC04B40)
                    )
                )
                Text(
                    text = " by Rahul Shah",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                )
            }
        }
    }

    if (showPermissionIntroDialog) {
        ArtmuseumIntroPermissionsDialog(
            onDismiss = { showPermissionIntroDialog = false },
            onGrantRequested = {
                permissionsLauncher.launch(permissionsToRequest)
            }
        )
    }

    if (showEmailAuthSheet) {
        EmailAuthModalSheet(
            onAuthSuccess = onAuthSuccess,
            onDismiss = { showEmailAuthSheet = false }
        )
    }
}

@Composable
fun ArtmuseumIntroPermissionsDialog(
    onDismiss: () -> Unit,
    onGrantRequested: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MuseumCream,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ArtmuseumOliveEmblem(size = 38.dp, showCircleMedallion = false)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome to Artmuseum",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlayfairDisplayFontFamily,
                        fontSize = 22.sp,
                        color = InkPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enable notifications and gallery access to enjoy the full museum experience.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        color = MutedTaupe,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Permission row 1: Notifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1ECE2))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5D634E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFF7F2E8),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary,
                                fontSize = 13.5.sp
                            )
                        )
                        Text(
                            text = "Real-time updates on likes, comments, and new followers.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MutedTaupe
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Permission row 2: Gallery Access
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1ECE2))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5D634E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = Color(0xFFF7F2E8),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gallery & Photos",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary,
                                fontSize = 13.5.sp
                            )
                        )
                        Text(
                            text = "Customize profile banners, avatars & exhibit original artworks.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MutedTaupe
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                ArtmuseumPrimaryButton(
                    text = "Grant All Permissions",
                    onClick = onGrantRequested,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "grant_all_permissions_button"
                )

                Spacer(modifier = Modifier.height(8.dp))

                ArtmuseumSecondaryButton(
                    text = "Continue",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dismiss_permissions_button"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailAuthModalSheet(
    onAuthSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WarmPaper,
        shape = ArtmuseumShapeTokens.LargeSheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSignUp) "Create Artmuseum Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = PlayfairDisplayFontFamily
                ),
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSignUp) "Enter your email & password to register" else "Sign in with your email & password",
                style = MaterialTheme.typography.bodySmall,
                color = MutedTaupe
            )

            Spacer(modifier = Modifier.height(16.dp))

            ArtmuseumSegmentedControl(
                options = listOf("Sign In", "Sign Up"),
                selectedIndex = if (isSignUp) 1 else 0,
                onSelect = {
                    isSignUp = it == 1
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFBEAE8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9C2F2F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Email address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_auth_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_auth_password_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            ArtmuseumPrimaryButton(
                text = if (isSignUp) "Create Account" else "Sign In",
                onClick = {
                    val cleanEmail = email.trim()
                    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                        errorMessage = "Please enter a valid email address."
                        return@ArtmuseumPrimaryButton
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                        return@ArtmuseumPrimaryButton
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            if (isSignUp) {
                                Firebase.auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                            } else {
                                try {
                                    Firebase.auth.signInWithEmailAndPassword(cleanEmail, password).await()
                                } catch (e: Exception) {
                                    // If sign in fails because user is new, automatically register
                                    Firebase.auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                                }
                            }
                            isLoading = false
                            onAuthSuccess()
                        } catch (e: Exception) {
                            isLoading = false
                            Log.e("Auth", "Email auth failed", e)
                            val msg = e.localizedMessage ?: "Authentication failed"
                            errorMessage = if (msg.contains("CONFIGURATION_NOT_FOUND") || msg.contains("ADMIN_ONLY_OPERATION")) {
                                "Email/Password sign-in is disabled in Firebase Console. Please sign in with Google."
                            } else {
                                msg
                            }
                        }
                    }
                },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_email_auth_button"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GoogleBadgeIcon() {
    Surface(
        shape = CircleShape,
        color = Color.White,
        modifier = Modifier.size(22.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(13.dp)) {
                val stroke = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = Color(0xFFEA4335),
                    startAngle = -140f,
                    sweepAngle = 100f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = Color(0xFFFBBC05),
                    startAngle = 130f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = Color(0xFF34A853),
                    startAngle = 40f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = Color(0xFF4285F4),
                    startAngle = -40f,
                    sweepAngle = 80f,
                    useCenter = false,
                    style = stroke
                )
            }
        }
    }
}

@Composable
fun GuestSignInPromptDialog(
    onDismiss: () -> Unit,
    onSignInWithGoogle: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MuseumCream,
            border = BorderStroke(1.dp, MuseumBorder),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                ArtmuseumOliveEmblem(size = 42.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sign in to Artmuseum",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You are currently browsing as a Guest. Sign in with Google to create exhibits, share thoughts, like, bookmark, and chat with creators.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkPrimary.copy(alpha = 0.82f),
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                ArtmuseumPrimaryButton(
                    text = "Sign in with Google",
                    onClick = {
                        onDismiss()
                        onSignInWithGoogle()
                    },
                    leadingContent = { GoogleBadgeIcon() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_google_sign_in_button"
                )
                Spacer(modifier = Modifier.height(10.dp))
                ArtmuseumSecondaryButton(
                    text = "Continue Browsing",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_dismiss_button"
                )
            }
        }
    }
}
