package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.DirectChatMessage
import com.example.data.model.GUEST_PROFILE
import com.example.data.model.GUEST_SAMPLE_POSTS
import com.example.data.model.Post
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Artmuseum", appName)
  }

  @Test
  fun `verify twenty theme palettes and cute collection defined`() {
    val palettes = ArtmuseumThemePalette.entries
    assertEquals(20, palettes.size)
    val names = palettes.map { it.label }
    assertTrue(names.contains("Strawberry Milk"))
    assertTrue(names.contains("Peach Mochi"))
    assertTrue(names.contains("Matcha Latte"))
    assertTrue(names.contains("Butter & Honey"))
    assertTrue(names.contains("Cotton Candy"))
    assertTrue(names.contains("Mint Macaron"))
    assertTrue(names.contains("Vanilla Chai"))

    palettes.forEach { p ->
      assertTrue(p.primaryBackgroundHex != 0L)
      assertTrue(p.cardSurfaceHex != 0L)
      assertTrue(p.subtleSurfaceHex != 0L)
      assertTrue(p.borderHex != 0L)
      assertTrue(p.navActivePillHex != 0L)
    }
  }

  @Test
  fun `verify pure real-time data with zero fake demo posts and zero fake stats`() {
    assertTrue(GUEST_SAMPLE_POSTS.isEmpty())
    assertEquals(0, GUEST_PROFILE.followerCount)
    assertEquals(0, GUEST_PROFILE.followingCount)
    assertEquals(0, GUEST_PROFILE.postCount)
  }

  @Test
  fun `verify invite code generation, hide age, aspect ratio, and chat attachments`() {
    val code = UserProfile.generateDefaultInviteCode("user_sophia_123")
    assertTrue(code.startsWith("ART-"))
    assertEquals(10, code.length)

    val profile = UserProfile(uid = "user_sophia_123", displayName = "Sophia", hideAge = true, inviteCode = code)
    assertTrue(profile.hideAge)
    assertEquals(code, profile.inviteCode)

    val portraitPost = Post(postId = "p1", text = "Portrait study", aspectRatio = "4:5")
    assertEquals("4:5", portraitPost.aspectRatio)

    val attachmentMsg = DirectChatMessage(
      messageId = "m1",
      text = "Check out this sketch",
      attachmentUrl = "data:image/jpeg;base64,abc",
      attachmentType = "image",
      fileName = "sketch.jpg",
      fileSize = "240 KB"
    )
    assertEquals("image", attachmentMsg.attachmentType)
    assertEquals("sketch.jpg", attachmentMsg.fileName)
  }
}
