package com.vahitkeskin.fencecalculator.util

import java.math.BigInteger

object IbanValidator {

    /**
     * Validates a Turkish IBAN using ISO 7064 Mod 97-10 algorithm.
     * TR IBAN format: TRnn nnnn nnnn nnnn nnnn nnnn nn (26 characters)
     */
    fun isValidIban(iban: String): Boolean {
        // 1. Clean the input: Remove everything except uppercase letters and digits
        var cleanIban = iban.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }

        // 2. Handle common prefixes like "IBAN" or "TR" if only numbers were entered
        if (cleanIban.startsWith("IBAN")) {
            cleanIban = cleanIban.substring(4)
        }

        // 3. If it's 24 digits and doesn't start with letters, assume it's a TR IBAN missing the "TR" prefix
        if (cleanIban.length == 24 && cleanIban.all { it.isDigit() }) {
            cleanIban = "TR$cleanIban"
        }

        // 4. Basic IBAN checks: 
        // - Length must be between 15 and 34 characters
        // - Must start with two letters
        if (cleanIban.length < 15 || cleanIban.length > 34 || !cleanIban.substring(0, 2).all { it.isLetter() }) {
            return false
        }

        // 5. Move the first four characters to the end
        val movedIban = cleanIban.substring(4) + cleanIban.substring(0, 4)

        // 6. Replace letters with digits (A=10, B=11, ..., Z=35)
        val numericIban = StringBuilder()
        for (char in movedIban) {
            if (char.isDigit()) {
                numericIban.append(char)
            } else {
                val numericValue = char - 'A' + 10
                numericIban.append(numericValue)
            }
        }

        // 7. Interpret as a decimal integer and compute remainder modulo 97
        return try {
            val bigIntIban = BigInteger(numericIban.toString())
            bigIntIban.remainder(BigInteger.valueOf(97)).toInt() == 1
        } catch (e: Exception) {
            false
        }
    }
}
