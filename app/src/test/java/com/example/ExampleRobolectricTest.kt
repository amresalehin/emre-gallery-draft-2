package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Lumina", appName)
  }

  @Test
  fun `verify device folders derive dynamically from media without fake data`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.MediaRepository(context)
    val media = repo.mediaItems.value
    val nonTrash = media.filterNot { it.isTrash }
    
    // Ensure folders are dynamic
    val folders = nonTrash.groupBy { it.folderName }
    assert(folders.containsKey("Camera"))
    assert(folders.getValue("Camera").isNotEmpty())

    // Initial user albums start empty, no fake hardcoded numbers
    val albums = repo.albums.value
    assertEquals(0, albums.size)
  }
}
