package com.vahitkeskin.fencecalculator.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import io.michaelrocks.libphonenumber.android.AsYouTypeFormatter
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

class PhoneVisualTransformation(
    private val countryCode: String,
    private val phoneUtil: PhoneNumberUtil
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatter = phoneUtil.getAsYouTypeFormatter(countryCode)
        var formatted = ""
        val originalText = text.text

        originalText.forEach { char ->
            formatted = formatter.inputDigit(char)
        }

        return TransformedText(
            AnnotatedString(formatted),
            PhoneOffsetMapping(originalText, formatted)
        )
    }
}

class PhoneOffsetMapping(
    private val originalText: String,
    private val formattedText: String
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0
        var originalCount = 0
        var transformedIndex = 0
        while (originalCount < offset && transformedIndex < formattedText.length) {
            if (formattedText[transformedIndex].isDigit()) {
                originalCount++
            }
            transformedIndex++
        }
        return transformedIndex
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset <= 0) return 0
        var originalCount = 0
        var transformedIndex = 0
        while (transformedIndex < offset && transformedIndex < formattedText.length) {
            if (formattedText[transformedIndex].isDigit()) {
                originalCount++
            }
            transformedIndex++
        }
        return originalCount
    }
}
