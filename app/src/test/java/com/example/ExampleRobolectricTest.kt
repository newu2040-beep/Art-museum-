package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.GUEST_PROFILE
import com.example.data.model.GUEST_SAMPLE_POSTS
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
}
