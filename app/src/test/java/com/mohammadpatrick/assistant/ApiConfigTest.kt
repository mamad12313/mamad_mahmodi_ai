package com.mohammadpatrick.assistant

import com.mohammadpatrick.assistant.domain.model.ApiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiConfigTest {

    @Test
    fun testDefaultApiConfigIsMamadBackend() {
        val config = ApiConfig()
        assertTrue("Default API should be configured for mamad.pfmc.ir", config.isConfigured)
        assertEquals("https://mamad.pfmc.ir", config.baseUrl)
        assertTrue("Default API should be enabled", config.isEnabled)
    }

    @Test
    fun testValidHttpUrlConfigured() {
        val config = ApiConfig(baseUrl = "https://mamad.pfmc.ir")
        assertTrue("Valid HTTPS url should be configured", config.isConfigured)
    }

    @Test
    fun testInvalidUrlNotConfigured() {
        val config = ApiConfig(baseUrl = "ftp://invalid-url")
        assertFalse(config.isConfigured)

        val emptyConfig = ApiConfig(baseUrl = "   ")
        assertFalse(emptyConfig.isConfigured)
    }
}
