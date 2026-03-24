package com.vahitkeskin.fencecalculator.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Giriş alanına odaklandığında onu ekranın (klavye dışında kalan alanın) ortasına getirir.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.centerOnFocus(): Modifier = composed {
    val scope = rememberCoroutineScope()
    val requester = remember { BringIntoViewRequester() }
    var itemSize by remember { mutableStateOf(Size.Zero) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    this
        .bringIntoViewRequester(requester)
        .onGloballyPositioned { itemSize = it.size.toSize() }
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    // Klavyenin açılma süresini ve UI'ın yeniden boyutlanmasını bekliyoruz.
                    delay(300)
                    
                    // Boyut henüz yakalanmadıysa biraz daha bekleyelim
                    if (itemSize.height <= 0) delay(150)
                    
                    if (itemSize.height > 0) {
                        // Rect koordinatları öğenin sol üst köşesine (0,0) göredir.
                        // Öğeyi tamamen görünür yapmak ve ortalamak için viewport'un %50-60'ını baz alıyoruz.
                        val viewportHeight = screenHeightPx * 0.5f 
                        val rect = Rect(
                            offset = Offset(0f, -(viewportHeight / 2 - itemSize.height / 2)),
                            size = Size(itemSize.width, viewportHeight)
                        )
                        requester.bringIntoView(rect)
                    } else {
                        // Boyut hala sıfırsa (nadir), en azından öğeyi görünür kılmak için requester'ı parametresiz çağırabiliriz
                        requester.bringIntoView()
                    }
                }
            }
        }
}
