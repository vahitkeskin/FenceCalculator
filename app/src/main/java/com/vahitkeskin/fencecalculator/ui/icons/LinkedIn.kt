package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Filled.LinkedIn: ImageVector
    get() {
        if (_linkedIn != null) {
            return _linkedIn!!
        }
        _linkedIn = ImageVector.Builder(
            name = "Filled.LinkedIn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF0A66C2))) {
                moveTo(22.23f, 0f)
                lineTo(1.77f, 0f)
                curveTo(0.792f, 0f, 0f, 0.774f, 0f, 1.729f)
                verticalLineTo(22.271f)
                curveTo(0f, 23.227f, 0.792f, 24f, 1.77f, 24f)
                horizontalLineTo(22.23f)
                curveTo(23.208f, 24f, 24f, 23.227f, 24f, 22.271f)
                verticalLineTo(1.729f)
                curveTo(24f, 0.774f, 23.208f, 0f, 22.23f, 0f)
                close()
                moveTo(7.12f, 20.452f)
                horizontalLineTo(3.558f)
                verticalLineTo(9f)
                horizontalLineTo(7.12f)
                verticalLineTo(20.452f)
                close()
                moveTo(5.339f, 7.433f)
                curveTo(4.198f, 7.433f, 3.273f, 6.508f, 3.273f, 5.368f)
                curveTo(3.273f, 4.228f, 4.198f, 3.303f, 5.339f, 3.303f)
                curveTo(6.48f, 3.303f, 7.405f, 4.228f, 7.405f, 5.368f)
                curveTo(7.404f, 6.508f, 6.479f, 7.433f, 5.339f, 7.433f)
                close()
                moveTo(20.452f, 20.452f)
                horizontalLineTo(16.89f)
                verticalLineTo(14.877f)
                curveTo(16.89f, 13.548f, 16.864f, 11.838f, 15.039f, 11.838f)
                curveTo(13.188f, 11.838f, 12.903f, 13.284f, 12.903f, 14.78f)
                verticalLineTo(20.452f)
                horizontalLineTo(9.341f)
                verticalLineTo(9f)
                horizontalLineTo(12.759f)
                verticalLineTo(10.565f)
                horizontalLineTo(12.807f)
                curveTo(13.283f, 9.664f, 14.444f, 8.71f, 16.181f, 8.71f)
                curveTo(19.799f, 8.71f, 20.453f, 11.091f, 20.453f, 14.204f)
                verticalLineTo(20.452f)
                close()
            }
        }.build()
        return _linkedIn!!
    }

private var _linkedIn: ImageVector? = null
