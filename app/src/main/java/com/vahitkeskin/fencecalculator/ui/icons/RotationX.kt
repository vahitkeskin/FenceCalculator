package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.RotationX: ImageVector
    get() {
        if (_rotationX != null) {
            return _rotationX!!
        }
        _rotationX = materialIcon(name = "Filled.RotationX") {
            materialPath {
                moveTo(5.0f, 5.0f)
                lineTo(19.0f, 19.0f)
                moveTo(19.0f, 5.0f)
                lineTo(5.0f, 19.0f)
            }
        }
        return _rotationX!!
    }

private var _rotationX: ImageVector? = null
