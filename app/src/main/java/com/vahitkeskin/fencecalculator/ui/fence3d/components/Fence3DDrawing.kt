package com.vahitkeskin.fencecalculator.ui.fence3d.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.vahitkeskin.fencecalculator.model.FenceResult
import com.vahitkeskin.fencecalculator.ui.fence3d.model.Vec3
import com.vahitkeskin.fencecalculator.ui.fence3d.utils.project
import kotlin.math.*

// Simülasyon zemini ve gölge alanını çizen yardımcı fonksiyon
fun DrawScope.drawFenceGround(
    realWidth: Float,
    curX: Float,
    curY: Float,
    curS: Float,
    windCycle: Float
) {
    val w = size.width
    val h = size.height
    val halfSide = realWidth / 2f

    val groundCorners = listOf(
        Vec3(-halfSide - 100, 0f, -halfSide - 100),
        Vec3(halfSide + 100, 0f, -halfSide - 100),
        Vec3(halfSide + 100, 0f, halfSide + 100),
        Vec3(-halfSide - 100, 0f, halfSide + 100)
    ).map { project(it, w, h, curX, curY, curS) }

    val groundPath = Path().apply {
        moveTo(groundCorners[0].x, groundCorners[0].y)
        groundCorners.forEach { lineTo(it.x, it.y) }
        close()
    }

    drawPath(
        path = groundPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2E7D32).copy(0.7f),
                Color(0xFF1B5E20).copy(0.9f)
            ), center = Offset(w / 2, h / 2), radius = realWidth * 3.5f * curS
        )
    )

    withTransform({ clipPath(groundPath) }) {
        drawGrass(realWidth, curX, curY, curS, windCycle)
    }
}

// Fizik tabanlı sallanan çim bıçaklarını çizen fonksiyon
fun DrawScope.drawGrass(
    realWidth: Float,
    curX: Float,
    curY: Float,
    curS: Float,
    windCycle: Float
) {
    val w = size.width
    val h = size.height
    val halfSide = realWidth / 2f
    val rand = java.util.Random(1337L)
    val grassGreen = Color(0xFF4CAF50)
    val darkGrass = Color(0xFF1B5E20)

    for (i in 0 until 3200) {
        val rx = rand.nextFloat() * (halfSide * 4.8f) - (halfSide * 2.4f)
        val rz = rand.nextFloat() * (halfSide * 4.8f) - (halfSide * 2.4f)
        val phase = (rx * 0.05f + rz * 0.05f)
        val swayRaw = sin(windCycle + phase)
        val swayX = swayRaw * (2.5f + rand.nextFloat() * 2f)
        val swayZ = cos(windCycle * 0.5f + phase) * 1.5f
        
        val pB = project(Vec3(rx, 0f, rz), w, h, curX, curY, curS)
        val pT = project(
            Vec3(rx + swayX, 5f + rand.nextFloat() * 5f, rz + swayZ),
            w, h, curX, curY, curS
        )
        
        drawLine(
            brush = Brush.verticalGradient(
                listOf(darkGrass.copy(0.6f), grassGreen.copy(0.8f))
            ), start = pB, end = pT, strokeWidth = 1.0f * curS, cap = StrokeCap.Round
        )
        if (i % 120 == 0) drawCircle(Color(0xFFFFEA00).copy(0.5f), 1.2f * curS, pT)
    }
}

// Beton direkleri ve köşe desteklerini çizen fonksiyon
fun DrawScope.drawFencePosts(
    fenceResult: FenceResult,
    realWidth: Float,
    fenceH: Float,
    curX: Float,
    curY: Float,
    curS: Float
) {
    val w = size.width
    val h = size.height
    val halfSide = realWidth / 2f
    val bTipH = 12f
    val bTipOut = 9f
    val postsPerSide = (fenceResult.postCount.coerceAtLeast(4) / 4)
    val concMain = Color(0xFFE0E0E0)
    val concEdge = Color(0xFF78909C).copy(0.6f)
    val shC = Color.Black.copy(0.4f)

    val sides = listOf(
        Pair(Vec3(-halfSide, 0f, -halfSide), Vec3(halfSide, 0f, -halfSide)),
        Pair(Vec3(halfSide, 0f, -halfSide), Vec3(halfSide, 0f, halfSide)),
        Pair(Vec3(halfSide, 0f, halfSide), Vec3(-halfSide, 0f, halfSide)),
        Pair(Vec3(-halfSide, 0f, halfSide), Vec3(-halfSide, 0f, -halfSide))
    )

    sides.forEach { (sV, eV) ->
        for (i in 0..postsPerSide) {
            val r = i.toFloat() / postsPerSide
            val x = sV.x + (eV.x - sV.x) * r
            val z = sV.z + (eV.z - sV.z) * r
            val base = Vec3(x, 0f, z)
            val joint = Vec3(x, fenceH, z)
            val mag = sqrt(x * x + z * z)
            val dx = if (mag > 0) x / mag else 0f
            val dz = if (mag > 0) z / mag else 0f
            val tip = Vec3(x + dx * bTipOut, fenceH + bTipH, z + dz * bTipOut)

            val pB = project(base, w, h, curX, curY, curS)
            val pJ = project(joint, w, h, curX, curY, curS)
            val pT = project(tip, w, h, curX, curY, curS)

            drawOval(shC, topLeft = pB - Offset(8f * curS, 2.2f * curS), size = Size(16f * curS, 4.4f * curS))
            drawLine(concEdge, pB, pJ, 5.2f * curS, StrokeCap.Square)
            drawLine(concMain, pB, pJ, 3.2f * curS, StrokeCap.Square)
            drawLine(concEdge, pJ, pT, 4.6f * curS, StrokeCap.Round)
            drawLine(concMain, pJ, pT, 2.4f * curS, StrokeCap.Round)

            if (i == 0) {
                val strutOffset = 28f
                val signX = if (sV.x < 0) 1f else -1f
                val signZ = if (sV.z < 0) 1f else -1f
                val s1Base = Vec3(sV.x + signX * strutOffset, 0f, sV.z)
                val s1Joint = Vec3(sV.x, fenceH * 0.65f, sV.z)
                val pS1B = project(s1Base, w, h, curX, curY, curS)
                val pS1J = project(s1Joint, w, h, curX, curY, curS)
                drawLine(concEdge, pS1B, pS1J, 4.8f * curS, StrokeCap.Round)
                drawLine(concMain, pS1B, pS1J, 2.6f * curS, StrokeCap.Round)
                val s2Base = Vec3(sV.x, 0f, sV.z + signZ * strutOffset)
                val pS2B = project(s2Base, w, h, curX, curY, curS)
                drawLine(concEdge, pS2B, pS1J, 4.8f * curS, StrokeCap.Round)
                drawLine(concMain, pS2B, pS1J, 2.6f * curS, StrokeCap.Round)
            }
        }
    }
}

// Tel örgü ağını çizen fonksiyon
fun DrawScope.drawFenceMesh(
    fenceResult: FenceResult,
    realWidth: Float,
    fenceH: Float,
    curX: Float,
    curY: Float,
    curS: Float
) {
    val w = size.width
    val h = size.height
    val halfSide = realWidth / 2f
    val vScale = 25f
    val meshEye = (fenceResult.meshEye / 50f) * vScale

    val sides = listOf(
        Pair(Vec3(-halfSide, 0f, -halfSide), Vec3(halfSide, 0f, -halfSide)),
        Pair(Vec3(halfSide, 0f, -halfSide), Vec3(halfSide, 0f, halfSide)),
        Pair(Vec3(halfSide, 0f, halfSide), Vec3(-halfSide, 0f, halfSide)),
        Pair(Vec3(-halfSide, 0f, halfSide), Vec3(-halfSide, 0f, -halfSide))
    )

    sides.forEach { (sV, eV) ->
        val mD = (realWidth / meshEye).toInt().coerceAtLeast(4)
        val hD = (fenceH / meshEye).toInt().coerceAtLeast(4)
        
        withTransform({
            val clP = Path().apply {
                val c1 = project(Vec3(sV.x, 0f, sV.z), w, h, curX, curY, curS)
                val c2 = project(Vec3(eV.x, 0f, eV.z), w, h, curX, curY, curS)
                val c3 = project(Vec3(eV.x, fenceH, eV.z), w, h, curX, curY, curS)
                val c4 = project(Vec3(sV.x, fenceH, sV.z), w, h, curX, curY, curS)
                moveTo(c1.x, c1.y); lineTo(c2.x, c2.y); lineTo(c3.x, c3.y); lineTo(c4.x, c4.y); close()
            }
            clipPath(clP)
        }) {
            for (m in -hD..mD + hD) {
                val r1 = (m.toFloat() / mD)
                val r2 = ((m + hD).toFloat() / mD)
                val p1 = project(Vec3(sV.x + (eV.x - sV.x) * r1, 0f, sV.z + (eV.z - sV.z) * r1), w, h, curX, curY, curS)
                val p2 = project(Vec3(sV.x + (eV.x - sV.x) * r2, fenceH, sV.z + (eV.z - sV.z) * r2), w, h, curX, curY, curS)
                drawLine(Color(0xFFB0BEC5).copy(0.35f), p1, p2, 0.15f * curS)
                
                val p3 = project(Vec3(sV.x + (eV.x - sV.x) * r2, 0f, sV.z + (eV.z - sV.z) * r2), w, h, curX, curY, curS)
                val p4 = project(Vec3(sV.x + (eV.x - sV.x) * r1, fenceH, sV.z + (eV.z - sV.z) * r1), w, h, curX, curY, curS)
                drawLine(Color(0xFFB0BEC5).copy(0.35f), p3, p4, 0.15f * curS)
            }
        }
    }
}

// Direklerin üzerindeki dikenli telleri çizen fonksiyon
fun DrawScope.drawBarbedWire(
    realWidth: Float,
    fenceH: Float,
    curX: Float,
    curY: Float,
    curS: Float
) {
    val w = size.width
    val h = size.height
    val halfSide = realWidth / 2f
    val bTipH = 12f
    val bTipOut = 9f

    val sides = listOf(
        Pair(Vec3(-halfSide, 0f, -halfSide), Vec3(halfSide, 0f, -halfSide)),
        Pair(Vec3(halfSide, 0f, -halfSide), Vec3(halfSide, 0f, halfSide)),
        Pair(Vec3(halfSide, 0f, halfSide), Vec3(-halfSide, 0f, halfSide)),
        Pair(Vec3(-halfSide, 0f, halfSide), Vec3(-halfSide, 0f, -halfSide))
    )

    sides.forEach { (sV, eV) ->
        val mS = sqrt(sV.x * sV.x + sV.z * sV.z)
        val dSX = if (mS > 0) sV.x / mS else 0f
        val dSZ = if (mS > 0) sV.z / mS else 0f
        val mE = sqrt(eV.x * eV.x + eV.z * eV.z)
        val dEX = if (mE > 0) eV.x / mE else 0f
        val dEZ = if (mE > 0) eV.z / mE else 0f

        for (j in 1..3) {
            val tr = j / 3.0f
            val wS = Vec3(sV.x + dSX * bTipOut * tr, fenceH + bTipH * tr, sV.z + dSZ * bTipOut * tr)
            val wE = Vec3(eV.x + dEX * bTipOut * tr, fenceH + bTipH * tr, eV.z + dEZ * bTipOut * tr)
            val pW1 = project(wS, w, h, curX, curY, curS)
            val pW2 = project(wE, w, h, curX, curY, curS)
            drawLine(Color(0xFFB0BEC5).copy(0.8f), pW1, pW2, 0.6f * curS)
            
            val barbCount = 10
            for (b in 1 until barbCount) {
                val bT = b.toFloat() / barbCount
                val bP = Vec3(wS.x + (wE.x - wS.x) * bT, wS.y + (wE.y - wS.y) * bT, wS.z + (wE.z - wS.z) * bT)
                val off = 1.5f
                val b1 = project(Vec3(bP.x - off, bP.y + off, bP.z + off), w, h, curX, curY, curS)
                val b2 = project(Vec3(bP.x + off, bP.y - off, bP.z - off), w, h, curX, curY, curS)
                val b3 = project(Vec3(bP.x + off, bP.y + off, bP.z - off), w, h, curX, curY, curS)
                val b4 = project(Vec3(bP.x - off, bP.y - off, bP.z + off), w, h, curX, curY, curS)
                drawLine(Color(0xFFCFD8DC).copy(0.9f), b1, b2, 0.5f * curS)
                drawLine(Color(0xFFCFD8DC).copy(0.9f), b3, b4, 0.5f * curS)
            }
        }
    }
}
