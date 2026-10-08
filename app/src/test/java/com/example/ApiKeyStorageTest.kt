package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.service.ApiKeyStorageImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApiKeyStorageTest {

    @Test
    fun testSaveAndRetrieveApiKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = ApiKeyStorageImpl(context)

        storage.clearCustomApiKey()
        assertFalse(storage.hasCustomApiKey())
        assertEquals("", storage.getCustomApiKey())

        // Save a test key
        val testKey = "AIzaSyTestKey1234567890"
        storage.saveCustomApiKey(testKey)

        assertTrue(storage.hasCustomApiKey())
        assertEquals(testKey, storage.getCustomApiKey())
        assertEquals(testKey, storage.getEffectiveApiKey())

        // Clear key
        storage.clearCustomApiKey()
        assertFalse(storage.hasCustomApiKey())
        assertEquals("", storage.getCustomApiKey())
    }
}
