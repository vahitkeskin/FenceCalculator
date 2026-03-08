package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.SwapLayoutResultRow
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CustomCardsScreen(
    viewModel: CalculatorViewModel,
    navController: NavController
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val customItems = viewModel.orderedVisibleItems.filter { it.id.startsWith("custom_") }
    var isEditMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { 
                    Text("ÖZEL KARTLAR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                },
                actions = {
                    if (customItems.isNotEmpty()) {
                        IconButton(onClick = { isEditMode = !isEditMode }) {
                            Icon(
                                if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                null,
                                tint = if (isEditMode) primaryColor else LocalContentColor.current
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                overscrollEffect = null
            ) {
                if (customItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp, bottom = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(primaryColor.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Extension,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = primaryColor.copy(alpha = 0.3f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Henüz Kart Yok",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Kendi özel hesaplama kartlarınızı oluşturarak uygulamayı kişiselleştirin.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }

                items(customItems, key = { it.id }) { item ->
                    val realId = item.id.removePrefix("custom_")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEditMode) {
                            Column {
                                IconButton(onClick = { viewModel.moveCardUp(item.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { viewModel.moveCardDown(item.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        
                        Box(modifier = Modifier.weight(1f).clickable { 
                            if (!isEditMode) navController.navigate("add_edit_card/$realId")
                        }) {
                            SwapLayoutResultRow(
                                item = item,
                                currentPriceInput = viewModel.getPriceString(item.id),
                                onPriceChange = { viewModel.onPriceChange(item.id, it) },
                                onPinToggle = { viewModel.togglePin(item.id) }
                            )
                        }

                        if (isEditMode) {
                            IconButton(onClick = { viewModel.hideCard(item.id) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { navController.navigate("add_edit_card/new") },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, null, tint = primaryColor)
                        Spacer(Modifier.width(8.dp))
                        Text("YENİ KART OLUŞTUR", fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
                
                // Klavye açıldığında en alttaki içeriğin yukarı kaydırılabilmesi için spacer
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}
