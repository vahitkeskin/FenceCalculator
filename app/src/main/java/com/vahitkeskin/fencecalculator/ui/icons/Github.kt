package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Filled.Github: ImageVector
    get() {
        if (_github != null) {
            return _github!!
        }
        _github = ImageVector.Builder(
            name = "Filled.Github",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF24292F))) {
                moveTo(12f, 0f)
                curveTo(5.37f, 0f, 0f, 5.37f, 0f, 12f)
                curveToRelative(0f, 5.31f, 3.435f, 9.795f, 8.205f, 11.385f)
                curveToRelative(0.6f, 0.105f, 0.825f, -0.255f, 0.825f, -0.57f)
                curveToRelative(0f, -0.285f, -0.015f, -1.05f, -0.015f, -2.055f)
                curveToRelative(-3.33f, 0.72f, -4.035f, -1.605f, -4.035f, -1.605f)
                curveToRelative(-0.54f, -1.38f, -1.335f, -1.755f, -1.335f, -1.755f)
                curveToRelative(-1.08f, -0.735f, 0.075f, -0.72f, 0.075f, -0.72f)
                curveToRelative(1.2f, 0.09f, 1.83f, 1.23f, 1.83f, 1.23f)
                curveToRelative(1.065f, 1.83f, 2.805f, 1.305f, 3.495f, 0.99f)
                curveToRelative(0.105f, -0.765f, 0.42f, -1.305f, 0.75f, -1.59f)
                curveToRelative(-2.67f, -0.3f, -5.46f, -1.335f, -5.46f, -5.925f)
                curveToRelative(0f, -1.305f, 0.465f, -2.385f, 1.23f, -3.225f)
                curveToRelative(-0.12f, -0.3f, -0.54f, -1.53f, 0.12f, -3.18f)
                curveToRelative(0f, 0f, 1.005f, -0.315f, 3.3f, 1.23f)
                curveToRelative(0.96f, -0.27f, 1.98f, -0.405f, 3f, -0.405f)
                reflectiveCurveToRelative(2.04f, 0.135f, 3f, 0.405f)
                curveToRelative(2.295f, -1.56f, 3.3f, -1.23f, 3.3f, -1.23f)
                curveToRelative(0.66f, 1.65f, 0.24f, 2.88f, 0.12f, 3.18f)
                curveToRelative(0.765f, 0.84f, 1.23f, 1.905f, 1.23f, 3.225f)
                curveToRelative(0f, 4.605f, -2.805f, 5.625f, -5.475f, 5.925f)
                curveToRelative(0.435f, 0.375f, 0.81f, 1.11f, 0.81f, 2.22f)
                curveToRelative(0f, 1.605f, -0.015f, 2.895f, -0.015f, 3.3f)
                curveToRelative(0f, 0.315f, 0.225f, 0.69f, 0.825f, 0.57f)
                curveToRelative(4.77f, -1.59f, 8.205f, -6.075f, 8.205f, -11.385f)
                curveToRelative(0f, -6.63f, -5.37f, -12f, -12f, -12f)
                close()
            }
        }.build()
        return _github!!
    }

private var _github: ImageVector? = null
