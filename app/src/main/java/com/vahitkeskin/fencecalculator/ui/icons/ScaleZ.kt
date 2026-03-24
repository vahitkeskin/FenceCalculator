package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.ScaleZ: ImageVector
    get() {
        if (_scaleZ != null) {
            return _scaleZ!!
        }
        _scaleZ = materialIcon(name = "Filled.ScaleZ") {
            materialPath {
                moveTo(6.0f, 6.0f)
                lineTo(18.0f, 6.0f)
                lineTo(6.0f, 18.0f)
                lineTo(18.0f, 18.0f)
            }
        }
        return _scaleZ!!
    }

private var _scaleZ: ImageVector? = null
