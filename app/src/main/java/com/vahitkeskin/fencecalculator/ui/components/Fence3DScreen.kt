package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.model.FenceResult
import kotlinx.coroutines.launch
import kotlin.math.*

private data class Vec3(val x: Float, val y: Float, val z: Float)

/**
 * KMP-ready ULTIMATE 3D Fence Viewer / Screen.
 * ULTIMATE POLISH V8: Physics-Based Swaying (3200+ Blades), Fast Gusts.
 */
@Composable
fun Fence3DScreen(
    fenceResult: FenceResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val animRotationX = remember { Animatable(-60f) }
    val animRotationY = remember { Animatable(-180f) }
    val animScale = remember { Animatable(0.5f) }
    val appearanceAlpha = remember { Animatable(0f) }

    var rotationX by remember { mutableStateOf(-15f) }
    var rotationY by remember { mutableStateOf(35f) }
    var scale by remember { mutableStateOf(2.5f) }

    val infiniteTransition = rememberInfiniteTransition()
    val windCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(Unit) {
        launch { appearanceAlpha.animateTo(1f, tween(1000)) }
        launch { animRotationX.animateTo(rotationX, tween(1200, easing = LinearOutSlowInEasing)) }
        launch { animRotationY.animateTo(rotationY, tween(1200, easing = LinearOutSlowInEasing)) }
        launch { animScale.animateTo(scale, tween(1200, easing = LinearOutSlowInEasing)) }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        val curX = if (animRotationX.isRunning) animRotationX.value else rotationX
        val curY = if (animRotationY.isRunning) animRotationY.value else rotationY
        val curS = if (animScale.isRunning) animScale.value else scale

        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = appearanceAlpha.value).pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                rotationY += pan.x / 5f; rotationX -= pan.y / 5f; scale = (scale * zoom).coerceIn(0.5f, 25.0f)
            }
        }) {
            val w = size.width; val h = size.height
            val vScale = 25f
            val realWidth = sqrt(max(10f, fenceResult.totalLandLength) / 4f) * vScale * 1.8f 
            val halfSide = realWidth / 2f
            val fenceH = fenceResult.height * vScale
            val meshEye = (fenceResult.meshEye / 50f) * vScale
            val bTipH = 12f; val bTipOut = 9f
            
            // 1. DENSE PHYSICS GRASS (3200 Blades)
            val groundCorners = listOf(Vec3(-halfSide - 100, 0f, -halfSide - 100), Vec3(halfSide + 100, 0f, -halfSide - 100), Vec3(halfSide + 100, 0f, halfSide + 100), Vec3(-halfSide - 100, 0f, halfSide + 100))
                .map { project(it, w, h, curX, curY, curS) }
            val groundPath = Path().apply { moveTo(groundCorners[0].x, groundCorners[0].y); groundCorners.forEach { lineTo(it.x, it.y) }; close() }
            drawPath(path = groundPath, brush = Brush.radialGradient(colors = listOf(Color(0xFF2E7D32).copy(0.7f), Color(0xFF1B5E20).copy(0.9f)), center = Offset(w/2, h/2), radius = realWidth * 3.5f * curS))
            
            withTransform({ clipPath(groundPath) }) {
                val rand = java.util.Random(1337L)
                val grassGreen = Color(0xFF4CAF50); val darkGrass = Color(0xFF1B5E20)
                // Density increased to 3200 for "More Grass"
                for (i in 0 until 3200) {
                    val rx = rand.nextFloat() * (halfSide * 4.8f) - (halfSide * 2.4f)
                    val rz = rand.nextFloat() * (halfSide * 4.8f) - (halfSide * 2.4f)
                    
                    val phase = (rx * 0.05f + rz * 0.05f)
                    val swayRaw = sin(windCycle + phase)
                    val swayX = swayRaw * (2.5f + rand.nextFloat() * 2f) 
                    val swayZ = cos(windCycle * 0.5f + phase) * 1.5f
                    
                    val pB = project(Vec3(rx, 0f, rz), w, h, curX, curY, curS)
                    val pT = project(Vec3(rx + swayX, 5f + rand.nextFloat() * 5f, rz + swayZ), w, h, curX, curY, curS)
                    
                    drawLine(brush = Brush.verticalGradient(listOf(darkGrass.copy(0.6f), grassGreen.copy(0.8f))), start = pB, end = pT, strokeWidth = 1.0f * curS, cap = StrokeCap.Round)
                    
                    if (i % 120 == 0) drawCircle(Color(0xFFFFEA00).copy(0.5f), 1.2f * curS, pT)
                }
            }

            // 2. CONCRETE POSTS & CORNER STRUTS
            val postsPerSide = (fenceResult.postCount.coerceAtLeast(4) / 4)
            val concMain = Color(0xFFE0E0E0); val concEdge = Color(0xFF78909C).copy(0.6f); val shC = Color.Black.copy(0.4f)
            val sides = listOf(Pair(Vec3(-halfSide, 0f, -halfSide), Vec3(halfSide, 0f, -halfSide)), Pair(Vec3(halfSide, 0f, -halfSide), Vec3(halfSide, 0f, halfSide)), Pair(Vec3(halfSide, 0f, halfSide), Vec3(-halfSide, 0f, halfSide)), Pair(Vec3(-halfSide, 0f, halfSide), Vec3(-halfSide, 0f, -halfSide)))
            
            sides.forEach { (sV, eV) ->
                for (i in 0..postsPerSide) {
                    val r = i.toFloat() / postsPerSide
                    val x = sV.x + (eV.x - sV.x) * r; val z = sV.z + (eV.z - sV.z) * r
                    val base = Vec3(x, 0f, z); val joint = Vec3(x, fenceH, z); val mag = sqrt(x * x + z * z); val dx = if (mag > 0) x / mag else 0f; val dz = if (mag > 0) z / mag else 0f
                    val tip = Vec3(x + dx * bTipOut, fenceH + bTipH, z + dz * bTipOut)
                    val pB = project(base, w, h, curX, curY, curS); val pJ = project(joint, w, h, curX, curY, curS); val pT = project(tip, w, h, curX, curY, curS)
                    
                    drawOval(shC, topLeft = pB - Offset(8f * curS, 2.2f * curS), size = Size(16f * curS, 4.4f * curS))
                    drawLine(concEdge, pB, pJ, 5.2f * curS, StrokeCap.Square); drawLine(concMain, pB, pJ, 3.2f * curS, StrokeCap.Square)
                    drawLine(concEdge, pJ, pT, 4.6f * curS, StrokeCap.Round); drawLine(concMain, pJ, pT, 2.4f * curS, StrokeCap.Round)

                    if (i == 0) {
                        val strutOffset = 28f; val signX = if (sV.x < 0) 1f else -1f; val signZ = if (sV.z < 0) 1f else -1f
                        val s1Base = Vec3(sV.x + signX * strutOffset, 0f, sV.z); val s1Joint = Vec3(sV.x, fenceH * 0.65f, sV.z)
                        val pS1B = project(s1Base, w, h, curX, curY, curS); val pS1J = project(s1Joint, w, h, curX, curY, curS)
                        drawLine(concEdge, pS1B, pS1J, 4.8f * curS, StrokeCap.Round); drawLine(concMain, pS1B, pS1J, 2.6f * curS, StrokeCap.Round)
                        val s2Base = Vec3(sV.x, 0f, sV.z + signZ * strutOffset); val pS2B = project(s2Base, w, h, curX, curY, curS)
                        drawLine(concEdge, pS2B, pS1J, 4.8f * curS, StrokeCap.Round); drawLine(concMain, pS2B, pS1J, 2.6f * curS, StrokeCap.Round)
                    }
                }

                // 3. HYPER-DENSE MESH
                val mD = (realWidth / meshEye).toInt().coerceAtLeast(4); val hD = (fenceH / meshEye).toInt().coerceAtLeast(4)
                withTransform({
                    val clP = Path().apply {
                        val c1 = project(Vec3(sV.x, 0f, sV.z), w, h, curX, curY, curS); val c2 = project(Vec3(eV.x, 0f, eV.z), w, h, curX, curY, curS)
                        val c3 = project(Vec3(eV.x, fenceH, eV.z), w, h, curX, curY, curS); val c4 = project(Vec3(sV.x, fenceH, sV.z), w, h, curX, curY, curS)
                        moveTo(c1.x, c1.y); lineTo(c2.x, c2.y); lineTo(c3.x, c3.y); lineTo(c4.x, c4.y); close()
                    }
                    clipPath(clP)
                }) {
                    for (m in -hD..mD + hD) {
                        val r1 = (m.toFloat() / mD); val r2 = ((m + hD).toFloat() / mD)
                        val p1 = project(Vec3(sV.x + (eV.x - sV.x) * r1, 0f, sV.z + (eV.z - sV.z) * r1), w, h, curX, curY, curS); val p2 = project(Vec3(sV.x + (eV.x - sV.x) * r2, fenceH, sV.z + (eV.z - sV.z) * r2), w, h, curX, curY, curS)
                        drawLine(Color(0xFFB0BEC5).copy(0.35f), p1, p2, 0.15f * curS)
                        val p3 = project(Vec3(sV.x + (eV.x - sV.x) * r2, 0f, sV.z + (eV.z - sV.z) * r2), w, h, curX, curY, curS); val p4 = project(Vec3(sV.x + (eV.x - sV.x) * r1, fenceH, sV.z + (eV.z - sV.z) * r1), w, h, curX, curY, curS)
                        drawLine(Color(0xFFB0BEC5).copy(0.35f), p3, p4, 0.15f * curS)
                    }
                }
                
                // 4. BARBED TOP STRANDS (3D-Projected Barbs)
                val mS = sqrt(sV.x * sV.x + sV.z * sV.z); val dSX = if (mS > 0) sV.x / mS else 0f; val dSZ = if (mS > 0) sV.z / mS else 0f
                val mE = sqrt(eV.x * eV.x + eV.z * eV.z); val dEX = if (mE > 0) eV.x / mE else 0f; val dEZ = if (mE > 0) eV.z / mE else 0f
                for (j in 1..3) {
                    val tr = j / 3.0f
                    val wS = Vec3(sV.x + dSX * bTipOut * tr, fenceH + bTipH * tr, sV.z + dSZ * bTipOut * tr); val wE = Vec3(eV.x + dEX * bTipOut * tr, fenceH + bTipH * tr, eV.z + dEZ * bTipOut * tr)
                    val pW1 = project(wS, w, h, curX, curY, curS); val pW2 = project(wE, w, h, curX, curY, curS)
                    // Main wire
                    drawLine(Color(0xFFB0BEC5).copy(0.8f), pW1, pW2, 0.6f * curS)
                    
                    // Draw Barbs at intervals
                    val barbCount = 10
                    for (b in 1 until barbCount) {
                        val bT = b.toFloat() / barbCount
                        val bP = Vec3(wS.x + (wE.x - wS.x) * bT, wS.y + (wE.y - wS.y) * bT, wS.z + (wE.z - wS.z) * bT)
                        
                        // Small "X" shape barbs for realistic feel
                        val off = 1.5f
                        val b1 = project(Vec3(bP.x - off, bP.y + off, bP.z + off), w, h, curX, curY, curS)
                        val b2 = project(Vec3(bP.x + off, bP.y - off, bP.z - off), w, h, curX, curY, curS)
                        val b3 = project(Vec3(bP.x + off, bP.y + off, bP.z - off), w, h, curX, curY, curS)
                        val b4 = project(Vec3(bP.x - off, bP.y - off, bP.z + off), w, h, curX, curY, curS)
                        
                        drawLine(Color(0xFFCFD8DC).copy(0.9f), b1, b2, 0.5f * curS); drawLine(Color(0xFFCFD8DC).copy(0.9f), b3, b4, 0.5f * curS)
                    }
                }
            }
        }

        // --- OVERLAY (Glassmorphic Proje Özet) ---
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, Color.White.copy(0.12f)),
                modifier = Modifier.blur(0.5.dp) // Subtle softening
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(0.15f), shape = CircleShape) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(6.dp).size(14.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("PROJE ÖZETİ", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        
                        // Physical Stats
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            StatItem(Icons.Default.Square, "${fenceResult.postCount}", "Direk")
                            StatItem(Icons.Default.Height, "${fenceResult.height}m", "Boy")
                            StatItem(Icons.Default.Grid4x4, "${fenceResult.meshEye}cm", "Göz")
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp).width(120.dp), color = Color.White.copy(0.1f), thickness = 1.dp)
                        
                        // View Stats (XYZ Real-time)
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            ViewStatItem(null, "X: ${curX.toInt()}°")
                            ViewStatItem(null, "Y: ${curY.toInt()}°")
                            ViewStatItem(Icons.Default.ZoomIn, "Z: %.1fX".format(curS))
                        }
                    }
                    
                    val infiniteT = rememberInfiniteTransition(); val icS by infiniteT.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(1200), RepeatMode.Reverse))
                    Surface(
                        modifier = Modifier.size(56.dp).clickable {
                            scope.launch {
                                launch { rotationX = -15f; animRotationX.snapTo(rotationX); animRotationX.animateTo(-15f, tween(1000, easing = LinearOutSlowInEasing)) }
                                launch { rotationY = 35f; animRotationY.snapTo(rotationY); animRotationY.animateTo(35f, tween(1000, easing = LinearOutSlowInEasing)) }
                                launch { scale = 2.5f; animScale.snapTo(scale); animScale.animateTo(2.5f, tween(1000, easing = LinearOutSlowInEasing)) }
                            }
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sıfırla",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp).graphicsLayer(scaleX = icS, scaleY = icS)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ViewStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector?, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(0.7f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
    }
}

private fun project(v: Vec3, w: Float, h: Float, rotX: Float, rotY: Float, s: Float): Offset {
    val radX = rotX * PI / 180f; val radY = rotY * PI / 180f
    val cosX = cos(radX).toFloat(); val sinX = sin(radX).toFloat()
    val cosY = cos(radY).toFloat(); val sinY = sin(radY).toFloat()
    var x = v.x; var y = v.y; var z = v.z
    val y1 = y * cosX - z * sinX; val z1 = y * sinX + z * cosX
    y = y1; z = z1
    val x2 = x * cosY + z * sinY; val z2 = -x * sinY + z * cosY
    x = x2; z = z2
    val p = 1000f; val factor = p / (p + z)
    return Offset(w / 2 + x * factor * s, h / 2 - y * factor * s)
}
