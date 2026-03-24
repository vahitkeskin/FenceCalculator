package com.vahitkeskin.fencecalculator.ui.fence3d.utils

import androidx.compose.ui.geometry.Offset
import com.vahitkeskin.fencecalculator.ui.fence3d.model.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 3 boyutlu noktaları 2 boyutlu ekran düzlemine iz düşüren yardımcı fonksiyon
fun project(v: Vec3, w: Float, h: Float, rotX: Float, rotY: Float, s: Float): Offset {
    val radX = rotX * PI / 180f
    val radY = rotY * PI / 180f
    val cosX = cos(radX).toFloat()
    val sinX = sin(radX).toFloat()
    val cosY = cos(radY).toFloat()
    val sinY = sin(radY).toFloat()
    var x = v.x
    var y = v.y
    var z = v.z
    val y1 = y * cosX - z * sinX
    val z1 = y * sinX + z * cosX
    y = y1; z = z1
    val x2 = x * cosY + z * sinY
    val z2 = -x * sinY + z * cosY
    x = x2; z = z2
    val p = 1000f
    val factor = p / (p + z)
    return Offset(w / 2 + x * factor * s, h / 2 - y * factor * s)
}
