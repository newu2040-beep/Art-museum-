package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityNotification
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.UserBlock
import com.example.data.model.UserProfile
import com.example.data.repository.ArtmuseumRepository
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ArtmuseumViewModel(
    private val repository: ArtmuseumRepository,
    private val currentUserId: String,
    private val initialDisplayName: String = "Sophia"
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("For You")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTheme = MutableStateFlow(ArtmuseumThemePalette.CREAM)
    val activeTheme: StateFlow<ArtmuseumThemePalette> = _activeTheme.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private val _selectedPostComments = MutableStateFlow<List<PostComment>>(emptyList())
    val selectedPostComments: StateFlow<List<PostComment>> = _selectedPostComments.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private var commentsJob: Job? = null

    val userProfileState: StateFlow<UiState<UserProfile>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile>> { profile ->
            if (profile != null) {
                _activeTheme.value = ArtmuseumThemePalette.fromName(profile.themePreference)
                UiState.Success(profile)
            } else {
                UiState.Loading
            }
        }
        .catch { e ->
            Log.w(TAG, "Error observing user profile", e)
            emit(UiState.Error(e.localizedMessage ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val savedPostIds: StateFlow<Set<String>> = repository.observeSavedPostIds(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing saved posts", e)
            emit(emptySet())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptySet()
        )

    val blockedUsers: StateFlow<List<UserBlock>> = repository.observeBlockedUsers(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing blocked users", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val notifications: StateFlow<List<ActivityNotification>> = repository.observeNotifications(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing notifications", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val publicUsers: StateFlow<List<UserProfile>> = repository.observePublicUsers()
        .catch { e ->
            Log.w(TAG, "Error observing public users", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val rawPublicPosts: StateFlow<UiState<List<Post>>> = repository.observePublicPosts()
        .map<List<Post>, UiState<List<Post>>> { posts -> UiState.Success(posts) }
        .catch { e ->
            Log.w(TAG, "Error observing public posts", e)
            emit(UiState.Error(e.localizedMessage ?: "Failed to load museum feed"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val feedState: StateFlow<UiState<List<Post>>> = combine(
        rawPublicPosts,
        _selectedFilter,
        blockedUsers
    ) { postsUi, filter, blocks ->
        val blockedSet = blocks.map { it.blockedUid }.toSet()
        when (postsUi) {
            is UiState.Loading -> UiState.Loading
            is UiState.Error -> postsUi
            is UiState.Success -> {
                val visible = postsUi.data.filter { it.authorId !in blockedSet }
                val filtered = if (filter == "For You") {
                    visible
                } else {
                    visible.filter { post ->
                        post.topics.any { it.equals(filter, ignoreCase = true) } ||
                            post.text.contains(filter, ignoreCase = true)
                    }
                }
                UiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading
    )

    val userOwnPosts: StateFlow<List<Post>> = repository.observeUserPosts(currentUserId)
        .catch { e ->
            Log.w(TAG, "Error observing user own posts", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        bootstrapSession()
    }

    private fun bootstrapSession() {
        viewModelScope.launch {
            val result = repository.createOrInitializeProfile(
                displayName = initialDisplayName.ifBlank { "Sophia" },
                bio = "Just a girl who finds beauty in meaningful conversations, art, nature and real connections. \uD83C\uDF3F",
                age = 23,
                gender = "Female",
                photoURL = "preset:avatar_sophia",
                themePreference = "Cream"
            )
            result.onSuccess { profile ->
                _activeTheme.value = ArtmuseumThemePalette.fromName(profile.themePreference)
                repository.ensureInitialExhibitsIfEmpty(profile)
            }
        }
    }

    fun selectFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showBannerMessage(message: String) {
        _statusBanner.value = message
    }

    fun observeCommentsForPost(postId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            val liked = repository.isPostLiked(postId, currentUserId)
            if (liked) {
                _likedPostIds.value = _likedPostIds.value + postId
            }
            repository.observeComments(postId)
                .catch { e -> Log.w(TAG, "Error observing comments for $postId", e) }
                .collect { comments ->
                    _selectedPostComments.value = comments
                }
        }
    }

    fun toggleLike(post: Post) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" }
        )
        // Optimistic UI toggle with rollback on failure
        val wasLiked = post.postId in _likedPostIds.value
        _likedPostIds.value = if (wasLiked) {
            _likedPostIds.value - post.postId
        } else {
            _likedPostIds.value + post.postId
        }
        viewModelScope.launch {
            val result = repository.toggleLike(post, currentProfile)
            result.onSuccess { nowLiked ->
                _likedPostIds.value = if (nowLiked) {
                    _likedPostIds.value + post.postId
                } else {
                    _likedPostIds.value - post.postId
                }
            }.onFailure { err ->
                // Rollback optimistic update
                _likedPostIds.value = if (wasLiked) {
                    _likedPostIds.value + post.postId
                } else {
                    _likedPostIds.value - post.postId
                }
                _statusBanner.value = err.localizedMessage ?: "Unable to update like"
            }
        }
    }

    fun toggleSave(postId: String) {
        viewModelScope.launch {
            val result = repository.toggleSavePost(postId)
            result.onSuccess { saved ->
                _statusBanner.value = if (saved) "Saved to your collection" else "Removed from Saved"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to save exhibit"
            }
        }
    }

    fun publishPost(
        text: String,
        imageURL: String,
        imageCaption: String,
        topics: List<String>,
        themePalette: ArtmuseumThemePalette,
        onSuccess: () -> Unit
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" },
            photoURL = "preset:avatar_sophia",
            age = 23,
            gender = "Female"
        )
        _isSubmitting.value = true
        viewModelScope.launch {
            val result = repository.createPost(
                authorProfile = profile,
                text = text.ifBlank { imageCaption.ifBlank { "Quiet reflections from today's gallery." } },
                imageURL = imageURL,
                imageCaption = imageCaption,
                topics = topics,
                themeName = themePalette.label
            )
            _isSubmitting.value = false
            result.onSuccess {
                _activeTheme.value = themePalette
                repository.updateThemePreference(themePalette.label)
                _statusBanner.value = "Exhibit published to Artmuseum"
                onSuccess()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to publish post"
            }
        }
    }

    fun deletePost(postId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess {
                _statusBanner.value = "Exhibit deleted"
                onDeleted()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to delete post"
            }
        }
    }

    fun sendComment(post: Post, text: String, onComplete: () -> Unit) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" }
        )
        _isSubmitting.value = true
        viewModelScope.launch {
            val res = repository.addComment(post, profile, text)
            _isSubmitting.value = false
            res.onSuccess {
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to post comment"
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId)
        }
    }

    fun saveProfileChanges(
        displayName: String,
        bio: String,
        ageInput: String,
        gender: String,
        photoURL: String,
        themePalette: ArtmuseumThemePalette,
        notificationsEnabled: Boolean,
        profileVisibility: String,
        onSaved: () -> Unit
    ) {
        val parsedAge = ageInput.trim().toIntOrNull()
        if (parsedAge == null || parsedAge !in 13..120) {
            _statusBanner.value = "Please enter a valid numeric age between 13 and 120."
            return
        }
        if (gender != "Male" && gender != "Female") {
            _statusBanner.value = "Please select Male or Female."
            return
        }
        _isSubmitting.value = true
        viewModelScope.launch {
            val res = repository.updateProfile(
                displayName = displayName,
                bio = bio,
                age = parsedAge,
                gender = gender,
                photoURL = photoURL,
                themePreference = themePalette.label,
                notificationsEnabled = notificationsEnabled,
                profileVisibility = profileVisibility
            )
            _isSubmitting.value = false
            res.onSuccess {
                _activeTheme.value = themePalette
                _statusBanner.value = "Profile updated"
                onSaved()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to update profile"
            }
        }
    }

    fun selectTheme(palette: ArtmuseumThemePalette) {
        _activeTheme.value = palette
        viewModelScope.launch {
            repository.updateThemePreference(palette.label)
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun reportContent(targetId: String, targetType: String, reason: String) {
        viewModelScope.launch {
            repository.submitReport(targetId, targetType, reason).onSuccess {
                _statusBanner.value = "Report submitted to Trust & Safety"
            }.onFailure {
                _statusBanner.value = "Could not submit report"
            }
        }
    }

    fun blockUser(blockedUid: String, blockedName: String) {
        if (blockedUid == currentUserId) {
            _statusBanner.value = "You cannot block your own account"
            return
        }
        viewModelScope.launch {
            repository.blockUser(blockedUid, blockedName).onSuccess {
                _statusBanner.value = "Blocked $blockedName"
            }
        }
    }

    fun unblockUser(blockedUid: String) {
        viewModelScope.launch {
            repository.unblockUser(blockedUid).onSuccess {
                _statusBanner.value = "User unblocked"
            }
        }
    }

    fun deleteOwnAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccountAndOwnContent().onSuccess {
                onDeleted()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to delete account data"
            }
        }
    }

    companion object {
        private const val TAG = "ArtmuseumVM"

        suspend fun encodeUriToCompactDataUri(context: Context, uri: Uri): String? =
            withContext(Dispatchers.IO) {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: return@withContext null
                    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: return@withContext null
                    encodeBitmapToCompactDataUri(original)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to encode URI to data URI", e)
                    null
                }
            }

        suspend fun encodeBitmapToCompactDataUri(bitmap: Bitmap): String? =
            withContext(Dispatchers.IO) {
                try {
                    val maxDim = 480
                    val ratio = minOf(
                        maxDim.toFloat() / bitmap.width.coerceAtLeast(1),
                        maxDim.toFloat() / bitmap.height.coerceAtLeast(1),
                        1f
                    )
                    val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                    val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                    val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 65, out)
                    val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    "data:image/jpeg;base64,$base64"
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to encode bitmap", e)
                    null
                }
            }
    }
}
