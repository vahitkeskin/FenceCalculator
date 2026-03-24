package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Filled.PlayStore: ImageVector
    get() {
        if (_playStore != null) {
            return _playStore!!
        }
        _playStore = ImageVector.Builder(
            name = "Filled.PlayStore",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Google Play Store Triangle shapes (Simplified)
            path(fill = SolidColor(Color(0xFF00C3E3))) { // Top
                 moveTo(17.523f, 13.041f)
                 lineTo(14.502f, 11.455f)
                 lineTo(2.317f, 22.128f)
                 curveToRelative(0.404f, 0.437f, 1.01f, 0.509f, 1.57f, 0.215f)
                 lineTo(17.523f, 13.041f)
                 close()
            }
            path(fill = SolidColor(Color(0xFFFFD500))) { // Bottom
                moveTo(22.046f, 11.233f)
                lineTo(17.523f, 8.868f)
                lineTo(14.502f, 10.454f)
                lineTo(17.523f, 12.04f)
                lineTo(22.046f, 9.676f)
                curveToRelative(0.605f, -0.317f, 0.605f, -0.832f, 0f, -1.149f)
                close()
            }
            path(fill = SolidColor(Color(0xFF00F076))) { // Right
                moveTo(17.523f, 7.959f)
                lineTo(3.887f, 0.793f)
                curveToRelative(-0.56f, -0.294f, -1.166f, -0.222f, -1.57f, 0.215f)
                lineTo(14.502f, 11.455f)
                lineTo(17.523f, 9.868f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFF3031))) { // Left
                moveTo(2.317f, 1.008f)
                curveTo(2.113f, 1.229f, 2f, 1.554f, 2f, 1.944f)
                verticalLineTo(21.056f)
                curveToRelative(0f, 0.39f, 0.113f, 0.715f, 0.317f, 0.936f)
                lineTo(11.979f, 11.455f)
                lineTo(2.317f, 1.008f)
                close()
            }
        }.build()
        return _playStore!!
    }

private var _playStore: ImageVector? = null
