package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.RotationY: ImageVector
    get() {
        if (_rotationY != null) {
            return _rotationY!!
        }
        _rotationY = materialIcon(name = "Filled.RotationY") {
            materialPath {
                moveTo(5.0f, 5.0f)
                lineTo(12.0f, 12.0f)
                lineTo(19.0f, 5.0f)
                moveTo(12.0f, 12.0f)
                lineTo(12.0f, 19.0f)
            }
        }
        return _rotationY!!
    }

private var _rotationY: ImageVector? = null
