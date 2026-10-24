package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ActivityNotification
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.DirectChatConversation
import com.example.data.model.DirectChatMessage
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.UserBlock
import com.example.data.model.UserFollow
import com.example.data.model.UserProfile
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private fun DocumentReference.observeSnapshots(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            handleFirestoreError(error, OperationType.GET, path)
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

private fun Query.observeSnapshots(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            handleFirestoreError(error, OperationType.LIST, null)
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

class ArtmuseumRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank() && dbId != "(default)" && !dbId.startsWith("ai-studio-android-devpixel")) {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before writing to Firestore.")
    }

    // --- 1. User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.observeSnapshots()
            .map { snapshot -> UserProfile.fromSnapshot(snapshot) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
            }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snap = docRef.get().await()
            UserProfile.fromSnapshot(snap)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createOrInitializeProfile(
        displayName: String,
        bio: String,
        age: Int,
        gender: String,
        photoURL: String = "preset:avatar_sophia",
        themePreference: String = "Cream"
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            val existingProfile = UserProfile.fromSnapshot(existingSnap)
            if (existingProfile != null && existingProfile.accountStatus == "active") {
                return@runCatching existingProfile
            }
            val profile = UserProfile(
                documentId = uid,
                uid = uid,
                displayName = displayName.trim().ifEmpty { "Sophia" }.take(60),
                photoURL = photoURL.ifBlank { "preset:avatar_sophia" },
                bio = bio.trim().ifEmpty {
                    "Just a girl who finds beauty in meaningful conversations, art, nature and real connections. \uD83C\uDF3F"
                }.take(500),
                age = age.coerceIn(13, 120),
                gender = if (gender == "Male") "Male" else "Female",
                themePreference = ArtmuseumThemePalette.fromName(themePreference).label,
                notificationsEnabled = true,
                profileVisibility = "public",
                accountStatus = "active",
                postCount = 0,
                followerCount = 0,
                followingCount = 0,
                isVerified = false,
                inviteCode = UserProfile.generateDefaultInviteCode(uid),
                hideAge = false
            )
            docRef.set(profile.toCreateMap()).await()
            UserProfile.fromSnapshot(docRef.get().await()) ?: profile
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateProfile(
        displayName: String,
        bio: String,
        age: Int,
        gender: String,
        photoURL: String,
        bannerURL: String,
        themePreference: String,
        notificationsEnabled: Boolean,
        profileVisibility: String,
        hideAge: Boolean = false,
        inviteCode: String = ""
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            val effectiveCode = if (inviteCode.isNotBlank()) inviteCode.trim().take(32) else UserProfile.generateDefaultInviteCode(uid)
            if (!existingSnap.exists()) {
                val newProfile = UserProfile(
                    documentId = uid,
                    uid = uid,
                    displayName = displayName.trim().ifEmpty { "Sophia" }.take(60),
                    photoURL = photoURL.ifBlank { "preset:avatar_sophia" },
                    bannerURL = bannerURL.ifBlank { "preset:profile_header" },
                    bio = bio.trim().take(500),
                    age = age.coerceIn(13, 120),
                    gender = if (gender == "Male") "Male" else "Female",
                    themePreference = ArtmuseumThemePalette.fromName(themePreference).label,
                    notificationsEnabled = notificationsEnabled,
                    profileVisibility = if (profileVisibility == "private") "private" else "public",
                    accountStatus = "active",
                    inviteCode = effectiveCode,
                    hideAge = hideAge
                )
                docRef.set(newProfile.toCreateMap()).await()
            } else {
                val updates = mapOf<String, Any>(
                    "displayName" to displayName.trim().ifEmpty { "Sophia" }.take(60),
                    "bio" to bio.trim().take(500),
                    "age" to age.coerceIn(13, 120),
                    "gender" to if (gender == "Male") "Male" else "Female",
                    "photoURL" to photoURL.ifBlank { "preset:avatar_sophia" },
                    "bannerURL" to bannerURL.ifBlank { "preset:profile_header" },
                    "themePreference" to ArtmuseumThemePalette.fromName(themePreference).label,
                    "notificationsEnabled" to notificationsEnabled,
                    "profileVisibility" to if (profileVisibility == "private") "private" else "public",
                    "inviteCode" to effectiveCode,
                    "hideAge" to hideAge,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 1B. Real-Time Following & Followers ---

    fun observeFollowingUserIds(userId: String): Flow<Set<String>> {
        val path = "users/$userId/following"
        return db.collection("users").document(userId).collection("following")
            .whereEqualTo("followerUid", userId)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("followingUid") ?: it.id }.toSet()
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptySet())
            }
    }

    fun observeFollowers(userId: String): Flow<List<UserFollow>> {
        val path = "users/$userId/followers"
        return db.collection("users").document(userId).collection("followers")
            .whereEqualTo("followingUid", userId)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { UserFollow.fromSnapshot(it) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }
    }

    fun observeFollowing(userId: String): Flow<List<UserFollow>> {
        val path = "users/$userId/following"
        return db.collection("users").document(userId).collection("following")
            .whereEqualTo("followerUid", userId)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { UserFollow.fromSnapshot(it) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }
    }

    suspend fun toggleFollowUser(
        targetUid: String,
        targetName: String,
        targetPhotoURL: String,
        actorProfile: UserProfile
    ): Result<Boolean> = runCatching {
        val currentUid = requireUserId()
        require(currentUid != targetUid) { "Cannot follow yourself" }

        val myFollowingRef = db.collection("users").document(currentUid).collection("following").document(targetUid)
        val targetFollowerRef = db.collection("users").document(targetUid).collection("followers").document(currentUid)
        val myUserRef = db.collection("users").document(currentUid)
        val targetUserRef = db.collection("users").document(targetUid)

        try {
            val isFollowingSnap = myFollowingRef.get().await()
            if (isFollowingSnap.exists()) {
                // Unfollow
                myFollowingRef.delete().await()
                targetFollowerRef.delete().await()

                val mySnap = myUserRef.get().await()
                val targetSnap = targetUserRef.get().await()

                val currentMyFollowing = (mySnap.getLong("followingCount") ?: 1L).toInt()
                val currentTargetFollowers = (targetSnap.getLong("followerCount") ?: 1L).toInt()

                myUserRef.update("followingCount", (currentMyFollowing - 1).coerceAtLeast(0)).await()
                targetUserRef.update("followerCount", (currentTargetFollowers - 1).coerceAtLeast(0)).await()

                false
            } else {
                // Follow
                val followDataForMe = mapOf(
                    "followerUid" to currentUid,
                    "followingUid" to targetUid,
                    "targetName" to targetName.ifBlank { "Museum Member" },
                    "targetPhotoURL" to targetPhotoURL.ifBlank { "preset:avatar_sophia" },
                    "createdAt" to FieldValue.serverTimestamp()
                )
                val followDataForTarget = mapOf(
                    "followerUid" to currentUid,
                    "followingUid" to targetUid,
                    "targetName" to actorProfile.displayName.ifBlank { "Sophia" },
                    "targetPhotoURL" to actorProfile.photoURL.ifBlank { "preset:avatar_sophia" },
                    "createdAt" to FieldValue.serverTimestamp()
                )

                myFollowingRef.set(followDataForMe).await()
                targetFollowerRef.set(followDataForTarget).await()

                val mySnap = myUserRef.get().await()
                val targetSnap = targetUserRef.get().await()

                val currentMyFollowing = (mySnap.getLong("followingCount") ?: 0L).toInt()
                val currentTargetFollowers = (targetSnap.getLong("followerCount") ?: 0L).toInt()

                myUserRef.update("followingCount", currentMyFollowing + 1).await()
                targetUserRef.update("followerCount", currentTargetFollowers + 1).await()

                // Notify target user
                createNotification(
                    recipientUid = targetUid,
                    actorProfile = actorProfile,
                    type = "system",
                    message = "${actorProfile.displayName} started following your museum gallery."
                )

                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, myFollowingRef.path)
            throw e
        }
    }

    suspend fun updateThemePreference(themeName: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "themePreference" to ArtmuseumThemePalette.fromName(themeName).label,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observePublicUsers(): Flow<List<UserProfile>> {
        val path = "users"
        return db.collection(path)
            .whereEqualTo("profileVisibility", "public")
            .limit(40)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { UserProfile.fromSnapshot(it) }
                    .filter { it.accountStatus == "active" }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    // --- 2. Posts & Real-Time Feed ---

    fun observePublicPosts(limitCount: Long = 40L): Flow<List<Post>> {
        val path = "posts"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .limit(limitCount)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { Post.fromSnapshot(it) }
                    .filter { it.status == "active" }
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeUserPosts(authorId: String): Flow<List<Post>> {
        val path = "posts"
        return db.collection(path)
            .whereEqualTo("authorId", authorId)
            .limit(40)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { Post.fromSnapshot(it) }
                    .filter { it.status == "active" }
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun getPostById(postId: String): Result<Post?> = runCatching {
        val docRef = db.collection("posts").document(postId)
        try {
            val snap = docRef.get().await()
            Post.fromSnapshot(snap)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createPost(
        authorProfile: UserProfile,
        text: String,
        imageURL: String = "",
        imageCaption: String = "",
        topics: List<String> = emptyList(),
        themeName: String = "Cream",
        visibility: String = "public",
        aspectRatio: String = "1:1"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val postId = "post_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("posts").document(postId)
        try {
            val authorAge = if (authorProfile.hideAge) 0 else authorProfile.age.coerceIn(13, 120)
            val post = Post(
                documentId = postId,
                postId = postId,
                authorId = uid,
                authorNameSnapshot = authorProfile.displayName.ifBlank { "Sophia" },
                authorPhotoSnapshot = authorProfile.photoURL.ifBlank { "preset:avatar_sophia" },
                authorAgeSnapshot = authorAge,
                authorGenderSnapshot = if (authorProfile.gender == "Male") "Male" else "Female",
                text = text.trim().take(10000),
                imageURL = imageURL,
                imageCaption = imageCaption.trim().take(500),
                topics = topics,
                themeName = themeName,
                likeCount = 0,
                commentCount = 0,
                visibility = if (visibility == "private") "private" else "public",
                status = "active",
                aspectRatio = aspectRatio
            )
            docRef.set(post.toCreateMap()).await()
            postId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updatePost(
        postId: String,
        text: String,
        imageCaption: String,
        topics: List<String>,
        themeName: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            val cleanTopics = topics.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(10)
            val updates = mapOf<String, Any>(
                "text" to text.trim().take(10000),
                "imageCaption" to imageCaption.trim().take(500),
                "topics" to cleanTopics,
                "themeName" to ArtmuseumThemePalette.fromName(themeName).label,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 3. Idempotent Likes ---

    suspend fun isPostLiked(postId: String, userId: String): Boolean {
        val docRef = db.collection("posts").document(postId).collection("likes").document(userId)
        return try {
            docRef.get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun toggleLike(
        post: Post,
        actorProfile: UserProfile
    ): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val postRef = db.collection("posts").document(post.postId)
        val likeRef = postRef.collection("likes").document(uid)
        try {
            val existingLike = likeRef.get().await()
            val freshPostSnap = postRef.get().await()
            val currentLikes = (freshPostSnap.getLong("likeCount") ?: post.likeCount.toLong()).toInt()
            if (existingLike.exists()) {
                likeRef.delete().await()
                postRef.update(
                    mapOf(
                        "likeCount" to (currentLikes - 1).coerceAtLeast(0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                false
            } else {
                likeRef.set(
                    mapOf(
                        "uid" to uid,
                        "postId" to post.postId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                postRef.update(
                    mapOf(
                        "likeCount" to (currentLikes + 1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                if (post.authorId.isNotBlank() && post.authorId != uid) {
                    createNotification(
                        recipientUid = post.authorId,
                        actorProfile = actorProfile,
                        type = "like",
                        postId = post.postId,
                        message = "${actorProfile.displayName} appreciated your exhibit."
                    )
                }
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, likeRef.path)
            throw e
        }
    }

    // --- 4. Saved Posts (Bookmarks) ---

    fun observeSavedPostIds(userId: String): Flow<Set<String>> {
        val path = "users/$userId/savedPosts"
        return db.collection("users").document(userId).collection("savedPosts")
            .whereEqualTo("uid", userId)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("postId") ?: it.id }.toSet()
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun toggleSavePost(postId: String): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val savedRef = db.collection("users").document(uid).collection("savedPosts").document(postId)
        try {
            val snap = savedRef.get().await()
            if (snap.exists()) {
                savedRef.delete().await()
                false
            } else {
                savedRef.set(
                    mapOf(
                        "uid" to uid,
                        "postId" to postId,
                        "savedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, savedRef.path)
            throw e
        }
    }

    // --- 5. Comments ---

    fun observeComments(postId: String): Flow<List<PostComment>> {
        val path = "posts/$postId/comments"
        return db.collection("posts").document(postId).collection("comments")
            .whereEqualTo("status", "active")
            .limit(50)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { PostComment.fromSnapshot(it) }
                    .sortedBy { it.createdAt ?: Timestamp.now() }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addComment(
        post: Post,
        authorProfile: UserProfile,
        text: String,
        parentCommentId: String? = null,
        replyToAuthor: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val trimmed = text.trim().take(2000)
        require(trimmed.isNotEmpty()) { "Comment cannot be empty" }
        val commentId = "cmt_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val postRef = db.collection("posts").document(post.postId)
        val commentRef = postRef.collection("comments").document(commentId)
        try {
            val payload = mutableMapOf<String, Any>(
                "commentId" to commentId,
                "postId" to post.postId,
                "authorId" to uid,
                "authorNameSnapshot" to authorProfile.displayName.ifBlank { "Arjun" }.take(60),
                "authorPhotoSnapshot" to authorProfile.photoURL.ifBlank { "preset:avatar_daniel" },
                "text" to trimmed,
                "likeCount" to 0,
                "status" to "active",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!parentCommentId.isNullOrBlank()) {
                payload["parentCommentId"] = parentCommentId
            }
            if (!replyToAuthor.isNullOrBlank()) {
                payload["replyToAuthor"] = replyToAuthor
            }
            commentRef.set(payload).await()
            val freshPost = postRef.get().await()
            val currentComments = (freshPost.getLong("commentCount") ?: post.commentCount.toLong()).toInt()
            postRef.update(
                mapOf(
                    "commentCount" to (currentComments + 1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (post.authorId.isNotBlank() && post.authorId != uid) {
                createNotification(
                    recipientUid = post.authorId,
                    actorProfile = authorProfile,
                    type = "comment",
                    postId = post.postId,
                    message = if (!replyToAuthor.isNullOrBlank()) {
                        "${authorProfile.displayName} replied: \"${trimmed.take(60)}\""
                    } else {
                        "${authorProfile.displayName} reflected on your post: \"${trimmed.take(60)}\""
                    }
                )
            }
            commentId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            throw e
        }
    }

    suspend fun toggleCommentLike(postId: String, commentId: String): Result<Unit> = runCatching {
        requireUserId()
        val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
        try {
            val snap = commentRef.get().await()
            if (snap.exists()) {
                val currentLikes = (snap.getLong("likeCount") ?: 0L).toInt()
                commentRef.update(
                    mapOf(
                        "likeCount" to (currentLikes + 1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, commentRef.path)
            throw e
        }
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> = runCatching {
        requireUserId()
        val postRef = db.collection("posts").document(postId)
        val commentRef = postRef.collection("comments").document(commentId)
        try {
            commentRef.delete().await()
            val freshPost = postRef.get().await()
            if (freshPost.exists()) {
                val currentComments = (freshPost.getLong("commentCount") ?: 1L).toInt()
                postRef.update(
                    mapOf(
                        "commentCount" to (currentComments - 1).coerceAtLeast(0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, commentRef.path)
            throw e
        }
    }

    // --- 6. Notifications ---

    fun observeNotifications(userId: String): Flow<List<ActivityNotification>> {
        val path = "users/$userId/notifications"
        return db.collection("users").document(userId).collection("notifications")
            .whereEqualTo("recipientUid", userId)
            .limit(40)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { ActivityNotification.fromSnapshot(it) }
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createNotification(
        recipientUid: String,
        actorProfile: UserProfile,
        type: String,
        postId: String = "",
        message: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val notifId = "ntf_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val notifRef = db.collection("users").document(recipientUid)
            .collection("notifications").document(notifId)
        try {
            val payload = mapOf<String, Any?>(
                "notificationId" to notifId,
                "recipientUid" to recipientUid,
                "actorId" to uid,
                "actorName" to actorProfile.displayName.ifBlank { "Artmuseum" }.take(60),
                "actorPhotoURL" to actorProfile.photoURL.ifBlank { "preset:avatar_sophia" },
                "type" to if (type in listOf("like", "comment", "welcome", "system")) type else "system",
                "postId" to postId.takeIf { it.isNotBlank() },
                "message" to message.trim().take(300),
                "isRead" to false,
                "deliveryStatus" to "delivered",
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            notifRef.set(payload).await()
            notifId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notifRef.path)
            throw e
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val notifRef = db.collection("users").document(uid)
            .collection("notifications").document(notificationId)
        try {
            notifRef.update(
                mapOf(
                    "isRead" to true,
                    "deliveryStatus" to "read",
                    "readAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, notifRef.path)
            throw e
        }
    }

    // --- 7. Trust & Safety: Blocks, Reports, Account Deletion ---

    fun observeBlockedUsers(userId: String): Flow<List<UserBlock>> {
        val path = "users/$userId/blocks"
        return db.collection("users").document(userId).collection("blocks")
            .whereEqualTo("blockerUid", userId)
            .observeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { UserBlock.fromSnapshot(it) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun blockUser(blockedUid: String, blockedName: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val blockRef = db.collection("users").document(uid).collection("blocks").document(blockedUid)
        try {
            blockRef.set(
                mapOf(
                    "blockerUid" to uid,
                    "blockedUid" to blockedUid,
                    "blockedName" to blockedName.trim().take(60),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, blockRef.path)
            throw e
        }
    }

    suspend fun unblockUser(blockedUid: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val blockRef = db.collection("users").document(uid).collection("blocks").document(blockedUid)
        try {
            blockRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, blockRef.path)
            throw e
        }
    }

    suspend fun submitReport(
        targetId: String,
        targetType: String,
        reason: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val reportId = "rep_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val reportRef = db.collection("reports").document(reportId)
        try {
            reportRef.set(
                mapOf(
                    "reportId" to reportId,
                    "reporterUid" to uid,
                    "targetId" to targetId.take(128),
                    "targetType" to if (targetType in listOf("post", "comment", "user")) targetType else "post",
                    "reason" to reason.trim().ifEmpty { "Inappropriate content" }.take(500),
                    "status" to "open",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            reportId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reportRef.path)
            throw e
        }
    }

    suspend fun deleteAccountAndOwnContent(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        try {
            val ownPosts = db.collection("posts").whereEqualTo("authorId", uid).get().await()
            for (doc in ownPosts.documents) {
                doc.reference.delete().await()
            }
            userRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userRef.path)
            throw e
        }
    }

    // --- 8. Real-time exhibits initialization ---

    suspend fun ensureInitialExhibitsIfEmpty(currentProfile: UserProfile) {
        // Pure real-time data: No artificial demo posts, fake likes, or simulated comments
    }

    // --- 9. Direct Messaging System ---

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    private val localConversationsState = MutableStateFlow<List<DirectChatConversation>>(emptyList())

    private val localMessagesByChat = mutableMapOf<String, MutableStateFlow<List<DirectChatMessage>>>()

    fun observeConversations(currentUserId: String): Flow<List<DirectChatConversation>> {
        return localConversationsState.asStateFlow()
    }

    fun observeMessages(chatId: String): Flow<List<DirectChatMessage>> {
        val flow = localMessagesByChat.getOrPut(chatId) {
            MutableStateFlow(emptyList())
        }
        return flow.asStateFlow()
    }

    suspend fun findUserByInviteCode(inviteCode: String): Result<UserProfile?> = runCatching {
        val clean = inviteCode.trim().uppercase()
        if (clean.isBlank()) return@runCatching null

        // 1. Try direct exact match on inviteCode
        val byCodeSnap = db.collection("users")
            .whereEqualTo("inviteCode", clean)
            .limit(1)
            .get()
            .await()

        val foundDoc = byCodeSnap.documents.firstOrNull()
        if (foundDoc != null) {
            return@runCatching UserProfile.fromSnapshot(foundDoc)
        }

        // 2. Try match on document UID or generated invite code
        val byUidDoc = db.collection("users").document(clean).get().await()
        if (byUidDoc.exists()) {
            return@runCatching UserProfile.fromSnapshot(byUidDoc)
        }

        // 3. Scan public active users for prefix or derived code match
        val usersSnap = db.collection("users")
            .whereEqualTo("accountStatus", "active")
            .limit(60)
            .get()
            .await()

        val matched = usersSnap.documents.firstOrNull { doc ->
            val p = UserProfile.fromSnapshot(doc)
            p != null && (p.inviteCode.equals(clean, ignoreCase = true) ||
                    p.uid.equals(clean, ignoreCase = true) ||
                    UserProfile.generateDefaultInviteCode(p.uid).equals(clean, ignoreCase = true) ||
                    clean.contains(p.uid.take(6), ignoreCase = true))
        }

        matched?.let { UserProfile.fromSnapshot(it) }
    }

    suspend fun sendDirectMessage(
        senderProfile: UserProfile,
        peerUid: String,
        peerName: String,
        peerPhoto: String,
        text: String,
        attachmentUrl: String = "",
        attachmentType: String = "",
        fileName: String = "",
        fileSize: String = ""
    ): Result<DirectChatMessage> = runCatching {
        val uid = auth.currentUser?.uid ?: senderProfile.uid.ifBlank { "current_user" }
        val trimmed = text.trim().take(1000)
        require(trimmed.isNotEmpty() || attachmentUrl.isNotEmpty()) { "Message or attachment cannot be empty" }

        val chatId = if (peerUid.startsWith("chat_")) peerUid else "chat_${peerUid.take(16)}"
        val messageId = "msg_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val now = System.currentTimeMillis()
        val timeStr = timeFormat.format(Date(now))

        val previewSnippet = when {
            trimmed.isNotEmpty() -> trimmed
            attachmentType == "image" -> "📷 Shared an artwork image"
            attachmentType == "file" -> "📄 $fileName"
            else -> "Shared an attachment"
        }

        val newMsg = DirectChatMessage(
            messageId = messageId,
            chatId = chatId,
            senderUid = uid,
            receiverUid = peerUid,
            text = trimmed,
            attachmentUrl = attachmentUrl,
            attachmentType = attachmentType,
            fileName = fileName,
            fileSize = fileSize,
            timestamp = now,
            isFromMe = true,
            formattedTime = timeStr
        )

        val chatFlow = localMessagesByChat.getOrPut(chatId) { MutableStateFlow(emptyList()) }
        chatFlow.value = chatFlow.value + newMsg

        // Update or add conversation
        val existingConversations = localConversationsState.value.toMutableList()
        val existingIndex = existingConversations.indexOfFirst { it.chatId == chatId || it.peerUid == peerUid }
        val updatedConv = DirectChatConversation(
            chatId = chatId,
            peerUid = peerUid,
            peerName = peerName.ifBlank { "Museum Member" },
            peerPhotoURL = peerPhoto.ifBlank { "preset:avatar_sophia" },
            lastMessage = previewSnippet,
            lastMessageTimeFormatted = "Just now",
            unreadCount = 0,
            lastMessageTimestamp = now
        )
        if (existingIndex >= 0) {
            existingConversations.removeAt(existingIndex)
        }
        existingConversations.add(0, updatedConv)
        localConversationsState.value = existingConversations

        // Best effort Firestore sync and in-app/push notification to recipient
        try {
            val chatDoc = db.collection("chats").document(chatId)
            chatDoc.set(
                mapOf(
                    "chatId" to chatId,
                    "participantIds" to listOf(uid, peerUid).distinct(),
                    "lastMessage" to previewSnippet,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            chatDoc.collection("messages").document(messageId).set(
                mapOf(
                    "messageId" to messageId,
                    "chatId" to chatId,
                    "senderUid" to uid,
                    "receiverUid" to peerUid,
                    "text" to trimmed,
                    "attachmentType" to attachmentType,
                    "fileName" to fileName,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            createNotification(
                recipientUid = peerUid,
                actorProfile = senderProfile,
                type = "system",
                message = "${senderProfile.displayName}: $previewSnippet"
            )
        } catch (_: Exception) {
            // Local state is already updated
        }

        newMsg
    }

    fun markChatRead(chatId: String) {
        val current = localConversationsState.value
        localConversationsState.value = current.map {
            if (it.chatId == chatId) it.copy(unreadCount = 0) else it
        }
    }
}
