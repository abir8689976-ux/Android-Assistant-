package com.example

import com.example.data.model.CommandClassification
import com.example.data.model.CommandType
import com.example.data.service.CommandRouterImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandRouterTest {

    private val router = CommandRouterImpl()

    @Test
    fun testNormalAiQueries() {
        val q1 = router.classify("Who is Albert Einstein?")
        assertEquals(CommandType.NORMAL_AI_QUERY, q1.type)

        val q2 = router.classify("বাংলাদেশের রাজধানী কী?")
        assertEquals(CommandType.NORMAL_AI_QUERY, q2.type)
    }

    @Test
    fun testAppLaunchCommands() {
        val q1 = router.classify("Open YouTube")
        assertEquals(CommandType.OPEN_APP_COMMAND, q1.type)
        assertTrue(q1 is CommandClassification.OpenAppCommand)
        assertEquals("YouTube", (q1 as CommandClassification.OpenAppCommand).targetName)

        val q2 = router.classify("Chrome খুলে দাও")
        assertEquals(CommandType.OPEN_APP_COMMAND, q2.type)
        assertTrue(q2 is CommandClassification.OpenAppCommand)
        assertEquals("Chrome", (q2 as CommandClassification.OpenAppCommand).targetName)

        val q3 = router.classify("Launch camera")
        assertEquals(CommandType.OPEN_APP_COMMAND, q3.type)
        assertEquals("Camera", (q3 as CommandClassification.OpenAppCommand).targetName)
    }

    @Test
    fun testUnsupportedDeviceCommands() {
        val q1 = router.classify("Turn on Bluetooth")
        assertEquals(CommandType.UNSUPPORTED_DEVICE_COMMAND, q1.type)

        val q2 = router.classify("ওয়াইফাই চালু করো")
        assertEquals(CommandType.UNSUPPORTED_DEVICE_COMMAND, q2.type)

        val q3 = router.classify("Turn on flashlight")
        assertEquals(CommandType.UNSUPPORTED_DEVICE_COMMAND, q3.type)
    }
}
