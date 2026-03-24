package com.vahitkeskin.fencecalculator.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vahitkeskin.fencecalculator.ui.icons.WhatsApp
import com.vahitkeskin.fencecalculator.util.AppStrings

@Composable
fun ContactCard(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    PremiumGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clickable { onExpandedChange(!isExpanded) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.orderCardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = strings.orderContactText,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    PhoneNumberField(
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = onPhoneNumberChange,
                        label = strings.personalInfo,
                        selectCountryLabel = strings.selectCountry,
                        searchCountryLabel = strings.searchCountryOrCode,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        onBackgroundColor = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = strings.callAction,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        val fullMessage =
                            "${strings.playStoreLink}\n\n${strings.orderMessageTemplate}"

                        Button(
                            onClick = {
                                val cleanPhone = phoneNumber.replace("+", "").replace(" ", "")
                                val textEncoded = Uri.encode(fullMessage)
                                val url = "https://wa.me/$cleanPhone?text=$textEncoded"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WhatsApp,
                                contentDescription = strings.whatsappAction,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }

                        Button(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse("smsto:$phoneNumber")
                                ).apply {
                                    putExtra("sms_body", fullMessage)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = strings.smsAction,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
