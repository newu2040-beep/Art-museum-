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
    val isDark: Boolean = false
) {
    CREAM("Cream", 0xFFF7F2E8, 0xFFFFFDF8, 0xFF5D634E, 0xFF5D634E),
    SAGE("Sage", 0xFFEEF1EA, 0xFFF9FAF7, 0xFF56634D, 0xFF67755D),
    BLUE("Blue", 0xFFEDF2F5, 0xFFF8FBFD, 0xFF5C7B8E, 0xFF7596AA),
    ROSE("Rose", 0xFFF7EFEF, 0xFFFFFAFA, 0xFFA86458, 0xFFC98779),
    LAVENDER("Lavender", 0xFFF2EFF6, 0xFFFCFAFF, 0xFF7A6D94, 0xFF9688B0),
    TERRACOTTA("Terracotta", 0xFFF6EDE8, 0xFFFFF9F5, 0xFFA1583E, 0xFFB86E52),
    SUNSET("Sunset", 0xFFFAF0E6, 0xFFFFFBF7, 0xFFC26D43, 0xFFD88055),
    OLIVE("Olive Grove", 0xFFEFF1E8, 0xFFF9FAF5, 0xFF4A563E, 0xFF5B694D),
    MIDNIGHT("Midnight", 0xFF1F2326, 0xFF292E32, 0xFFA5B599, 0xFF4B5645, isDark = true),
    OBSIDIAN("Obsidian Gold", 0xFF121214, 0xFF1C1C20, 0xFFD4AF37, 0xFF3D3A2B, isDark = true),
    DARK_EMERALD("Dark Emerald", 0xFF111E18, 0xFF1A2B23, 0xFF7CA982, 0xFF2D4738, isDark = true),
    DARK_AMETHYST("Dark Amethyst", 0xFF181421, 0xFF231E30, 0xFFAD96DC, 0xFF3C3152, isDark = true),
    DARK_CHARCOAL("Dark Charcoal", 0xFF1A1A1D, 0xFF252529, 0xFFE07A5F, 0xFF423B38, isDark = true);

    companion object {
        fun fromName(name: String?): ArtmuseumThemePalette {
            return entries.firstOrNull { it.label.equals(name, ignoreCase = true) } ?: CREAM
        }
    }
}

val ARTMUSEUM_FEED_FILTERS = listOf(
    "For You",
    "Life",
    "Love",
    "Courage",
    "Growth",
    "Peace",
    "Art"
)

val ARTMUSEUM_TOPICS = listOf(
    "Life",
    "Love",
    "Courage",
    "Growth",
    "Peace",
    "Art"
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
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
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
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): UserProfile? {
            if (!doc.exists()) return null
            val uid = doc.getString("uid") ?: doc.id
            val displayName = doc.getString("displayName") ?: "Museum Member"
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
