package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ArtmuseumThemePalette(
    val label: String,
    val primaryBackgroundHex: Long,
    val cardSurfaceHex: Long,
    val accentHex: Long,
    val chipFillHex: Long,
    val isDark: Boolean = false,
    val subtleSurfaceHex: Long = 0xFFEFE8D8,
    val borderHex: Long = 0xFFE2D9C8,
    val navActivePillHex: Long = 0xFF4A3B32
) {
    CREAM("Cream", 0xFFF7F2E8, 0xFFFFFDF8, 0xFF5D634E, 0xFF5D634E, false, 0xFFEFE8D8, 0xFFE2D9C8, 0xFF4A3B32),
    SAGE("Sage", 0xFFEEF1EA, 0xFFF9FAF7, 0xFF56634D, 0xFF67755D, false, 0xFFE2E7DC, 0xFFD2D9CA, 0xFF526049),
    BLUE("Blue", 0xFFEDF2F5, 0xFFF8FBFD, 0xFF5C7B8E, 0xFF7596AA, false, 0xFFE0E9EE, 0xFFCCDCE5, 0xFF4D6F83),
    ROSE("Rose", 0xFFF7EFEF, 0xFFFFFAFA, 0xFFA86458, 0xFFC98779, false, 0xFFF2E2E2, 0xFFE5CECE, 0xFF965246),
    LAVENDER("Lavender", 0xFFF2EFF6, 0xFFFCFAFF, 0xFF7A6D94, 0xFF9688B0, false, 0xFFE8E2F0, 0xFFD6CFE2, 0xFF6A5D84),
    TERRACOTTA("Terracotta", 0xFFF6EDE8, 0xFFFFF9F5, 0xFFA1583E, 0xFFB86E52, false, 0xFFEEDDCE, 0xFFE2CBBA, 0xFF8F472D),
    SUNSET("Sunset", 0xFFFAF0E6, 0xFFFFFBF7, 0xFFC26D43, 0xFFD88055, false, 0xFFF3E2D0, 0xFFE6D0BA, 0xFFB05B32),
    OLIVE("Olive Grove", 0xFFEFF1E8, 0xFFF9FAF5, 0xFF4A563E, 0xFF5B694D, false, 0xFFE1E5D8, 0xFFD1D8C6, 0xFF434E37),
    STRAWBERRY_MILK("Strawberry Milk", 0xFFFDF0F4, 0xFFFFF8FA, 0xFFE0527F, 0xFFE96590, false, 0xFFFAE0E8, 0xFFF5CCD8, 0xFFD64473),
    PEACH_MOCHI("Peach Mochi", 0xFFFFF3ED, 0xFFFFF9F6, 0xFFF27A59, 0xFFF68D70, false, 0xFFFDE4D8, 0xFFF7D1BF, 0xFFE26A49),
    MATCHA_LATTE("Matcha Latte", 0xFFF0F5EC, 0xFFF9FCF7, 0xFF5F854D, 0xFF739A60, false, 0xFFE3EDE0, 0xFFD3E0CE, 0xFF537841),
    BUTTER_HONEY("Butter & Honey", 0xFFFEF9E8, 0xFFFFFDF5, 0xFFD99018, 0xFFE6A32E, false, 0xFFFCF0CD, 0xFFF4E2B0, 0xFFC48012),
    COTTON_CANDY("Cotton Candy", 0xFFF3F1FD, 0xFFFAF9FF, 0xFF7D67C7, 0xFF927CD8, false, 0xFFE7E3FA, 0xFFD7D1F4, 0xFF6F59B9),
    MINT_MACARON("Mint Macaron", 0xFFECF7F3, 0xFFF7FCFA, 0xFF3D9682, 0xFF4FA894, false, 0xFFDCF0E8, 0xFFC9E4D9, 0xFF328773),
    VANILLA_CHAI("Vanilla Chai", 0xFFF7F2EC, 0xFFFDFBFA, 0xFFA66E44, 0xFFB98055, false, 0xFFEFE4D8, 0xFFE0D3C4, 0xFF955F35),
    MIDNIGHT("Midnight", 0xFF181B1D, 0xFF22272B, 0xFFA5B599, 0xFF4B5645, true, 0xFF2C3237, 0xFF384046, 0xFF3A444C),
    OBSIDIAN("Obsidian Gold", 0xFF101012, 0xFF19191D, 0xFFD4AF37, 0xFF5C4F2A, true, 0xFF232328, 0xFF33333A, 0xFF363224),
    DARK_EMERALD("Dark Emerald", 0xFF0E1A14, 0xFF16251E, 0xFF7CA982, 0xFF2D4738, true, 0xFF1F3229, 0xFF2D4438, 0xFF263D31),
    DARK_AMETHYST("Dark Amethyst", 0xFF14101C, 0xFF1E1929, 0xFFAD96DC, 0xFF3C3152, true, 0xFF282236, 0xFF372F4A, 0xFF332945),
    DARK_CHARCOAL("Dark Charcoal", 0xFF151517, 0xFF202024, 0xFFE07A5F, 0xFF483A36, true, 0xFF2B2B30, 0xFF3A3A42, 0xFF3D322F);

    companion object {
        fun fromName(name: String?): ArtmuseumThemePalette {
            return entries.firstOrNull { it.label.equals(name, ignoreCase = true) } ?: CREAM
        }
    }
}

val ARTMUSEUM_FEED_FILTERS = listOf(
    "For You",
    "Art",
    "Life",
    "Love",
    "Courage",
    "Growth",
    "Peace",
    "Philosophy",
    "Poetry",
    "Solitude",
    "Nature",
    "Classical",
    "Modern",
    "Reflections"
)

val ARTMUSEUM_TOPICS = listOf(
    "Art",
    "Life",
    "Love",
    "Courage",
    "Growth",
    "Peace",
    "Philosophy",
    "Poetry",
    "Solitude",
    "Nature",
    "Classical",
    "Modern",
    "Reflections"
)

data class PresetArtwork(
    val key: String,
    val title: String,
    val subtitle: String,
    val defaultCaption: String
)

val PRESET_ARTWORKS = listOf(
    PresetArtwork(
        key = "preset:coastal_villa",
        title = "Mediterranean Villa at Golden Hour",
        subtitle = "Warm impressionist coastal landscape",
        defaultCaption = "Golden light resting softly on quiet waters."
    ),
    PresetArtwork(
        key = "preset:alpine_valley",
        title = "Sunlit Alpine Valley & River",
        subtitle = "Painterly mountain morning",
        defaultCaption = "Where the mountain air clears every hurried thought."
    ),
    PresetArtwork(
        key = "preset:meadow_reading",
        title = "Wildflower Meadow Afternoon",
        subtitle = "Soft botanical study in oil",
        defaultCaption = "Stillness among wildflowers and unhurried pages."
    ),
    PresetArtwork(
        key = "preset:golden_river",
        title = "Autumn Reflections on the Seine",
        subtitle = "Warm dusk landscape",
        defaultCaption = "Even the longest days soften into amber light."
    ),
    PresetArtwork(
        key = "preset:portrait_study",
        title = "Portrait in Quiet Light",
        subtitle = "Classical figurative study",
        defaultCaption = "Every person carries an unseen gallery of memories."
    ),
    PresetArtwork(
        key = "preset:welcome_sea",
        title = "Contemplation by the Sea",
        subtitle = "Classical coastal oil painting",
        defaultCaption = "Art is not what you see, but what you make others see."
    )
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val documentId: String = "",
    val uid: String = "",
    val displayName: String = "",
    val photoURL: String = "preset:avatar_sophia",
    val bannerURL: String = "preset:profile_header",
    val bio: String = "",
    val age: Int = 23,
    val gender: String = "Female",
    val themePreference: String = "Cream",
    val notificationsEnabled: Boolean = true,
    val profileVisibility: String = "public",
    val accountStatus: String = "active",
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = true,
    val inviteCode: String = "",
    val hideAge: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val code = if (inviteCode.isNotBlank()) inviteCode.trim().take(32) else generateDefaultInviteCode(uid)
        return mapOf<String, Any?>(
            "uid" to uid,
            "displayName" to displayName.trim().take(60),
            "photoURL" to photoURL,
            "bannerURL" to bannerURL,
            "bio" to bio.trim().take(500),
            "age" to age.coerceIn(13, 120),
            "gender" to if (gender == "Male") "Male" else "Female",
            "themePreference" to ArtmuseumThemePalette.fromName(themePreference).label,
            "notificationsEnabled" to notificationsEnabled,
            "profileVisibility" to if (profileVisibility == "private") "private" else "public",
            "accountStatus" to "active",
            "postCount" to postCount.coerceAtLeast(0),
            "followerCount" to followerCount.coerceAtLeast(0),
            "followingCount" to followingCount.coerceAtLeast(0),
            "isVerified" to isVerified,
            "inviteCode" to code,
            "hideAge" to hideAge,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun generateDefaultInviteCode(uid: String): String {
            val clean = uid.filter { it.isLetterOrDigit() }.uppercase()
            val code = if (clean.length >= 6) clean.take(6) else (clean + "MUSEUM").take(6)
            return "ART-$code"
        }

        fun fromSnapshot(doc: DocumentSnapshot): UserProfile? {
            if (!doc.exists()) return null
            val uid = doc.getString("uid") ?: doc.id
            val displayName = doc.getString("displayName") ?: "Museum Member"
            val storedCode = doc.getString("inviteCode")
            val code = if (!storedCode.isNullOrBlank()) storedCode else generateDefaultInviteCode(uid)
            return UserProfile(
                documentId = doc.id,
                uid = uid,
                displayName = displayName,
                photoURL = doc.getString("photoURL") ?: "preset:avatar_sophia",
                bannerURL = doc.getString("bannerURL") ?: "preset:profile_header",
                bio = doc.getString("bio") ?: "",
                age = (doc.getLong("age") ?: 23L).toInt(),
                gender = doc.getString("gender") ?: "Female",
                themePreference = doc.getString("themePreference") ?: "Cream",
                notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                profileVisibility = doc.getString("profileVisibility") ?: "public",
                accountStatus = doc.getString("accountStatus") ?: "active",
                postCount = (doc.getLong("postCount") ?: 0L).toInt(),
                followerCount = (doc.getLong("followerCount") ?: 0L).toInt(),
                followingCount = (doc.getLong("followingCount") ?: 0L).toInt(),
                isVerified = doc.getBoolean("isVerified") ?: true,
                inviteCode = code,
                hideAge = doc.getBoolean("hideAge") ?: false,
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

@IgnoreExtraProperties
data class UserFollow(
    @DocumentId val documentId: String = "",
    val followerUid: String = "",
    val followingUid: String = "",
    val targetName: String = "",
    val targetPhotoURL: String = "preset:avatar_sophia",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): UserFollow? {
            if (!doc.exists()) return null
            return UserFollow(
                documentId = doc.id,
                followerUid = doc.getString("followerUid") ?: "",
                followingUid = doc.getString("followingUid") ?: doc.id,
                targetName = doc.getString("targetName") ?: "Museum Member",
                targetPhotoURL = doc.getString("targetPhotoURL") ?: "preset:avatar_sophia",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

@IgnoreExtraProperties
data class Post(
    @DocumentId val documentId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorNameSnapshot: String = "",
    val authorPhotoSnapshot: String = "preset:avatar_sophia",
    val authorAgeSnapshot: Int = 23,
    val authorGenderSnapshot: String = "Female",
    val text: String = "",
    val imageURL: String = "",
    val imageStoragePath: String = "",
    val imageCaption: String = "",
    val topics: List<String> = emptyList(),
    val themeName: String = "Cream",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val visibility: String = "public",
    val status: String = "active",
    val aspectRatio: String = "1:1",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val cleanTopics = topics.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(10)
        return mapOf<String, Any?>(
            "postId" to postId,
            "authorId" to authorId,
            "authorNameSnapshot" to authorNameSnapshot.trim().ifEmpty { "Museum Member" }.take(60),
            "authorPhotoSnapshot" to authorPhotoSnapshot,
            "authorAgeSnapshot" to authorAgeSnapshot.coerceIn(13, 120),
            "authorGenderSnapshot" to if (authorGenderSnapshot == "Male") "Male" else "Female",
            "text" to text.trim().take(10000),
            "imageURL" to imageURL.takeIf { it.isNotBlank() },
            "imageStoragePath" to imageStoragePath.takeIf { it.isNotBlank() },
            "imageCaption" to imageCaption.trim().take(500).takeIf { it.isNotBlank() },
            "topics" to cleanTopics,
            "themeName" to ArtmuseumThemePalette.fromName(themeName).label,
            "likeCount" to 0,
            "commentCount" to 0,
            "visibility" to if (visibility == "private") "private" else "public",
            "status" to "active",
            "aspectRatio" to aspectRatio,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Post? {
            if (!doc.exists()) return null
            val rawTopics = (doc.get("topics") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            return Post(
                documentId = doc.id,
                postId = doc.getString("postId") ?: doc.id,
                authorId = doc.getString("authorId") ?: "",
                authorNameSnapshot = doc.getString("authorNameSnapshot") ?: "Museum Member",
                authorPhotoSnapshot = doc.getString("authorPhotoSnapshot") ?: "preset:avatar_sophia",
                authorAgeSnapshot = (doc.getLong("authorAgeSnapshot") ?: 23L).toInt(),
                authorGenderSnapshot = doc.getString("authorGenderSnapshot") ?: "Female",
                text = doc.getString("text") ?: "",
                imageURL = doc.getString("imageURL") ?: "",
                imageStoragePath = doc.getString("imageStoragePath") ?: "",
                imageCaption = doc.getString("imageCaption") ?: "",
                topics = rawTopics,
                themeName = doc.getString("themeName") ?: "Cream",
                likeCount = (doc.getLong("likeCount") ?: 0L).toInt(),
                commentCount = (doc.getLong("commentCount") ?: 0L).toInt(),
                visibility = doc.getString("visibility") ?: "public",
                status = doc.getString("status") ?: "active",
                aspectRatio = doc.getString("aspectRatio") ?: "1:1",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

@IgnoreExtraProperties
data class PostComment(
    @DocumentId val documentId: String = "",
    val commentId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorNameSnapshot: String = "",
    val authorPhotoSnapshot: String = "preset:avatar_daniel",
    val text: String = "",
    val likeCount: Int = 0,
    val status: String = "active",
    val parentCommentId: String? = null,
    val replyToAuthor: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): PostComment? {
            if (!doc.exists()) return null
            return PostComment(
                documentId = doc.id,
                commentId = doc.getString("commentId") ?: doc.id,
                postId = doc.getString("postId") ?: "",
                authorId = doc.getString("authorId") ?: "",
                authorNameSnapshot = doc.getString("authorNameSnapshot") ?: "Museum Member",
                authorPhotoSnapshot = doc.getString("authorPhotoSnapshot") ?: "preset:avatar_daniel",
                text = doc.getString("text") ?: "",
                likeCount = (doc.getLong("likeCount") ?: 0L).toInt(),
                status = doc.getString("status") ?: "active",
                parentCommentId = doc.getString("parentCommentId"),
                replyToAuthor = doc.getString("replyToAuthor"),
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

@IgnoreExtraProperties
data class ActivityNotification(
    @DocumentId val documentId: String = "",
    val notificationId: String = "",
    val recipientUid: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorPhotoURL: String = "preset:avatar_sophia",
    val type: String = "like",
    val postId: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val deliveryStatus: String = "delivered",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val readAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ActivityNotification? {
            if (!doc.exists()) return null
            return ActivityNotification(
                documentId = doc.id,
                notificationId = doc.getString("notificationId") ?: doc.id,
                recipientUid = doc.getString("recipientUid") ?: "",
                actorId = doc.getString("actorId") ?: "",
                actorName = doc.getString("actorName") ?: "Artmuseum",
                actorPhotoURL = doc.getString("actorPhotoURL") ?: "preset:avatar_sophia",
                type = doc.getString("type") ?: "system",
                postId = doc.getString("postId") ?: "",
                message = doc.getString("message") ?: "",
                isRead = doc.getBoolean("isRead") ?: false,
                deliveryStatus = doc.getString("deliveryStatus") ?: "delivered",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
                readAt = doc.getTimestamp("readAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

@IgnoreExtraProperties
data class UserBlock(
    @DocumentId val documentId: String = "",
    val blockerUid: String = "",
    val blockedUid: String = "",
    val blockedName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): UserBlock? {
            if (!doc.exists()) return null
            return UserBlock(
                documentId = doc.id,
                blockerUid = doc.getString("blockerUid") ?: "",
                blockedUid = doc.getString("blockedUid") ?: doc.id,
                blockedName = doc.getString("blockedName") ?: "Blocked User",
                createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        }
    }
}

@IgnoreExtraProperties
data class DirectChatConversation(
    val chatId: String = "",
    val peerUid: String = "",
    val peerName: String = "",
    val peerPhotoURL: String = "preset:avatar_sophia",
    val lastMessage: String = "",
    val lastMessageTimeFormatted: String = "Just now",
    val unreadCount: Int = 0,
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class DirectChatMessage(
    val messageId: String = "",
    val chatId: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val text: String = "",
    val attachmentUrl: String = "",
    val attachmentType: String = "", // "image", "file", or ""
    val fileName: String = "",
    val fileSize: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false,
    val formattedTime: String = "12:00 PM"
)

val GUEST_PROFILE = UserProfile(
    documentId = "guest_visitor",
    uid = "guest_visitor",
    displayName = "Guest Explorer",
    bio = "Exploring Artmuseum in Guest Mode.",
    age = 22,
    gender = "Visitor",
    photoURL = "preset:avatar_sophia",
    bannerURL = "preset:profile_header",
    themePreference = "Cream",
    postCount = 0,
    followerCount = 0,
    followingCount = 0,
    isVerified = false
)

val GUEST_SAMPLE_POSTS: List<Post> = emptyList()

val GUEST_SAMPLE_COMMENTS: List<PostComment> = emptyList()


