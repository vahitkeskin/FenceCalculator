package com.vahitkeskin.fencecalculator

import com.vahitkeskin.fencecalculator.util.IbanValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IbanValidatorTest {

    @Test
    fun `valid TR IBAN should return true`() {
        // Valid TR IBAN from reliable source
        assertTrue(IbanValidator.isValidIban("TR33 0006 1005 1978 6457 8413 26"))
        assertTrue(IbanValidator.isValidIban("TR330006100519786457841326"))
    }

    @Test
    fun `IBAN with IBAN prefix should return true if valid`() {
        assertTrue(IbanValidator.isValidIban("IBAN TR 33 0006 1005 1978 6457 8413 26"))
    }

    @Test
    fun `IBAN with missing TR prefix should return true if 24 digits and valid`() {
        // "TR" + 24 digits = valid. If user only enters the 24 digits.
        assertTrue(IbanValidator.isValidIban("33 0006 1005 1978 6457 8413 26"))
    }

    @Test
    fun `invalid TR IBAN should return false`() {
        assertFalse(IbanValidator.isValidIban("TR123")) // Too short
        assertFalse(IbanValidator.isValidIban("TR330006100519786457841327")) // Wrong checksum
        assertFalse(IbanValidator.isValidIban("330006100519786457841327")) // Wrong checksum (no TR)
    }

    @Test
    fun `IBAN with spaces and mixed case should return true if valid`() {
        assertTrue(IbanValidator.isValidIban("tr 33 0006 1005 1978 6457 8413 26"))
    }
}
