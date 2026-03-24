package com.vahitkeskin.fencecalculator.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Helper to build custom icons following the Material Icons pattern.
 */
fun materialIcon(
    name: String,
    block: ImageVector.Builder.() -> ImageVector.Builder
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).block().build()

/**
 * Helper to add a path to an [ImageVector.Builder] with default stroke styling.
 */
fun ImageVector.Builder.materialPath(
    strokeLineWidth: Float = 2.5f,
    strokeLineCap: StrokeCap = StrokeCap.Round,
    strokeLineJoin: StrokeJoin = StrokeJoin.Round,
    pathBuilder: PathBuilder.() -> Unit
) = path(
    fill = null,
    stroke = SolidColor(Color.Black),
    strokeLineWidth = strokeLineWidth,
    strokeLineCap = strokeLineCap,
    strokeLineJoin = strokeLineJoin,
    pathBuilder = pathBuilder
)
