package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ArtmuseumRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProfileAndPost_authenticatedUser_succeedsAndEmitsInFeed() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ArtmuseumRepository(firestore, auth)

        val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrInitializeProfile(
                displayName = "Sophia",
                bio = "Art & meaningful conversations.",
                age = 23,
                gender = "Female"
            )
        }
        assertTrue(profileResult.isSuccess)
        val profile = profileResult.getOrThrow()
        assertEquals("Sophia", profile.displayName)

        val postIdResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createPost(
                authorProfile = profile,
                text = "Sometimes the most beautiful things in life aren't things at all.",
                imageURL = "preset:coastal_villa",
                imageCaption = "Golden hour",
                topics = listOf("Life", "Love"),
                themeName = "Cream"
            )
        }
        assertTrue(postIdResult.isSuccess)
        val postId = postIdResult.getOrThrow()
        assertTrue(postId.isNotEmpty())

        val posts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePublicPosts().first { list -> list.any { it.postId == postId } }
        }
        assertTrue(posts.any { it.postId == postId })
    }

    @Test
    fun savePost_crossUserObservation_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ArtmuseumRepository(firestore, auth)
        val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createOrInitializeProfile("Sophia", "Bio", 23, "Female").getOrThrow()
        }
        val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createPost(profile, "Exhibit text", topics = listOf("Art")).getOrThrow()
        }
        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.toggleSavePost(postId)
        }
        assertTrue(saveResult.isSuccess)

        // Switch to Bob and verify Bob cannot observe another user's private savedPosts subcollection
        signInTestUser(BOB_EMAIL)
        val bobRepo = ArtmuseumRepository(firestore, auth)
        val targetOtherUid = "other_user_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeSavedPostIds(targetOtherUid).first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED for cross-user savedPosts")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun observeSavedPosts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = ArtmuseumRepository(firestore, auth)
        val randomUid = "unauth_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSavedPostIds(randomUid).first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED when unauthenticated")
        } catch (e: FirebaseFirestoreException) {
            assertNotNull(e)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "sophia@artmuseum.test"
        const val BOB_EMAIL = "daniel@artmuseum.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
