package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Filled.WhatsApp: ImageVector
    get() {
        if (_whatsApp != null) {
            return _whatsApp!!
        }
        _whatsApp = ImageVector.Builder(
            name = "Filled.WhatsApp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            // Background Rounded Square
            path(fill = SolidColor(Color(0xFF25D366))) {
                moveTo(76.8f, 0f)
                lineTo(435.2f, 0f)
                arcTo(76.8f, 76.8f, 0f, false, true, 512f, 76.8f)
                lineTo(512f, 435.2f)
                arcTo(76.8f, 76.8f, 0f, false, true, 435.2f, 512f)
                lineTo(76.8f, 512f)
                arcTo(76.8f, 76.8f, 0f, false, true, 0f, 435.2f)
                lineTo(0f, 76.8f)
                arcTo(76.8f, 76.8f, 0f, false, true, 76.8f, 0f)
                close()
            }
            // Speech Bubble Outline
            path(
                fill = SolidColor(Color(0xFF25D366)),
                stroke = SolidColor(Color.White),
                strokeLineWidth = 26f
            ) {
                moveTo(123f, 393f)
                lineToRelative(14f, -65f)
                arcToRelative(138f, 138f, 0f, true, true, 50f, 47f)
                close()
            }
            // Phone Hook
            path(fill = SolidColor(Color.White)) {
                moveTo(308f, 273f)
                curveToRelative(-3f, -2f, -6f, -3f, -9f, 1f)
                lineToRelative(-12f, 16f)
                curveToRelative(3f, 2f, -5f, 3f, -9f, 1f)
                curveToRelative(-15f, -8f, -36f, -17f, -54f, -47f)
                curveToRelative(-1f, -4f, 1f, -6f, 3f, -8f)
                lineToRelative(9f, -14f)
                curveToRelative(2f, -2f, 1f, -4f, 0f, -6f)
                lineToRelative(-12f, -29f)
                curveToRelative(-3f, -8f, -6f, -7f, -9f, -7f)
                horizontalLineToRelative(-8f)
                curveToRelative(-2f, 0f, -6f, 1f, -10f, 5f)
                curveToRelative(-22f, 22f, -13f, 53f, 3f, 73f)
                curveToRelative(3f, 4f, 23f, 40f, 66f, 59f)
                curveToRelative(32f, 14f, 39f, 12f, 48f, 10f)
                curveToRelative(11f, -1f, 22f, -10f, 27f, -19f)
                curveToRelative(1f, -3f, 6f, -16f, 2f, -18f)
                close()
            }
        }.build()
        return _whatsApp!!
    }

private var _whatsApp: ImageVector? = null
