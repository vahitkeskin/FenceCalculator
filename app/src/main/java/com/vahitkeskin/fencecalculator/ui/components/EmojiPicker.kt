package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val presetEmojis = listOf(
    "📦", "🔩", "🧱", "🪵", "🪨", "⚙️",
    "🔧", "🔨", "🪛", "🪜", "🏗️", "🚧",
    "💡", "🔌", "🪣", "🧲", "⛓️", "🪤",
    "🛠️", "📐", "📏", "✂️", "🪚", "🔑",
    "🏠", "🚪", "🪟", "🧹", "💰", "📋",
    "🎨", "🌿", "🌲", "💧", "🔥", "⚡"
)

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        Text(
            text = "KART EMOJİSİ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 260.dp)
        ) {
            items(presetEmojis) { emoji ->
                val isSelected = emoji == selectedEmoji
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier
                                .background(primaryColor.copy(alpha = 0.15f))
                                .border(2.dp, primaryColor.copy(alpha = 0.6f), CircleShape)
                            else Modifier
                                .background(onSurfaceColor.copy(alpha = 0.04f))
                                .border(1.dp, onSurfaceColor.copy(alpha = 0.08f), CircleShape)
                        )
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
