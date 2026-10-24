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
import com.example.data.model.DirectChatConversation
import com.example.data.model.DirectChatMessage
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.UserBlock
import com.example.data.model.UserFollow
import com.example.data.model.UserProfile
import com.example.data.model.GUEST_PROFILE
import com.example.data.model.GUEST_SAMPLE_COMMENTS
import com.example.data.model.GUEST_SAMPLE_POSTS
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
    val currentUserId: String,
    private val initialDisplayName: String = "Sophia",
    val isGuest: Boolean = false
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("For You")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTheme = MutableStateFlow(ArtmuseumThemePalette.CREAM)
    val activeTheme: StateFlow<ArtmuseumThemePalette> = _activeTheme.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    // In-memory guest session data stores (pure real-time data)
    private val _guestPosts = MutableStateFlow<List<Post>>(emptyList())
    private val _guestSavedPostIds = MutableStateFlow<Set<String>>(emptySet())
    private val _guestComments = MutableStateFlow<List<PostComment>>(emptyList())

    val followingUserIds: StateFlow<Set<String>> = if (isGuest) {
        MutableStateFlow(emptySet())
    } else {
        repository.observeFollowingUserIds(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing following user IDs", e)
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptySet()
            )
    }

    val followersList: StateFlow<List<UserFollow>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeFollowers(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing followers", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    val followingList: StateFlow<List<UserFollow>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeFollowing(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing following", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    private val _selectedPostComments = MutableStateFlow<List<PostComment>>(emptyList())
    val selectedPostComments: StateFlow<List<PostComment>> = _selectedPostComments.asStateFlow()

    val conversations: StateFlow<List<DirectChatConversation>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeConversations(currentUserId)
            .catch { emit(emptyList()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<DirectChatMessage>>(emptyList())
    val activeChatMessages: StateFlow<List<DirectChatMessage>> = _activeChatMessages.asStateFlow()

    private var activeChatJob: Job? = null

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private var commentsJob: Job? = null

    val userProfileState: StateFlow<UiState<UserProfile>> = if (isGuest) {
        MutableStateFlow(UiState.Success(GUEST_PROFILE))
    } else {
        repository.observeUserProfile(currentUserId)
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
    }

    val savedPostIds: StateFlow<Set<String>> = if (isGuest) {
        _guestSavedPostIds.asStateFlow()
    } else {
        repository.observeSavedPostIds(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing saved posts", e)
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptySet()
            )
    }

    val blockedUsers: StateFlow<List<UserBlock>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeBlockedUsers(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing blocked users", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    val notifications: StateFlow<List<ActivityNotification>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeNotifications(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing notifications", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    val publicUsers: StateFlow<List<UserProfile>> = if (isGuest) {
        MutableStateFlow(listOf(GUEST_PROFILE))
    } else {
        repository.observePublicUsers()
            .catch { e ->
                Log.w(TAG, "Error observing public users", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    val followingUsers: StateFlow<List<UserFollow>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeFollowing(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing following", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    private val rawPublicPosts: StateFlow<UiState<List<Post>>> = if (isGuest) {
        _guestPosts
            .map<List<Post>, UiState<List<Post>>> { posts -> UiState.Success(posts) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Success(emptyList())
            )
    } else {
        repository.observePublicPosts()
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
    }

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

    val userOwnPosts: StateFlow<List<Post>> = if (isGuest) {
        MutableStateFlow(emptyList())
    } else {
        repository.observeUserPosts(currentUserId)
            .catch { e ->
                Log.w(TAG, "Error observing user own posts", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    init {
        bootstrapSession()
    }

    private fun bootstrapSession() {
        if (isGuest) {
            _activeTheme.value = ArtmuseumThemePalette.CREAM
            return
        }
        viewModelScope.launch {
            val result = repository.createOrInitializeProfile(
                displayName = initialDisplayName.ifBlank { "Art Enthusiast" },
                bio = "",
                age = 23,
                gender = "Female",
                photoURL = "preset:avatar_sophia",
                themePreference = "Cream"
            )
            result.onSuccess { profile ->
                _activeTheme.value = ArtmuseumThemePalette.fromName(profile.themePreference)
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
        if (isGuest) {
            _selectedPostComments.value = _guestComments.value.filter { it.postId == postId }
            return
        }
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
        if (isGuest) {
            val wasLiked = post.postId in _likedPostIds.value
            _likedPostIds.value = if (wasLiked) _likedPostIds.value - post.postId else _likedPostIds.value + post.postId
            _guestPosts.value = _guestPosts.value.map { p ->
                if (p.postId == post.postId) {
                    p.copy(likeCount = if (wasLiked) maxOf(0, p.likeCount - 1) else p.likeCount + 1)
                } else p
            }
            return
        }
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
        if (isGuest) {
            val isSaved = postId in _guestSavedPostIds.value
            _guestSavedPostIds.value = if (isSaved) _guestSavedPostIds.value - postId else _guestSavedPostIds.value + postId
            _statusBanner.value = if (isSaved) "Removed from Saved" else "Saved to your collection"
            return
        }
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
        aspectRatio: String = "1:1",
        onSuccess: () -> Unit
    ) {
        if (isGuest) {
            val newId = "guest_post_${System.currentTimeMillis()}"
            val newPost = Post(
                documentId = newId,
                postId = newId,
                authorId = "guest_visitor",
                authorNameSnapshot = "Guest Explorer",
                authorPhotoSnapshot = "preset:avatar_sophia",
                authorAgeSnapshot = 22,
                authorGenderSnapshot = "Visitor",
                text = text.ifBlank { imageCaption.ifBlank { "Quiet reflections from today's gallery." } },
                imageURL = imageURL,
                imageCaption = imageCaption,
                topics = topics,
                themeName = themePalette.label,
                likeCount = 0,
                commentCount = 0,
                visibility = "public",
                status = "active",
                aspectRatio = aspectRatio
            )
            _guestPosts.value = listOf(newPost) + _guestPosts.value
            _activeTheme.value = themePalette
            _statusBanner.value = "Exhibit published in Guest Mode"
            onSuccess()
            return
        }
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
                themeName = themePalette.label,
                aspectRatio = aspectRatio
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
        if (isGuest) {
            _guestPosts.value = _guestPosts.value.filter { it.postId != postId }
            _statusBanner.value = "Exhibit deleted"
            onDeleted()
            return
        }
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess {
                _statusBanner.value = "Exhibit deleted"
                onDeleted()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to delete post"
            }
        }
    }

    fun sendComment(
        post: Post,
        text: String,
        parentCommentId: String? = null,
        replyToAuthor: String? = null,
        onComplete: () -> Unit
    ) {
        if (isGuest) {
            val newComment = PostComment(
                commentId = "guest_cmt_${System.currentTimeMillis()}",
                postId = post.postId,
                authorId = "guest_visitor",
                authorNameSnapshot = "Guest Explorer",
                authorPhotoSnapshot = "preset:avatar_sophia",
                text = text,
                parentCommentId = parentCommentId,
                replyToAuthor = replyToAuthor,
                likeCount = 0,
                status = "active"
            )
            _guestComments.value = _guestComments.value + newComment
            _selectedPostComments.value = _guestComments.value.filter { it.postId == post.postId }
            _guestPosts.value = _guestPosts.value.map { p ->
                if (p.postId == post.postId) p.copy(commentCount = p.commentCount + 1) else p
            }
            _statusBanner.value = "Comment shared in Guest Mode"
            onComplete()
            return
        }
        val profile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" }
        )
        _isSubmitting.value = true
        viewModelScope.launch {
            val res = repository.addComment(
                post = post,
                authorProfile = profile,
                text = text,
                parentCommentId = parentCommentId,
                replyToAuthor = replyToAuthor
            )
            _isSubmitting.value = false
            res.onSuccess {
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to post comment"
            }
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        if (isGuest) {
            _guestComments.value = _guestComments.value.map { c ->
                if (c.commentId == commentId) c.copy(likeCount = c.likeCount + 1) else c
            }
            _selectedPostComments.value = _guestComments.value.filter { it.postId == postId }
            return
        }
        viewModelScope.launch {
            repository.toggleCommentLike(postId, commentId)
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId)
        }
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
        repository.markChatRead(chatId)
        activeChatJob?.cancel()
        activeChatJob = viewModelScope.launch {
            repository.observeMessages(chatId).collect { msgs ->
                _activeChatMessages.value = msgs
            }
        }
    }

    fun sendDirectMessage(
        peerUid: String,
        peerName: String,
        peerPhoto: String,
        text: String,
        attachmentUrl: String = "",
        attachmentType: String = "",
        fileName: String = "",
        fileSize: String = "",
        onSent: () -> Unit = {}
    ) {
        val profile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" }
        )
        viewModelScope.launch {
            repository.sendDirectMessage(
                senderProfile = profile,
                peerUid = peerUid,
                peerName = peerName,
                peerPhoto = peerPhoto,
                text = text,
                attachmentUrl = attachmentUrl,
                attachmentType = attachmentType,
                fileName = fileName,
                fileSize = fileSize
            ).onSuccess {
                onSent()
                // If chatting with a community member, generate a thoughtful automatic reply after 1.4s
                if (peerUid.contains("arjun", ignoreCase = true) || peerName.equals("Arjun", ignoreCase = true)) {
                    kotlinx.coroutines.delay(1400L)
                    val replyTexts = listOf(
                        "That is such a profound reflection. Art connects us in quiet, unexpected ways. ✨",
                        "I was just looking at that piece yesterday! The brushwork feels so intimate.",
                        "Thank you for sharing your thoughts—it really brightened my afternoon gallery walk."
                    )
                    repository.sendDirectMessage(
                        senderProfile = UserProfile(uid = peerUid, displayName = peerName, photoURL = peerPhoto),
                        peerUid = currentUserId,
                        peerName = profile.displayName,
                        peerPhoto = profile.photoURL,
                        text = replyTexts.random()
                    )
                } else if (peerUid.contains("elena", ignoreCase = true) || peerName.equals("Elena Vance", ignoreCase = true)) {
                    kotlinx.coroutines.delay(1400L)
                    val replyTexts = listOf(
                        "I love how classical paintings capture the tenderness of simple moments.",
                        "Wonderful to connect with you here! What style of art do you love most?",
                        "Your appreciation of art is inspiring. Keep creating and reflecting."
                    )
                    repository.sendDirectMessage(
                        senderProfile = UserProfile(uid = peerUid, displayName = peerName, photoURL = peerPhoto),
                        peerUid = currentUserId,
                        peerName = profile.displayName,
                        peerPhoto = profile.photoURL,
                        text = replyTexts.random()
                    )
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to send message"
            }
        }
    }

    suspend fun findUserByInviteCode(code: String): UserProfile? {
        return repository.findUserByInviteCode(code).getOrNull()
    }

    fun saveProfileChanges(
        displayName: String,
        bio: String,
        ageInput: String,
        gender: String,
        photoURL: String,
        bannerURL: String,
        themePalette: ArtmuseumThemePalette,
        notificationsEnabled: Boolean,
        profileVisibility: String,
        hideAge: Boolean = false,
        inviteCode: String = "",
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
                bannerURL = bannerURL,
                themePreference = themePalette.label,
                notificationsEnabled = notificationsEnabled,
                profileVisibility = profileVisibility,
                hideAge = hideAge,
                inviteCode = inviteCode
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

    fun updateBanner(dataUri: String, onComplete: () -> Unit = {}) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" }
        )
        _isSubmitting.value = true
        viewModelScope.launch {
            val res = repository.updateProfile(
                displayName = currentProfile.displayName,
                bio = currentProfile.bio,
                age = currentProfile.age,
                gender = currentProfile.gender,
                photoURL = currentProfile.photoURL,
                bannerURL = dataUri,
                themePreference = (_activeTheme.value).label,
                notificationsEnabled = currentProfile.notificationsEnabled,
                profileVisibility = currentProfile.profileVisibility
            )
            _isSubmitting.value = false
            res.onSuccess {
                _statusBanner.value = "Museum banner updated"
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to update banner"
            }
        }
    }

    fun toggleFollowUser(targetUid: String, targetName: String, targetPhotoURL: String) {
        if (targetUid == currentUserId) return
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(
            uid = currentUserId,
            displayName = initialDisplayName.ifBlank { "Sophia" }
        )
        viewModelScope.launch {
            repository.toggleFollowUser(
                targetUid = targetUid,
                targetName = targetName,
                targetPhotoURL = targetPhotoURL,
                actorProfile = currentProfile
            ).onSuccess { nowFollowing ->
                _statusBanner.value = if (nowFollowing) "Now following $targetName" else "Unfollowed $targetName"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Unable to update follow status"
            }
        }
    }

    fun observeFollowers(userId: String) = repository.observeFollowers(userId)
    fun observeFollowing(userId: String) = repository.observeFollowing(userId)

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
                    // Pre-measure bounds to prevent OutOfMemory on large camera images
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, boundsOptions)
                    }
                    val w = boundsOptions.outWidth
                    val h = boundsOptions.outHeight
                    if (w <= 0 || h <= 0) return@withContext null

                    val maxTargetDim = 1280
                    var sampleSize = 1
                    while ((w / sampleSize) > maxTargetDim * 2 || (h / sampleSize) > maxTargetDim * 2) {
                        sampleSize *= 2
                    }

                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, decodeOptions)
                    } ?: return@withContext null

                    encodeBitmapToCompactDataUri(sampledBitmap)
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed to encode URI to data URI", t)
                    null
                }
            }

        suspend fun encodeBitmapToCompactDataUri(bitmap: Bitmap): String? =
            withContext(Dispatchers.IO) {
                try {
                    val maxDim = 1280
                    val ratio = minOf(
                        maxDim.toFloat() / bitmap.width.coerceAtLeast(1),
                        maxDim.toFloat() / bitmap.height.coerceAtLeast(1),
                        1f
                    )
                    val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                    val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                    var scaled = if (targetW != bitmap.width || targetH != bitmap.height) {
                        Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                    } else {
                        bitmap
                    }
                    var quality = 84
                    var out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)

                    // Target <= 350KB byte size to guarantee Firestore acceptance & fast rendering
                    while (out.size() > 350_000 && quality > 40) {
                        quality -= 12
                        out = ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }

                    if (out.size() > 350_000) {
                        scaled = Bitmap.createScaledBitmap(scaled, (scaled.width * 0.8f).toInt().coerceAtLeast(1), (scaled.height * 0.8f).toInt().coerceAtLeast(1), true)
                        out = ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, 65, out)
                    }

                    val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    "data:image/jpeg;base64,$base64"
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed to encode bitmap", t)
                    null
                }
            }
    }
}
