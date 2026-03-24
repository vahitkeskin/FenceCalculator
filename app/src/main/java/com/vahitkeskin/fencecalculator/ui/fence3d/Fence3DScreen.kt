package com.vahitkeskin.fencecalculator.ui.fence3d

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.vahitkeskin.fencecalculator.model.FenceResult
import com.vahitkeskin.fencecalculator.ui.fence3d.components.*
import kotlinx.coroutines.launch
import kotlin.math.*

// Tel çit simülasyonunu ve proje özetini gösteren ana ekran bileşeni
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Fence3DScreen(
    fenceResult: FenceResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        val pxWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val pxHeight = with(LocalDensity.current) { maxHeight.toPx() }

        val vScale = 25f
        val realWidth = sqrt(max(10f, fenceResult.totalLandLength) / 4f) * vScale * 1.8f
        val fenceH = fenceResult.height * vScale

        val initialScaleValue = remember(pxWidth, pxHeight, realWidth, fenceH) {
            val scaleW = (pxWidth * 0.75f) / realWidth
            val scaleH = (pxHeight * 0.45f) / fenceH
            min(scaleW, scaleH).coerceIn(0.8f, 15f)
        }
        val initialRotX = -23f
        val initialRotY = 36f

        val animRotationX = remember { Animatable(-60f) }
        val animRotationY = remember { Animatable(-180f) }
        val animScale = remember { Animatable(0.5f) }
        val appearanceAlpha = remember { Animatable(0f) }

        var rotationX by remember { mutableStateOf(initialRotX) }
        var rotationY by remember { mutableStateOf(initialRotY) }
        var scale by remember { mutableStateOf(initialScaleValue) }

        LaunchedEffect(initialScaleValue) { scale = initialScaleValue }

        val infiniteTransition = rememberInfiniteTransition()
        val windCycle by infiniteTransition.animateFloat(
            0f, 2f * PI.toFloat(),
            infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart)
        )

        LaunchedEffect(Unit) {
            launch { appearanceAlpha.animateTo(1f, tween(1000)) }
            launch { animRotationX.animateTo(rotationX, tween(1200, easing = LinearOutSlowInEasing)) }
            launch { animRotationY.animateTo(rotationY, tween(1200, easing = LinearOutSlowInEasing)) }
            launch { animScale.animateTo(scale, tween(1200, easing = LinearOutSlowInEasing)) }
        }

        val curX = if (animRotationX.isRunning) animRotationX.value else rotationX
        val curY = if (animRotationY.isRunning) animRotationY.value else rotationY
        val curS = if (animScale.isRunning) animScale.value else scale

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = appearanceAlpha.value)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        rotationY -= pan.x / 5f; rotationX -= pan.y / 5f; scale = (scale * zoom).coerceIn(0.5f, 25.0f)
                    }
                }
        ) {
            drawFenceGround(realWidth, curX, curY, curS, windCycle)
            drawFencePosts(fenceResult, realWidth, fenceH, curX, curY, curS)
            drawFenceMesh(fenceResult, realWidth, fenceH, curX, curY, curS)
            drawBarbedWire(realWidth, fenceH, curX, curY, curS)
        }

        Fence3DOverlay(
            fenceResult = fenceResult,
            curX = curX,
            curY = curY,
            curS = curS,
            onReset = {
                scope.launch {
                    val fromX = curX; val fromY = curY; val fromS = curS
                    animRotationX.snapTo(fromX); animRotationY.snapTo(fromY); animScale.snapTo(fromS)
                    launch { animRotationX.animateTo(initialRotX, tween(1000, easing = LinearOutSlowInEasing)) }
                    launch { animRotationY.animateTo(initialRotY, tween(1000, easing = LinearOutSlowInEasing)) }
                    launch { animScale.animateTo(initialScaleValue, tween(1000, easing = LinearOutSlowInEasing)) }
                    rotationX = initialRotX; rotationY = initialRotY; scale = initialScaleValue
                }
            }
        )
    }
}
