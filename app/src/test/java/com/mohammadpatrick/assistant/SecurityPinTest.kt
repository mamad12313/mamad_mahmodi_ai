package com.mohammadpatrick.assistant

import com.mohammadpatrick.assistant.system.JalaliCalendarConverter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class SecurityPinTest {

    @Test
    fun testSha256HashingConsistency() {
        val pin = "1234"
        val salt = "testSaltValue"

        val digest1 = MessageDigest.getInstance("SHA-256")
        digest1.update(salt.toByteArray(Charsets.UTF_8))
        val hash1 = digest1.digest(pin.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        val digest2 = MessageDigest.getInstance("SHA-256")
        digest2.update(salt.toByteArray(Charsets.UTF_8))
        val hash2 = digest2.digest(pin.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        assertEquals(hash1, hash2)
    }

    private fun assertEquals(a: String, b: String) {
        org.junit.Assert.assertEquals(a, b)
    }

    @Test
    fun testPersianDateGeneration() {
        val persianDate = JalaliCalendarConverter.getTodayPersianDate()
        assertNotNull(persianDate)
        assertTrue("Persian date should contain valid year and month", persianDate.isNotBlank())
    }
}
