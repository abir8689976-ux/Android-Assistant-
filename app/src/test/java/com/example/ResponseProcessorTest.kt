package com.example

import com.example.data.service.AppLaunchResult
import com.example.domain.ResponseProcessorImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseProcessorTest {

    private val processor = ResponseProcessorImpl()

    @Test
    fun testSanitizeForSpeech() {
        val markdown = "Hello **world**, here is a `code` and *italics*."
        val sanitized = processor.sanitizeForSpeech(markdown)
        assertEquals("Hello world , here is a code and italics .", sanitized)
    }

    @Test
    fun testProcessAppLaunchResult() {
        val successResult = AppLaunchResult.Success("YouTube", "Successfully launched YouTube.")
        val normalizedSuccess = processor.processAppLaunchResult(successResult)
        assertFalse(normalizedSuccess.isError)
        assertEquals(true, normalizedSuccess.actionSuccess)

        val failedResult = AppLaunchResult.Failed("YouTube", "App not installed.")
        val normalizedFail = processor.processAppLaunchResult(failedResult)
        assertTrue(normalizedFail.isError)
        assertEquals(false, normalizedFail.actionSuccess)
    }
}
